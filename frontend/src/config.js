// Toda chamada à API vai para o gateway, que encaminha ao serviço certo.
// Mudou a porta? Ajuste também o gateway no compose.yaml e FRONTEND_ORIGINS (CORS).
export const gateway = "http://localhost:18080";
