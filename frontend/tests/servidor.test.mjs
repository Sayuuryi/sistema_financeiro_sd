import { test } from "node:test";
import assert from "node:assert/strict";
import http from "node:http";
import { once } from "node:events";
import { createFrontendServer } from "../server.mjs";

async function listen(server) {
  server.listen(0, "127.0.0.1");
  await once(server, "listening");
  return `http://127.0.0.1:${server.address().port}`;
}
async function fixture(t, handler) {
  const backend = http.createServer(handler || ((req, res) => res.end("ok")));
  const backendUrl = await listen(backend);
  const frontend = createFrontendServer({ backendUrl });
  const url = await listen(frontend);
  t.after(async () => {
    await Promise.all([
      new Promise((r) => frontend.close(r)),
      new Promise((r) => backend.close(r)),
    ]);
  });
  return { url, backend };
}

test("entrega página, CSS e módulos com os tipos corretos", async (t) => {
  const { url } = await fixture(t);
  for (const [file, type, pattern] of [
    ["/", "text/html", /type="module"/],
    ["/assets/css/app.css", "text/css", /sidebar/],
    ["/src/main.js", "text/javascript", /import/],
  ]) {
    const res = await fetch(url + file);
    assert.equal(res.status, 200);
    assert.match(res.headers.get("content-type"), new RegExp(type));
    assert.match(await res.text(), pattern);
  }
});
test("não expõe configuração, código do servidor ou travessia de diretórios", async (t) => {
  const { url } = await fixture(t);
  for (const file of [
    "/.env",
    "/server.mjs",
    "/package.json",
    "/src/%2e%2e%2fserver.mjs",
    "/src/%5c..%5cserver.mjs",
    "/assets/css/ausente.css",
  ]) {
    assert.equal((await fetch(url + file)).status, 404, file);
  }
});
test("encaminha parâmetros, Host e Origin para a API", async (t) => {
  const { url } = await fixture(t, (req, res) => {
    res.setHeader("Content-Type", "application/json");
    res.end(
      JSON.stringify({
        path: req.url,
        host: req.headers.host,
        origin: req.headers.origin,
      }),
    );
  });
  const res = await fetch(url + "/api/contas?busca=Jo%C3%A3o&pagina=2", {
    headers: { Origin: url },
  });
  assert.deepEqual(await res.json(), {
    path: "/api/contas?busca=Jo%C3%A3o&pagina=2",
    host: new URL(url).host,
    origin: url,
  });
});
test("preserva corpo, chave de idempotência e status em uma baixa", async (t) => {
  const { url } = await fixture(t, async (req, res) => {
    let body = "";
    for await (const part of req) body += part;
    res.writeHead(201, { "Content-Type": "application/json" });
    res.end(
      JSON.stringify({
        body: JSON.parse(body),
        key: req.headers["idempotency-key"],
        method: req.method,
      }),
    );
  });
  const body = { principal: "12.34", observacoes: "Pagamento ação" };
  const res = await fetch(url + "/api/contas/1/baixas", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Idempotency-Key": "chave-teste",
    },
    body: JSON.stringify(body),
  });
  assert.equal(res.status, 201);
  assert.deepEqual(await res.json(), {
    body,
    key: "chave-teste",
    method: "POST",
  });
});
test("preserva CSV, BOM e nome do download", async (t) => {
  const csv = Buffer.from("\ufeffTítulo;Valor\r\nAção;12,34\r\n");
  const { url } = await fixture(t, (req, res) => {
    res.writeHead(200, {
      "Content-Type": "text/csv; charset=utf-8",
      "Content-Disposition": 'attachment; filename="contas.csv"',
    });
    res.end(csv);
  });
  const res = await fetch(url + "/api/relatorios/contas.csv");
  assert.match(res.headers.get("content-disposition"), /contas.csv/);
  assert.deepEqual(Buffer.from(await res.arrayBuffer()), csv);
});
test("preserva erros ProblemDetail e respostas sem conteúdo", async (t) => {
  const { url } = await fixture(t, (req, res) => {
    if (req.method === "DELETE") {
      res.writeHead(204);
      return res.end();
    }
    res.writeHead(409, { "Content-Type": "application/problem+json" });
    res.end('{"detail":"Título protegido"}');
  });
  const res = await fetch(url + "/api/contas/1");
  assert.equal(res.status, 409);
  assert.equal((await res.json()).detail, "Título protegido");
  const empty = await fetch(url + "/api/contas/1", { method: "DELETE" });
  assert.equal(empty.status, 204);
  assert.equal(await empty.text(), "");
});
test("encaminha Swagger, contrato OpenAPI e saúde do backend", async (t) => {
  const { url } = await fixture(t, (req, res) => {
    res.writeHead(200);
    res.end(req.url);
  });
  for (const file of [
    "/swagger-ui.html",
    "/swagger-ui/index.html",
    "/v3/api-docs/swagger-config",
    "/actuator/health",
  ]) {
    assert.equal(await (await fetch(url + file)).text(), file);
  }
});
test("informa indisponibilidade do backend sem derrubar os arquivos estáticos", async (t) => {
  const { url, backend } = await fixture(t);
  await new Promise((r) => backend.close(r));
  const res = await fetch(url + "/api/sistema");
  assert.equal(res.status, 503);
  assert.match((await res.json()).detail, /Backend indisponível/);
  assert.equal((await fetch(url + "/")).status, 200);
});
test("suporta HEAD e saúde própria, restringindo escrita em arquivos", async (t) => {
  const { url } = await fixture(t);
  const head = await fetch(url, { method: "HEAD" });
  assert.equal(head.status, 200);
  assert.equal(await head.text(), "");
  assert.deepEqual(await (await fetch(url + "/health")).json(), {
    status: "UP",
  });
  const res = await fetch(url + "/src/main.js", { method: "POST", body: "x" });
  assert.equal(res.status, 405);
  assert.equal(res.headers.get("allow"), "GET, HEAD");
});
