import { useState } from "react";
import {
  Briefcase,
  Building2,
  ChartColumn,
  Eye,
  EyeOff,
  Lock,
  Mail,
  Phone,
  QrCode,
  ReceiptText,
  UserRound,
} from "lucide-react";
import { useSessao } from "../contexto/SessaoContext.jsx";
import { api } from "../core/api.js";
import { primeiroNome } from "../core/sessao.js";
import { ErroFormulario, useEnvio } from "../componentes/Formularios.jsx";

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
      <Campo
        icone={<Mail size={15} aria-hidden="true" />}
        label="E-mail"
        name="email"
        type="email"
        autoComplete="email"
        required
      />
      <CampoSenha autoComplete="current-password" />
      <ErroFormulario erro={erro} />
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
      <ErroFormulario erro={erro} />
      <button type="submit" className="button primary auth-submit" disabled={enviando}>
        {enviando ? "Criando conta…" : "Continuar"}
      </button>
    </form>
  );
}

function Etapas({ atual }) {
  return (
    <ol className="auth-steps" aria-label={"Passo " + atual + " de 2"}>
      {["Sua conta", "Seu perfil"].map((nome, i) => (
        <li
          key={nome}
          className={i + 1 === atual ? "atual" : i + 1 < atual ? "feito" : ""}
          aria-current={i + 1 === atual ? "step" : undefined}>
          <span>{i + 1}</span>
          {nome}
        </li>
      ))}
    </ol>
  );
}

// Marca à esquerda e cartão à direita, igual no login e nas boas-vindas
function MolduraAcesso({ children }) {
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
        <section className="auth-card">{children}</section>
      </main>
    </div>
  );
}

export function Acesso() {
  const [aba, setAba] = useState("entrar");
  return (
    <MolduraAcesso>
      {aba === "entrar" ? <span className="eyebrow">ACESSO</span> : <Etapas atual={1} />}
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
    </MolduraAcesso>
  );
}

// Passo 2 do cadastro: dados de trabalho, todos opcionais
export function BoasVindas() {
  const { usuario, atualizarUsuario, concluirCadastro } = useSessao();
  const { erro, enviando, enviar } = useEnvio(async (d) => {
    const preenchidos = ["empresa", "cargo", "telefone"].some((c) => d[c].trim());
    if (preenchidos) {
      atualizarUsuario(
        await api("/api/usuarios/eu", {
          method: "PUT",
          body: {
            nome: usuario.nome,
            email: usuario.email,
            empresa: d.empresa.trim(),
            cargo: d.cargo.trim(),
            telefone: d.telefone.trim(),
            versao: usuario.versao,
          },
        }),
      );
    }
    concluirCadastro();
  });

  return (
    <MolduraAcesso>
      <Etapas atual={2} />
      <h1>Boas-vindas, {primeiroNome(usuario.nome)}!</h1>
      <p className="auth-lead">
        Esses dados aparecem no menu e no seu perfil. Todos são opcionais e podem ser preenchidos
        depois em <b>Meu perfil</b>.
      </p>
      <form onSubmit={enviar} className="auth-form">
        <Campo
          icone={<Building2 size={15} aria-hidden="true" />}
          label="Empresa"
          name="empresa"
          autoComplete="organization"
          maxLength={120}
          defaultValue={usuario.empresa ?? ""}
          autoFocus
        />
        <Campo
          icone={<Briefcase size={15} aria-hidden="true" />}
          label="Cargo"
          name="cargo"
          autoComplete="organization-title"
          maxLength={80}
          defaultValue={usuario.cargo ?? ""}
        />
        <Campo
          icone={<Phone size={15} aria-hidden="true" />}
          label="Telefone"
          name="telefone"
          type="tel"
          autoComplete="tel"
          maxLength={30}
          defaultValue={usuario.telefone ?? ""}
        />
        <ErroFormulario erro={erro} />
        <button type="submit" className="button primary auth-submit" disabled={enviando}>
          {enviando ? "Salvando…" : "Salvar e continuar"}
        </button>
        <button type="button" className="auth-skip" disabled={enviando} onClick={concluirCadastro}>
          Fazer depois
        </button>
      </form>
    </MolduraAcesso>
  );
}
