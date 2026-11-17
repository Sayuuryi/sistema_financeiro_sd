import { useState } from "react";
import { ChartColumn, Eye, EyeOff, Lock, Mail, QrCode, ReceiptText, UserRound } from "lucide-react";
import { useSessao } from "../contexto/SessaoContext.jsx";
import { useEnvio } from "../componentes/Formularios.jsx";

function Campo({ icone, label, name, type = "text", ...attrs }) {
  return (
    <label className="field auth-field">
      {label}
      <span className="auth-input">
        {icone}
        <input name={name} type={type} {...attrs} />
      </span>
    </label>
  );
}

function CampoSenha({ label = "Senha", name = "senha", ...attrs }) {
  const [visivel, setVisivel] = useState(false);
  return (
    <label className="field auth-field">
      {label}
      <span className="auth-input">
        <Lock size={15} aria-hidden="true" />
        <input name={name} type={visivel ? "text" : "password"} required {...attrs} />
        <button
          type="button"
          className="auth-eye"
          aria-label={visivel ? "Ocultar senha" : "Mostrar senha"}
          onClick={() => setVisivel((v) => !v)}>
          {visivel ? <EyeOff size={15} aria-hidden="true" /> : <Eye size={15} aria-hidden="true" />}
        </button>
      </span>
    </label>
  );
}

function Entrar() {
  const { entrar } = useSessao();
  const { erro, enviando, enviar } = useEnvio((d) => entrar(d.email, d.senha));

  return (
    <form onSubmit={enviar} className="auth-form">
      <div className="form-error" hidden={!erro} role="alert">
        {erro}
      </div>
      <Campo
        icone={<Mail size={15} aria-hidden="true" />}
        label="E-mail"
        name="email"
        type="email"
        autoComplete="email"
        required
      />
      <CampoSenha autoComplete="current-password" />
      <button type="submit" className="button primary auth-submit" disabled={enviando}>
        {enviando ? "Entrando…" : "Entrar"}
      </button>
    </form>
  );
}

function Cadastrar() {
  const { cadastrar } = useSessao();
  const { erro, enviando, enviar } = useEnvio(async (d) => {
    if (d.senha !== d.confirmacao) throw new Error("As senhas não conferem.");
    await cadastrar({ nome: d.nome, email: d.email, senha: d.senha });
  });

  return (
    <form onSubmit={enviar} className="auth-form">
      <div className="form-error" hidden={!erro} role="alert">
        {erro}
      </div>
      <Campo
        icone={<UserRound size={15} aria-hidden="true" />}
        label="Nome"
        name="nome"
        autoComplete="name"
        required
        minLength={2}
        maxLength={120}
      />
      <Campo
        icone={<Mail size={15} aria-hidden="true" />}
        label="E-mail"
        name="email"
        type="email"
        autoComplete="email"
        required
        maxLength={160}
      />
      <CampoSenha autoComplete="new-password" minLength={8} maxLength={72} />
      <CampoSenha
        label="Confirmar senha"
        name="confirmacao"
        autoComplete="new-password"
        minLength={8}
        maxLength={72}
      />
      <small className="auth-hint">Mínimo de 8 caracteres.</small>
      <button type="submit" className="button primary auth-submit" disabled={enviando}>
        {enviando ? "Criando conta…" : "Criar conta"}
      </button>
    </form>
  );
}

export function Acesso() {
  const [aba, setAba] = useState("entrar");
  return (
    <div className="auth">
      <aside className="auth-brand">
        <div className="brand">
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
        </div>
        <img className="auth-art" src="/assets/images/acesso.svg" alt="" />
        <ul className="auth-features">
          <li>
            <ReceiptText size={16} aria-hidden="true" /> Contas a pagar e a receber
          </li>
          <li>
            <QrCode size={16} aria-hidden="true" /> Pix, Pix Automático, cartões e boletos
          </li>
          <li>
            <ChartColumn size={16} aria-hidden="true" /> Gráficos e relatórios do período
          </li>
        </ul>
      </aside>
      <main className="auth-main">
        <section className="auth-card">
          <span className="eyebrow">{aba === "entrar" ? "ACESSO" : "NOVA CONTA"}</span>
          <h1>{aba === "entrar" ? "Entrar no Fluxo" : "Criar sua conta"}</h1>
          <div className="tabs auth-tabs" role="tablist">
            <button
              role="tab"
              aria-selected={aba === "entrar"}
              className={"tab" + (aba === "entrar" ? " active" : "")}
              onClick={() => setAba("entrar")}>
              Entrar
            </button>
            <button
              role="tab"
              aria-selected={aba === "cadastrar"}
              className={"tab" + (aba === "cadastrar" ? " active" : "")}
              onClick={() => setAba("cadastrar")}>
              Criar conta
            </button>
          </div>
          {aba === "entrar" ? <Entrar /> : <Cadastrar />}
        </section>
      </main>
    </div>
  );
}
