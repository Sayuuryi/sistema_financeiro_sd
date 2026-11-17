const brl = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });
const brlCompacto = new Intl.NumberFormat("pt-BR", {
  style: "currency",
  currency: "BRL",
  notation: "compact",
  maximumFractionDigits: 1,
});

export const money = (v) => (v == null ? "—" : brl.format(Number(v ?? 0)));

const brlInteiro = new Intl.NumberFormat("pt-BR", {
  style: "currency",
  currency: "BRL",
  maximumFractionDigits: 0,
});

export const moneyCompact = (v) => {
  const n = Number(v ?? 0);
  return Math.abs(n) >= 1000000 ? brlCompacto.format(n) : brlInteiro.format(n);
};

export const date = (v) => (v ? new Date(v + "T12:00:00").toLocaleDateString("pt-BR") : "—");

export const shortDate = (v) =>
  v
    ? new Date(v + "T12:00:00").toLocaleDateString("pt-BR", { day: "2-digit", month: "2-digit" })
    : "—";

export const percent = (v) =>
  v == null || Number.isNaN(v)
    ? "—"
    : new Intl.NumberFormat("pt-BR", { style: "percent", maximumFractionDigits: 1 }).format(v);

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
  PIX: "Pix",
  PIX_AUTOMATICO: "Pix Automático",
  TRANSFERENCIA: "Transferência (TED)",
  BOLETO: "Boleto",
  CARTAO: "Cartão",
  CARTAO_CREDITO: "Cartão de crédito",
  CARTAO_DEBITO: "Cartão de débito",
  CARTEIRA_DIGITAL: "Carteira digital",
  DEBITO_AUTOMATICO: "Débito automático",
  CHEQUE: "Cheque",
  DUPLICATA: "Duplicata",
  NOTA_PROMISSORIA: "Nota promissória",
  OUTROS: "Outros",
};

// Ordem do formulário de baixa; CARTAO fica só para o histórico antigo
export const FORMAS_PAGAMENTO = [
  "PIX",
  "PIX_AUTOMATICO",
  "CARTAO_CREDITO",
  "CARTAO_DEBITO",
  "CARTEIRA_DIGITAL",
  "BOLETO",
  "TRANSFERENCIA",
  "DEBITO_AUTOMATICO",
  "DINHEIRO",
  "CHEQUE",
];

export const enums = (keys) => keys.map((k) => [k, labels[k] || k]);
