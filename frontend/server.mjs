import http from "node:http";
import { readFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";

// Serve o build (dist) fora do Docker; rode "npm run build" antes
const root = path.join(path.dirname(fileURLToPath(import.meta.url)), "dist");
const mime = {
  ".html": "text/html",
  ".js": "text/javascript",
  ".css": "text/css",
  ".svg": "image/svg+xml",
};
const bloqueadas = /^\/(api|interno|servicos|actuator|swagger-ui|v3)(\/|\.|$)/;

function json(response, status, detail) {
  response.writeHead(status, { "Content-Type": "application/problem+json; charset=utf-8" });
  response.end(JSON.stringify({ status, detail }));
}

async function enviar(request, response, relative) {
  const data = await readFile(path.join(root, relative));
  response.writeHead(200, {
    "Content-Type": `${mime[path.extname(relative)]}; charset=utf-8`,
    "Content-Length": data.length,
    "Cache-Control": "no-cache",
    "X-Content-Type-Options": "nosniff",
  });
  response.end(request.method === "HEAD" ? undefined : data);
}

export function createFrontendServer() {
  return http.createServer(async (request, response) => {
    let pathname;
    try {
      pathname = decodeURIComponent(request.url.split("?")[0]);
    } catch {
      return json(response, 400, "Endereço inválido.");
    }
    if (!["GET", "HEAD"].includes(request.method)) {
      response.setHeader("Allow", "GET, HEAD");
      return json(response, 405, "Método não permitido para arquivos da interface.");
    }
    if (pathname === "/health") {
      response.writeHead(200, { "Content-Type": "application/json; charset=utf-8" });
      return response.end(request.method === "HEAD" ? undefined : '{"status":"UP"}');
    }
    if (
      pathname.includes("\\") ||
      pathname.includes("\0") ||
      pathname.split("/").includes("..") ||
      pathname.split("/").some((p) => p.startsWith(".")) ||
      bloqueadas.test(pathname)
    ) {
      return json(response, 404, "Arquivo não encontrado.");
    }
    const arquivo = /^\/(static\/.*\.(js|css)|assets\/images\/.*\.svg)$/.test(pathname);
    try {
      // Rotas do React sempre recebem o index.html
      await enviar(request, response, arquivo ? pathname.slice(1) : "index.html");
    } catch (error) {
      json(response, error.code === "ENOENT" ? 404 : 500, "Arquivo não disponível.");
    }
  });
}

if (process.argv[1] && import.meta.url === pathToFileURL(path.resolve(process.argv[1])).href) {
  const port = Number(process.env.FRONTEND_PORT || 8080),
    host = process.env.FRONTEND_HOST || "127.0.0.1";
  if (!Number.isInteger(port) || port < 1024 || port > 65535)
    throw new Error("FRONTEND_PORT inválida.");
  const server = createFrontendServer();
  server.on("error", (error) => {
    console.error(error.message);
    process.exitCode = 1;
  });
  server.listen(port, host, () => console.log(`Fluxo: http://${host}:${port}`));
  for (const signal of ["SIGINT", "SIGTERM"]) process.on(signal, () => server.close());
}
