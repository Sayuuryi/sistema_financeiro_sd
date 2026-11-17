const CHAVE = "fluxo.sessao";

export function lerSessao() {
  try {
    const sessao = JSON.parse(localStorage.getItem(CHAVE));
    return sessao?.token && sessao?.usuario ? sessao : null;
  } catch {
    return null;
  }
}

export function gravarSessao(sessao) {
  try {
    if (sessao) localStorage.setItem(CHAVE, JSON.stringify(sessao));
    else localStorage.removeItem(CHAVE);
  } catch {}
}

export const tokenAtual = () => lerSessao()?.token ?? null;

export function iniciais(nome) {
  const partes = String(nome ?? "")
    .trim()
    .split(/\s+/)
    .filter(Boolean);
  if (!partes.length) return "?";
  const primeira = partes[0][0];
  const ultima = partes.length > 1 ? partes.at(-1)[0] : (partes[0][1] ?? "");
  return (primeira + ultima).toUpperCase();
}

export function saudacao(hora) {
  if (hora >= 5 && hora < 12) return "Bom dia";
  if (hora >= 12 && hora < 18) return "Boa tarde";
  return "Boa noite";
}

export const primeiroNome = (nome) =>
  String(nome ?? "")
    .trim()
    .split(/\s+/)[0] || "";
