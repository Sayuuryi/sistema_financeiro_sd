// Estado da navegação e dos filtros; regras financeiras pertencem ao backend.

export const state = {
  view: "overview",
  today: "",
  page: 0,
  master: "pessoas",
  filters: {},
  data: {},
  from: "",
  to: "",
};

export const titles = {
  overview: "Visão geral",
  payables: "Contas a pagar",
  receivables: "Contas a receber",
  forecasts: "Despesas previstas",
  masters: "Cadastros",
  reports: "Relatórios",
};
