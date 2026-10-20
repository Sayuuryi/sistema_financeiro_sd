import http from "node:http";
import https from "node:https";
import { readFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";

const root = path.dirname(fileURLToPath(import.meta.url));
const mime = {
  ".html": "text/html",
  ".js": "text/javascript",
  ".css": "text/css",
};
const hopHeaders = [
  "connection",
  "keep-alive",
  "proxy-authenticate",
  "proxy-authorization",
  "te",
  "trailer",
  "transfer-encoding",
  "upgrade",
];

function endJson(response, status, detail) {
  response.writeHead(status, {
    "Content-Type": "application/problem+json; charset=utf-8",
  });
  response.end(JSON.stringify({ status, detail }));
}

function withoutHopHeaders(headers) {
  const result = { ...headers };
  for (const name of [
    ...hopHeaders,
    ...(headers.connection || "").split(",").map((s) => s.trim()),
  ]) {
    delete result[name.toLowerCase()];
  }
  return result;
}

// A API usa a mesma origem do navegador. Não há regras financeiras neste servidor.
export function createFrontendServer({
  backendUrl = "http://127.0.0.1:8080",
} = {}) {
  const backend = new URL(backendUrl);
  if (
    !["http:", "https:"].includes(backend.protocol) ||
    backend.username ||
    backend.password ||
    backend.pathname !== "/" ||
    backend.search ||
    backend.hash
  ) {
    throw new Error(
      "BACKEND_URL deve ser uma origem HTTP, por exemplo http://127.0.0.1:8080.",
    );
  }
  return http.createServer(async (request, response) => {
    let pathname;
    try {
      pathname = decodeURIComponent(request.url.split("?")[0]);
    } catch {
      return endJson(response, 400, "Endereço inválido.");
    }

    if (
      /^\/(api(?:\/|$)|actuator(?:\/|$)|v3\/api-docs(?:\/|$)|swagger-ui(?:[/.]|$))/.test(
        pathname,
      )
    ) {
      const transport = backend.protocol === "https:" ? https : http;
      // Preservar Host permite que Swagger use a origem pública e que Origin seja validado.
      const proxy = transport.request(
        {
          protocol: backend.protocol,
          hostname: backend.hostname,
          port: backend.port,
          method: request.method,
          path: request.url,
          headers: withoutHopHeaders(request.headers),
        },
        (upstream) => {
          response.writeHead(
            upstream.statusCode,
            withoutHopHeaders(upstream.headers),
          );
          upstream.pipe(response);
          upstream.on("error", () => response.destroy());
        },
      );
      proxy.setTimeout(30000, () =>
        proxy.destroy(new Error("Tempo de resposta excedido.")),
      );
      proxy.on("error", () => {
        if (!response.headersSent)
          endJson(
            response,
            503,
            "Backend indisponível. Inicie a API Java e confira sua porta.",
          );
        else response.destroy();
      });
      request.on("aborted", () => proxy.destroy());
      response.on("close", () => {
        if (!response.writableEnded) proxy.destroy();
      });
      request.pipe(proxy);
      return;
    }
    if (!["GET", "HEAD"].includes(request.method)) {
      response.setHeader("Allow", "GET, HEAD");
      return endJson(
        response,
        405,
        "Método não permitido para arquivos da interface.",
      );
    }
    if (pathname === "/health") {
      response.writeHead(200, {
        "Content-Type": "application/json; charset=utf-8",
      });
      return response.end(
        request.method === "HEAD" ? undefined : '{"status":"UP"}',
      );
    }
    const relative = pathname === "/" ? "index.html" : pathname.slice(1);
    // Só a página e os diretórios públicos são expostos. Código do servidor e .env ficam privados.
    if (
      pathname.includes("\\") ||
      pathname.includes("\0") ||
      pathname.split("/").includes("..") ||
      pathname.split("/").some((part) => part.startsWith(".")) ||
      !(
        relative === "index.html" ||
        /^(src\/.*\.js|assets\/css\/.*\.css)$/.test(relative)
      )
    ) {
      return endJson(response, 404, "Arquivo não encontrado.");
    }
    try {
      const data = await readFile(path.join(root, relative));
      response.writeHead(200, {
        "Content-Type": `${mime[path.extname(relative)]}; charset=utf-8`,
        "Content-Length": data.length,
        "Cache-Control": "no-cache",
        "X-Content-Type-Options": "nosniff",
      });
      response.end(request.method === "HEAD" ? undefined : data);
    } catch (error) {
      endJson(
        response,
        error.code === "ENOENT" ? 404 : 500,
        "Arquivo não disponível.",
      );
    }
  });
}

if (
  process.argv[1] &&
  import.meta.url === pathToFileURL(path.resolve(process.argv[1])).href
) {
  const port = Number(process.env.FRONTEND_PORT || 5173);
  if (!Number.isInteger(port) || port < 1024 || port > 65535)
    throw new Error("FRONTEND_PORT inválida.");
  const server = createFrontendServer({ backendUrl: process.env.BACKEND_URL });
  server.on("error", (error) => {
    console.error(error.message);
    process.exitCode = 1;
  });
  server.listen(port, "127.0.0.1", () =>
    console.log(`Fluxo: http://127.0.0.1:${port}`),
  );
  for (const signal of ["SIGINT", "SIGTERM"])
    process.on(signal, () => server.close());
}
