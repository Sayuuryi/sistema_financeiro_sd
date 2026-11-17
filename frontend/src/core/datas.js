// Data de hoje no fuso de São Paulo
export function hojeSaoPaulo(agora = new Date()) {
  return new Intl.DateTimeFormat("sv-SE", {
    timeZone: "America/Sao_Paulo",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).format(agora);
}

export function periodoDoMes(hoje) {
  const mes = hoje.slice(0, 7);
  const atual = new Date(hoje + "T12:00:00");
  const ultimoDia = new Date(atual.getFullYear(), atual.getMonth() + 1, 0).getDate();
  return { from: mes + "-01", to: mes + "-" + String(ultimoDia).padStart(2, "0") };
}
