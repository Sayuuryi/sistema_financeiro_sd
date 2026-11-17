import { useQueries } from "@tanstack/react-query";
import { api } from "./api.js";

export const ROTAS_CADASTROS = ["pessoas", "categorias", "bancos", "agencias", "contas-bancarias"];
export const ROTAS_REFERENCIA = ["pessoas", "categorias", "contas-bancarias"];

const caminho = (rota) =>
  "/api/" + rota + (rota === "contas-bancarias" ? "?incluirSaldo=false" : "");

// Cadastros usados em filtros e formulários
export function useCadastros(rotas = ROTAS_CADASTROS, { timeout } = {}) {
  const consultas = useQueries({
    queries: rotas.map((rota) => ({
      queryKey: ["cadastros", rota],
      queryFn: () =>
        api(caminho(rota), timeout ? { signal: AbortSignal.timeout(timeout) } : undefined),
    })),
  });
  const dados = {};
  rotas.forEach((rota, i) => (dados[rota] = consultas[i].data ?? []));
  return {
    dados,
    consultas,
    carregando: consultas.some((c) => c.isPending || (c.isError && c.isFetching)),
    disponiveis: consultas.every((c) => !c.isError),
    erro: consultas.find((c) => c.isError)?.error ?? null,
    recarregar: () => consultas.forEach((c) => c.refetch()),
  };
}

export function estadoDe(...consultas) {
  const comErro = consultas.find((c) => c.isError);
  return {
    carregando: consultas.some((c) => c.isPending || (c.isError && c.isFetching)),
    erro: comErro?.error ?? null,
    recarregar: () => consultas.forEach((c) => c.refetch()),
  };
}
