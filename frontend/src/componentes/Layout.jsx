import { useState } from "react";
import { CalendarRange, Check, RotateCw } from "lucide-react";
import { useApp } from "../contexto/AppContext.jsx";
import { labels, money } from "../core/formatacao.js";

export function ActionButton({ children, onClick, style = "", ...attrs }) {
  return (
    <button className={("button " + style).trim()} onClick={onClick} {...attrs}>
      {children}
    </button>
  );
}

export function PageHead({ title, subtitle, actions = null, image = "", eyebrow = "" }) {
  return (
    <div className={"page-head" + (image ? " illustrated" : "")}>
      <div>
        {image ? <img className="title-art" src={image} alt="" aria-hidden="true" /> : null}
        {eyebrow ? <span className="eyebrow">{eyebrow}</span> : null}
        <h1>{title}</h1>
        <p className="sub">{subtitle}</p>
      </div>
      <div className="actions">{actions}</div>
    </div>
  );
}

export function Period() {
  const { from, to, setPeriod } = useApp();
  const [versao, setVersao] = useState(0);

  function aplicar(evento) {
    evento.preventDefault();
    const f = new FormData(evento.target);
    setPeriod(f.get("from"), f.get("to"));
    setVersao((v) => v + 1);
  }

  return (
    <form id="period-form" className="period" onSubmit={aplicar} key={versao}>
      <CalendarRange size={14} aria-hidden="true" />
      <input type="date" aria-label="Início do período" name="from" defaultValue={from} required />
      <span>—</span>
      <input type="date" aria-label="Fim do período" name="to" defaultValue={to} required />
      <button className="button small" type="submit">
        <Check size={13} aria-hidden="true" /> Aplicar
      </button>
    </form>
  );
}

export function Metric({ label, value, text, note, icon, color = "" }) {
  return (
    <article className="card">
      <div className="metric-top">
        {label}
        <span className={("metric-icon " + color).trim()}>{icon}</span>
      </div>
      <div className="metric-value">{text ?? money(value)}</div>
      <div className="metric-note">{note}</div>
    </article>
  );
}

export function Warnings({ messages = [] }) {
  return (messages || []).map((message) => (
    <div className="notice warning" role="status" key={message}>
      {message}
    </div>
  ));
}

export function Badge({ s }) {
  return <span className={"badge " + String(s).toLowerCase()}>{labels[s] || s}</span>;
}

export function Loading({ children = "Consultando os dados…" }) {
  return <div className="loading">{children}</div>;
}

// Tela de serviço indisponível
export function Offline({ title, message, onRetry }) {
  return (
    <>
      <PageHead title={title} subtitle="" />
      <section className="offline-panel" role="status">
        <img src="/assets/images/service-offline.svg" alt="" />
        <h2>Esta área está indisponível</h2>
        <p>{message}</p>
        <button className="button primary" onClick={onRetry}>
          <RotateCw size={14} aria-hidden="true" /> Tentar novamente
        </button>
      </section>
    </>
  );
}

export const AVISO_CADASTROS_OFFLINE =
  "Cadastros indisponível no momento. A consulta continua; filtros e novos registros voltam quando ele retornar.";
