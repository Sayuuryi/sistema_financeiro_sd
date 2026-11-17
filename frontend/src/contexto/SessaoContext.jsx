import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { SESSAO_EXPIRADA, api } from "../core/api.js";
import { gravarSessao, lerSessao } from "../core/sessao.js";

const SessaoContext = createContext(null);

export function useSessao() {
  const contexto = useContext(SessaoContext);
  if (!contexto) {
    throw new Error("useSessao deve ser usado dentro de <SessaoProvider>.");
  }
  return contexto;
}

export function SessaoProvider({ children }) {
  const queryClient = useQueryClient();
  const [sessao, setSessao] = useState(() => lerSessao());

  const salvar = useCallback((nova) => {
    gravarSessao(nova);
    setSessao(nova);
  }, []);

  const token = sessao?.token;

  // Confirma a sessão guardada; com Usuários offline, segue com os dados locais
  useEffect(() => {
    if (!token) return;
    let ativo = true;
    api("/api/usuarios/eu")
      .then((usuario) => ativo && salvar({ ...lerSessao(), token, usuario }))
      .catch((erro) => ativo && erro.status === 401 && salvar(null));
    return () => {
      ativo = false;
    };
  }, [token, salvar]);

  useEffect(() => {
    const expirou = () => {
      salvar(null);
      queryClient.clear();
    };
    window.addEventListener(SESSAO_EXPIRADA, expirou);
    return () => window.removeEventListener(SESSAO_EXPIRADA, expirou);
  }, [salvar, queryClient]);

  const entrar = useCallback(
    async (email, senha) =>
      salvar(await api("/api/usuarios/login", { method: "POST", body: { email, senha } })),
    [salvar],
  );

  const cadastrar = useCallback(
    // Conta nova passa pelas boas-vindas antes de abrir o sistema
    async (dados) =>
      salvar({
        ...(await api("/api/usuarios", { method: "POST", body: dados })),
        completarPerfil: true,
      }),
    [salvar],
  );

  const sair = useCallback(async () => {
    try {
      await api("/api/usuarios/logout", { method: "POST" });
    } catch {}
    salvar(null);
    queryClient.clear();
  }, [salvar, queryClient]);

  const atualizarUsuario = useCallback((usuario) => salvar({ ...lerSessao(), usuario }), [salvar]);

  const concluirCadastro = useCallback(
    () => salvar({ ...lerSessao(), completarPerfil: false }),
    [salvar],
  );

  const value = useMemo(
    () => ({
      usuario: sessao?.usuario ?? null,
      completarPerfil: Boolean(sessao?.completarPerfil),
      entrar,
      cadastrar,
      sair,
      atualizarUsuario,
      concluirCadastro,
    }),
    [sessao, entrar, cadastrar, sair, atualizarUsuario, concluirCadastro],
  );

  return <SessaoContext.Provider value={value}>{children}</SessaoContext.Provider>;
}
