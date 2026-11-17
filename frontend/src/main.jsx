import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { AppProvider } from "./contexto/AppContext.jsx";
import { SessaoProvider } from "./contexto/SessaoContext.jsx";
import App from "./App.jsx";
import "./estilos/app.css";
import "./estilos/dashboard.css";
import "./estilos/acabamento.css";
import "./estilos/acesso.css";
import "./estilos/perfil.css";

// Sem retry: serviço offline aparece na hora
const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: false, refetchOnWindowFocus: false, staleTime: 0 },
  },
});

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <SessaoProvider>
          <AppProvider>
            <App />
          </AppProvider>
        </SessaoProvider>
      </BrowserRouter>
    </QueryClientProvider>
  </StrictMode>,
);
