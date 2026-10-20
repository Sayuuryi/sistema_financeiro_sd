export const escapeHtml = (v) =>
  String(v ?? "").replace(
    /[&<>"']/g,
    (c) =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[
        c
      ],
  );

export const money = (v) =>
  new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(
    Number(v ?? 0),
  );

export const date = (v) =>
  v ? new Date(v + "T12:00:00").toLocaleDateString("pt-BR") : "—";

export const labels = {
  ATIVO: "Ativo",
  ESTORNADA: "Estornada",
  ABERTA: "Em aberto",
  PARCIAL: "Parcial",
  VENCIDA: "Vencida",
  PAGA: "Paga",
  RECEBIDA: "Recebida",
  CANCELADA: "Cancelada",
  PREVISTA: "Prevista",
  CONVERTIDA: "Convertida",
  INATIVA: "Inativa",
  CLIENTE: "Cliente",
  FORNECEDOR: "Fornecedor",
  AMBOS: "Cliente e fornecedor",
  RECEITA: "Receita",
  DESPESA: "Despesa",
  DINHEIRO: "Dinheiro",
  PIX: "PIX",
  TRANSFERENCIA: "Transferência",
  BOLETO: "Boleto",
  CARTAO: "Cartão",
  CHEQUE: "Cheque",
  DUPLICATA: "Duplicata",
  NOTA_PROMISSORIA: "Nota promissória",
  OUTROS: "Outros",
};

export const badge = (s) =>
  `<span class="badge ${escapeHtml(s.toLowerCase())}">${escapeHtml(labels[s] || s)}</span>`;
