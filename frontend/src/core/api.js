import { services } from "../config.js";
import { tokenAtual } from "./sessao.js";

const resources = new Map([
  ...["pessoas", "categorias", "bancos", "agencias", "contas-bancarias"].map((r) => [
    r,
    "cadastros",
  ]),
  ["contas-pagar", "pagar"],
  ["contas-receber", "receber"],
  ["relatorios", "relatorios"],
  ["visao-geral", "visaoGeral"],
  ["despesas-previstas", "planejamento"],
  ["usuarios", "usuarios"],
]);

export function apiUrl(path) {
  const resource = path.match(/^\/api\/([^/?#]+)(?:\/|\?|$)/)?.[1];
  const service = resources.get(resource);
  if (!service) {
    throw new Error("Rota de API desconhecida: " + path);
  }
  return services[service] + path.replace(/^\/api\/contas-(pagar|receber)/, "/api/contas");
}

export const SESSAO_EXPIRADA = "fluxo:sessao-expirada";

function comToken(headers = {}) {
  const token = tokenAtual();
  if (token) {
    headers.Authorization = "Bearer " + token;
  }
  return headers;
}

// Qualquer serviço que recusar o token encerra a sessão na interface
function avisarSeExpirou(status) {
  if (status === 401 && tokenAtual()) {
    window.dispatchEvent(new Event(SESSAO_EXPIRADA));
  }
}

function erroHttp(mensagem, status) {
  const erro = new Error(mensagem);
  erro.status = status;
  return erro;
}

export async function api(path, options = {}) {
  const headers = comToken({ ...options.headers });
  if (options.body) {
    headers["Content-Type"] = "application/json";
  }
  const url = apiUrl(path);
  let response;
  try {
    response = await fetch(url, {
      ...options,
      headers,
      body: options.body ? JSON.stringify(options.body) : undefined,
      signal: options.signal || AbortSignal.timeout(6500),
    });
  } catch {
    throw erroHttp(
      "Não foi possível carregar esta área agora. As outras continuam disponíveis no menu.",
      0,
    );
  }
  if (response.status === 204) {
    return null;
  }
  let data;
  try {
    data = await response.json();
  } catch {
    throw new Error("A API retornou uma resposta inválida (HTTP " + response.status + ").");
  }
  if (!response.ok) {
    avisarSeExpirou(response.status);
    if (response.status === 503)
      throw erroHttp(
        "Não foi possível concluir agora porque uma área necessária está indisponível. Tente novamente em instantes.",
        503,
      );
    let msg = data.detail || "Não foi possível concluir a operação.";
    if (data.violacoes) {
      msg +=
        " " +
        Object.entries(data.violacoes)
          .map(([k, v]) => k + ": " + v)
          .join("; ");
    }
    throw erroHttp(msg, response.status);
  }
  return data;
}

export const params = (obj) =>
  new URLSearchParams(
    Object.entries(obj).filter(([, v]) => v !== "" && v !== null && v !== undefined),
  ).toString();

export async function download(path) {
  let response;
  try {
    response = await fetch(apiUrl(path), {
      headers: comToken(),
      signal: AbortSignal.timeout(8000),
    });
  } catch {
    throw new Error("Não foi possível exportar agora. Tente novamente em instantes.");
  }
  if (!response.ok) {
    avisarSeExpirou(response.status);
    let detail = "Não foi possível exportar os dados.";
    try {
      detail = (await response.json()).detail || detail;
    } catch {}
    throw new Error(detail);
  }
  const file = await response.blob();
  const objectUrl = URL.createObjectURL(file);
  const link = document.createElement("a");
  link.href = objectUrl;
  link.download =
    response.headers.get("Content-Disposition")?.match(/filename="?([^";]+)/)?.[1] ||
    "relatorio.csv";
  link.click();
  setTimeout(() => URL.revokeObjectURL(objectUrl), 1000);
}

export const accountPath = (tipo) =>
  tipo === "RECEBER" ? "/api/contas-receber" : "/api/contas-pagar";
