// Comunicação JSON com o backend pelo proxy do frontend.
import { state } from "./estado.js";

export async function api(path, options = {}) {
  const headers = { ...options.headers };
  if (options.body) headers["Content-Type"] = "application/json";
  const response = await fetch(path, {
    ...options,
    headers,
    body: options.body ? JSON.stringify(options.body) : undefined,
  });
  if (response.status === 204) return null;
  const data = await response.json();
  if (!response.ok) {
    let msg = data.detail || "Não foi possível concluir a operação.";
    if (data.violacoes)
      msg +=
        " " +
        Object.entries(data.violacoes)
          .map(([k, v]) => k + ": " + v)
          .join("; ");
    throw new Error(msg);
  }
  return data;
}

export const params = (obj) =>
  new URLSearchParams(
    Object.entries(obj).filter(
      ([, v]) => v !== "" && v !== null && v !== undefined,
    ),
  ).toString();

export async function loadMasters() {
  const routes = [
    "pessoas",
    "categorias",
    "bancos",
    "agencias",
    "contas-bancarias",
  ];
  const results = await Promise.all(routes.map((r) => api("/api/" + r)));
  routes.forEach((r, i) => (state.data[r] = results[i]));
}
