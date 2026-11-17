import { Suspense, lazy, useEffect, useRef, useState } from "react";
import { Link, Navigate, Route, Routes, useLocation, useNavigate } from "react-router-dom";
import {
  Building2,
  CalendarDays,
  ChevronDown,
  LogOut,
  PenLine,
  Plus,
  UserRound,
} from "lucide-react";
import { useApp } from "./contexto/AppContext.jsx";
import { useSessao } from "./contexto/SessaoContext.jsx";
import { primeiroNome, saudacao } from "./core/sessao.js";
import { Modal, Toast } from "./componentes/Dialogo.jsx";
import { Avatar } from "./componentes/Avatar.jsx";
import { EmpresaForm } from "./componentes/EmpresaForm.jsx";
import { Loading } from "./componentes/Layout.jsx";
import { VisaoGeral } from "./funcionalidades/VisaoGeral.jsx";
import { Contas } from "./funcionalidades/Contas.jsx";
import { Previsoes } from "./funcionalidades/Previsoes.jsx";
import { Cadastros } from "./funcionalidades/Cadastros.jsx";
import { Relatorios } from "./funcionalidades/Relatorios.jsx";
import { Acesso } from "./funcionalidades/Acesso.jsx";
import { Perfil } from "./funcionalidades/Perfil.jsx";

// Carrega os gráficos só ao abrir a dashboard
const Dashboard = lazy(() =>
  import("./funcionalidades/Dashboard.jsx").then((m) => ({ default: m.Dashboard })),
);

// Itens do menu lateral
export const ROTAS = [
  { path: "/", titulo: "Visão geral", icone: "overview", element: <VisaoGeral /> },
  { path: "/dashboard", titulo: "Dashboard", icone: "dashboard", element: <Dashboard /> },
  {
    path: "/contas-pagar",
    titulo: "Contas a pagar",
    icone: "payables",
    element: <Contas tipo="PAGAR" key="PAGAR" />,
  },
  {
    path: "/contas-receber",
    titulo: "Contas a receber",
    icone: "receivables",
    element: <Contas tipo="RECEBER" key="RECEBER" />,
  },
  {
    path: "/despesas-previstas",
    titulo: "Despesas previstas",
    icone: "forecasts",
    element: <Previsoes />,
  },
  { path: "/cadastros", titulo: "Cadastros", icone: "masters", element: <Cadastros /> },
  { path: "/relatorios", titulo: "Relatórios", icone: "reports", element: <Relatorios /> },
];

// Fora do menu principal; aberto pelo menu do usuário
const PERFIL = { path: "/perfil", titulo: "Meu perfil", element: <Perfil /> };

const horaSaoPaulo = () =>
  Number(
    new Date().toLocaleString("en-US", {
      timeZone: "America/Sao_Paulo",
      hour: "numeric",
      hourCycle: "h23",
    }),
  );

const dataPorExtenso = (iso) => {
  const texto = new Date(iso + "T12:00:00").toLocaleDateString("pt-BR", {
    weekday: "long",
    day: "numeric",
    month: "long",
  });
  return texto.charAt(0).toUpperCase() + texto.slice(1);
};

function Sidebar({ atual }) {
  const navigate = useNavigate();
  const { usuario } = useSessao();
  const { openModal } = useApp();
  return (
    <aside className="sidebar">
      <Link className="brand" to="/" aria-label="Fluxo início">
        <img
          className="brand-icon"
          src="/assets/images/fluxo-mark.svg"
          alt=""
          width="42"
          height="42"
        />
        <span>
          fluxo
          <span className="brand-dot">.</span>
          <small>GESTÃO FINANCEIRA</small>
        </span>
      </Link>
      <button
        className="workspace workspace-card"
        onClick={() =>
          openModal(usuario.empresa ? "Editar empresa" : "Adicionar empresa", <EmpresaForm />)
        }
        aria-label={usuario.empresa ? "Editar empresa " + usuario.empresa : "Adicionar empresa"}>
        <span className="workspace-card-icon">
          <Building2 size={17} aria-hidden="true" />
        </span>
        <span className="workspace-card-text">
          <small>Empresa</small>
          {usuario.empresa || "Adicionar empresa"}
        </span>
        {usuario.empresa ? (
          <PenLine size={13} aria-hidden="true" className="workspace-card-edit" />
        ) : (
          <Plus size={15} aria-hidden="true" className="workspace-card-edit" />
        )}
      </button>
      <div className="nav-label">MENU</div>
      <nav aria-label="Menu principal">
        {ROTAS.map((rota) => (
          <button
            key={rota.path}
            className={rota === atual ? "active" : ""}
            aria-current={rota === atual ? "page" : undefined}
            onClick={() => navigate(rota.path)}>
            <svg className="nav-icon" aria-hidden="true">
              <use href={"/assets/images/icons.svg#" + rota.icone}></use>
            </svg>
            {rota.titulo}
          </button>
        ))}
      </nav>
      <div className="sidebar-bottom">
        <div className="sidebar-signature">
          <img src="/assets/images/fluxo-mark.svg" alt="" width="18" height="18" />
          <span>
            <b>
              fluxo<span className="brand-dot">.</span>
            </b>{" "}
            Gestão financeira
          </span>
        </div>
      </div>
    </aside>
  );
}

function MenuUsuario() {
  const { usuario, sair } = useSessao();
  const navigate = useNavigate();
  const [aberto, setAberto] = useState(false);
  const ref = useRef(null);

  // Fecha ao clicar fora ou apertar Esc
  useEffect(() => {
    if (!aberto) return;
    const fora = (e) => ref.current && !ref.current.contains(e.target) && setAberto(false);
    const esc = (e) => e.key === "Escape" && setAberto(false);
    document.addEventListener("mousedown", fora);
    document.addEventListener("keydown", esc);
    return () => {
      document.removeEventListener("mousedown", fora);
      document.removeEventListener("keydown", esc);
    };
  }, [aberto]);

  function ir() {
    setAberto(false);
    navigate(PERFIL.path);
  }

  return (
    <div className="user-menu" ref={ref}>
      <button
        className="user-menu-button"
        aria-haspopup="menu"
        aria-expanded={aberto}
        aria-label="Menu do usuário"
        onClick={() => setAberto((v) => !v)}>
        <Avatar usuario={usuario} tamanho={34} />
        <span className="user-menu-name">
          {usuario.nome}
          <small>{usuario.cargo || usuario.email}</small>
        </span>
        <ChevronDown size={15} aria-hidden="true" />
      </button>
      {aberto ? (
        <div className="user-menu-list" role="menu">
          <div className="user-menu-head">
            <Avatar usuario={usuario} tamanho={40} />
            <span>
              <b>{usuario.nome}</b>
              <small>{usuario.email}</small>
            </span>
          </div>
          <button role="menuitem" onClick={ir}>
            <UserRound size={15} aria-hidden="true" /> Meu perfil
          </button>
          <button role="menuitem" className="danger" onClick={sair}>
            <LogOut size={15} aria-hidden="true" /> Sair
          </button>
        </div>
      ) : null}
    </div>
  );
}

export default function App() {
  const { usuario } = useSessao();
  return usuario ? <Aplicacao /> : <Acesso />;
}

function Aplicacao() {
  const { today } = useApp();
  const { usuario } = useSessao();
  const { pathname } = useLocation();
  const atual = [...ROTAS, PERFIL].find((rota) => rota.path === pathname) ?? ROTAS[0];

  return (
    <>
      <Sidebar atual={atual} />
      <div className="main-shell">
        <header className="topbar">
          <div className="topbar-greeting">
            <strong>
              {saudacao(horaSaoPaulo())}, {primeiroNome(usuario.nome)}
            </strong>
            <time dateTime={today}>
              <CalendarDays size={13} aria-hidden="true" /> {dataPorExtenso(today)}
            </time>
          </div>
          <div className="topbar-right">
            <MenuUsuario />
          </div>
        </header>
        <main id="main" tabIndex={-1} aria-live="polite">
          <Suspense fallback={<Loading />}>
            <Routes>
              {[...ROTAS, PERFIL].map((rota) => (
                <Route key={rota.path} path={rota.path} element={rota.element} />
              ))}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </Suspense>
        </main>
        <footer>Fluxo · Gestão financeira</footer>
      </div>
      <Toast />
      <Modal />
    </>
  );
}
