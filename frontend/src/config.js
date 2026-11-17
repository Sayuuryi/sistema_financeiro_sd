// Ao mudar as portas, ajuste também FRONTEND_ORIGINS no compose.yaml
export const services = Object.freeze({
  cadastros: "http://localhost:18081",
  pagar: "http://localhost:18082",
  planejamento: "http://localhost:18083",
  receber: "http://localhost:18084",
  relatorios: "http://localhost:18085",
  visaoGeral: "http://localhost:18086",
  usuarios: "http://localhost:18087",
});
