import { useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import {
  Briefcase,
  Building2,
  CalendarDays,
  Camera,
  Check,
  Clock,
  ImagePlus,
  Loader2,
  KeyRound,
  LogOut,
  Mail,
  Palette,
  Phone,
  Trash2,
  UserRound,
} from "lucide-react";
import { useApp } from "../contexto/AppContext.jsx";
import { useSessao } from "../contexto/SessaoContext.jsx";
import { api } from "../core/api.js";
import { FUNDOS, ehImagem, estiloCapa, prepararImagem } from "../core/imagens.js";
import { ITENS_PERFIL, progressoPerfil } from "../core/sessao.js";
import { useQuadroEstatico } from "../componentes/QuadroEstatico.js";
import { Avatar } from "../componentes/Avatar.jsx";
import { ErroFormulario, Field, useEnvio } from "../componentes/Formularios.jsx";
import { PageHead } from "../componentes/Layout.jsx";

const dataHora = (valor) =>
  valor
    ? new Date(valor).toLocaleString("pt-BR", {
        timeZone: "America/Sao_Paulo",
        dateStyle: "short",
        timeStyle: "short",
      })
    : "—";

function TituloPainel({ icone, titulo, descricao }) {
  return (
    <div className="panel-head">
      <div className="panel-title">
        <span className="panel-title-icon">{icone}</span>
        <div>
          <h2>{titulo}</h2>
          <p>{descricao}</p>
        </div>
      </div>
    </div>
  );
}

function DadosConta({ usuario }) {
  const { atualizarUsuario } = useSessao();
  const { toast } = useApp();
  const { erro, enviando, enviar } = useEnvio(async (d) => {
    const atualizado = await api("/api/usuarios/eu", {
      method: "PUT",
      body: { ...d, versao: usuario.versao },
    });
    atualizarUsuario(atualizado);
    toast("Perfil atualizado.");
  });

  return (
    <article className="panel">
      <TituloPainel
        icone={<UserRound size={16} aria-hidden="true" />}
        titulo="Dados da conta"
        descricao="Nome e e-mail aparecem no menu e servem para entrar"
      />
      <form className="panel-body perfil-dados" onSubmit={enviar} key={usuario.versao}>
        <div className="form-grid">
          <Field name="nome" label="Nome *" value={usuario.nome} required maxLength={120} />
          <Field
            name="email"
            label="E-mail *"
            type="email"
            value={usuario.email}
            required
            maxLength={160}
          />
          <Field
            name="telefone"
            label="Telefone"
            type="tel"
            value={usuario.telefone}
            maxLength={30}
          />
          <Field name="cargo" label="Cargo" value={usuario.cargo} maxLength={80} />
          <Field name="empresa" label="Empresa" value={usuario.empresa} maxLength={120} wide />
        </div>
        <ErroFormulario erro={erro} />
        <div className="dialog-actions">
          <button type="submit" className="button primary" disabled={enviando}>
            Salvar alterações
          </button>
        </div>
      </form>
    </article>
  );
}

function Seguranca() {
  const { toast } = useApp();
  const { erro, enviando, enviar } = useEnvio(async (d, formulario) => {
    if (d.novaSenha !== d.confirmacao) throw new Error("As senhas novas não conferem.");
    await api("/api/usuarios/eu/senha", {
      method: "PUT",
      body: { senhaAtual: d.senhaAtual, novaSenha: d.novaSenha },
    });
    formulario.reset();
    toast("Senha alterada. As outras sessões foram encerradas.");
  });

  return (
    <article className="panel">
      <TituloPainel
        icone={<KeyRound size={16} aria-hidden="true" />}
        titulo="Segurança"
        descricao="Ao trocar a senha, as sessões em outros navegadores são encerradas"
      />
      <form className="panel-body" onSubmit={enviar}>
        <div className="form-grid">
          <Field
            name="senhaAtual"
            label="Senha atual *"
            type="password"
            autoComplete="current-password"
            required
            wide
          />
          <Field
            name="novaSenha"
            label="Nova senha *"
            type="password"
            autoComplete="new-password"
            required
            minLength={8}
            maxLength={72}
          />
          <Field
            name="confirmacao"
            label="Confirmar nova senha *"
            type="password"
            autoComplete="new-password"
            required
            minLength={8}
            maxLength={72}
          />
        </div>
        <ErroFormulario erro={erro} />
        <div className="dialog-actions">
          <button type="submit" className="button primary" disabled={enviando}>
            Alterar senha
          </button>
        </div>
      </form>
    </article>
  );
}

// Leva até o campo pedido (ou abre o menu da foto) e destaca por um instante
function focarCampo(nome) {
  if (nome === "foto") {
    document.querySelector(".profile-photo-button")?.click();
    return;
  }
  const campo = document.querySelector(`.perfil-dados [name="${nome}"]`);
  if (!campo) return;
  campo.scrollIntoView?.({ block: "center", behavior: "smooth" });
  campo.focus({ preventScroll: true });
  const rotulo = campo.closest(".field");
  rotulo?.classList.add("campo-destaque");
  setTimeout(() => rotulo?.classList.remove("campo-destaque"), 1800);
}

const ICONES_PERFIL = { foto: Camera, empresa: Building2, cargo: Briefcase, telefone: Phone };

function CompletarPerfil({ usuario }) {
  const { feitos, total, faltando } = progressoPerfil(usuario);
  if (!faltando.length) return null;
  const pct = Math.round((feitos / total) * 100);
  return (
    <section className="panel completar-perfil" aria-label="Complete seu perfil">
      <div className="completar-topo">
        <div>
          <h2>Complete seu perfil</h2>
          <p>
            {feitos} de {total} itens preenchidos
          </p>
        </div>
        <strong>{pct}%</strong>
      </div>
      <div className="bar-track">
        <div className="bar" style={{ width: pct + "%" }} />
      </div>
      <div className="completar-itens">
        {ITENS_PERFIL.map(([chave, rotulo]) => {
          const Icone = ICONES_PERFIL[chave];
          return faltando.includes(chave) ? (
            <button key={chave} className="completar-item" onClick={() => focarCampo(chave)}>
              <Icone size={14} aria-hidden="true" /> Adicionar {rotulo.toLowerCase()}
            </button>
          ) : (
            <span key={chave} className="completar-item feito">
              <Check size={14} aria-hidden="true" /> {rotulo}
            </span>
          );
        })}
      </div>
    </section>
  );
}

// Menu que fecha ao clicar fora ou apertar Esc
function useMenuFlutuante(botao) {
  const [aberto, setAberto] = useState(false);
  const ref = useRef(null);
  useEffect(() => {
    if (!aberto) return;
    const fora = (e) => {
      if (ref.current && !ref.current.contains(e.target) && !e.target.closest?.(botao)) {
        setAberto(false);
      }
    };
    const esc = (e) => e.key === "Escape" && setAberto(false);
    document.addEventListener("mousedown", fora);
    document.addEventListener("keydown", esc);
    return () => {
      document.removeEventListener("mousedown", fora);
      document.removeEventListener("keydown", esc);
    };
  }, [aberto]);
  return { aberto, setAberto, ref };
}

// Envia, remove ou troca foto e capa e atualiza a sessão
function useImagem(destino) {
  const { atualizarUsuario } = useSessao();
  const { toast } = useApp();
  const [enviando, setEnviando] = useState(false);
  const rota = "/api/usuarios/eu/" + destino;

  async function executar(pedido, mensagem) {
    setEnviando(true);
    try {
      atualizarUsuario(await pedido());
      toast(mensagem);
    } catch (erro) {
      toast(erro.message);
    } finally {
      setEnviando(false);
    }
  }

  async function enviarArquivo(evento, largura, altura, mensagem) {
    const arquivo = evento.target.files?.[0];
    evento.target.value = "";
    if (!arquivo) return;
    let imagem;
    try {
      imagem = await prepararImagem(arquivo, destino, largura, altura);
    } catch (erro) {
      toast(erro.message);
      return;
    }
    await executar(() => api(rota, { method: "PUT", body: imagem }), mensagem);
  }

  const remover = (mensagem) => executar(() => api(rota, { method: "DELETE" }), mensagem);
  const escolherFundo = (fundo) =>
    executar(() => api(rota + "/fundo", { method: "PUT", body: { fundo } }), "Capa atualizada.");

  return { enviando, enviarArquivo, remover, escolherFundo };
}

function CartaoPerfil({ usuario }) {
  const { sair } = useSessao();
  const foto = useImagem("foto");
  const capa = useImagem("capa");
  const capaVisivel = useQuadroEstatico(usuario.capa);
  const menuFoto = useMenuFlutuante(".profile-photo-button");
  const menuCapa = useMenuFlutuante(".profile-cover-button");
  const fundos = menuCapa.aberto;
  const setFundos = menuCapa.setAberto;
  const capaPropria = ehImagem(usuario.capa);
  const arquivoFoto = useRef(null);
  const arquivoCapa = useRef(null);
  const detalhe = [usuario.cargo, usuario.empresa].filter(Boolean).join(" · ");

  return (
    <article className="panel profile-card">
      <div
        className="profile-cover"
        style={estiloCapa(capaPropria ? capaVisivel || usuario.capa : usuario.capa)}>
        {capa.enviando ? (
          <span className="profile-sending" role="status">
            <Loader2 size={15} aria-hidden="true" className="spin" /> Enviando capa…
          </span>
        ) : null}
        <button
          className="profile-cover-button"
          onClick={() => setFundos((v) => !v)}
          aria-expanded={fundos}
          disabled={capa.enviando}>
          <Palette size={14} aria-hidden="true" /> Alterar capa
        </button>
      </div>
      {fundos ? (
        <div className="cover-picker" role="group" aria-label="Escolher capa" ref={menuCapa.ref}>
          <div className="cover-swatches">
            {Object.entries(FUNDOS).map(([chave, fundo]) => (
              <button
                key={chave}
                className={
                  "cover-swatch" + ((usuario.capa ?? "fundo:teal") === chave ? " active" : "")
                }
                disabled={capa.enviando}
                style={{ background: fundo.css }}
                title={fundo.nome}
                aria-label={"Fundo " + fundo.nome}
                onClick={() => capa.escolherFundo(chave)}
              />
            ))}
          </div>
          <div className="actions">
            <button
              className="button small"
              disabled={capa.enviando}
              onClick={() => arquivoCapa.current.click()}>
              <ImagePlus size={13} aria-hidden="true" /> Enviar imagem ou GIF
            </button>
            {usuario.capa ? (
              <button
                className="button small danger"
                disabled={capa.enviando}
                onClick={() => capa.remover("Capa padrão restaurada.")}>
                <Trash2 size={13} aria-hidden="true" /> Restaurar padrão
              </button>
            ) : null}
          </div>
          <small className="cover-hint">
            PNG, JPEG ou WebP são ajustados automaticamente. GIF animado até 4 MB.
          </small>
          <input
            ref={arquivoCapa}
            type="file"
            accept="image/png,image/jpeg,image/webp,image/gif"
            hidden
            onChange={(e) => capa.enviarArquivo(e, 1200, 360, "Capa atualizada.")}
          />
        </div>
      ) : null}
      <div className="profile-avatar-wrap">
        <Avatar usuario={usuario} tamanho={92} className="profile-avatar" />
        {foto.enviando ? (
          <span className="profile-photo-sending" role="status" aria-label="Enviando foto">
            <Loader2 size={20} aria-hidden="true" className="spin" />
          </span>
        ) : null}
        <button
          className="profile-photo-button"
          aria-label="Alterar foto"
          title="Alterar foto"
          aria-haspopup="menu"
          aria-expanded={menuFoto.aberto}
          disabled={foto.enviando}
          onClick={() => menuFoto.setAberto((v) => !v)}>
          <Camera size={15} aria-hidden="true" />
        </button>
        {menuFoto.aberto ? (
          <div className="photo-menu" role="menu" ref={menuFoto.ref}>
            <button
              role="menuitem"
              onClick={() => {
                menuFoto.setAberto(false);
                arquivoFoto.current.click();
              }}>
              <ImagePlus size={15} aria-hidden="true" />
              <span>
                {usuario.foto ? "Trocar foto" : "Enviar foto"}
                <small>PNG, JPEG, WebP ou GIF até 1 MB</small>
              </span>
            </button>
            {usuario.foto ? (
              <button
                role="menuitem"
                className="danger"
                onClick={() => {
                  menuFoto.setAberto(false);
                  foto.remover("Foto removida.");
                }}>
                <Trash2 size={15} aria-hidden="true" />
                <span>Remover foto</span>
              </button>
            ) : null}
          </div>
        ) : null}
        <input
          ref={arquivoFoto}
          type="file"
          accept="image/png,image/jpeg,image/webp,image/gif"
          hidden
          onChange={(e) => foto.enviarArquivo(e, 320, 320, "Foto atualizada.")}
        />
      </div>
      <h2>{usuario.nome}</h2>
      <p className="sub">{detalhe || "Cargo e empresa não informados"}</p>
      <div className="panel-body profile-facts">
        <div className="mini-line">
          <span>
            <Mail size={14} aria-hidden="true" /> E-mail
          </span>
          <strong>{usuario.email}</strong>
        </div>
        <div className="mini-line">
          <span>
            <Phone size={14} aria-hidden="true" /> Telefone
          </span>
          <strong>{usuario.telefone || "—"}</strong>
        </div>
        <div className="mini-line">
          <span>
            <Building2 size={14} aria-hidden="true" /> Empresa
          </span>
          <strong>{usuario.empresa || "—"}</strong>
        </div>
        <div className="mini-line">
          <span>
            <CalendarDays size={14} aria-hidden="true" /> Conta criada em
          </span>
          <strong>{dataHora(usuario.criadoEm)}</strong>
        </div>
        <div className="mini-line">
          <span>
            <Clock size={14} aria-hidden="true" /> Último acesso
          </span>
          <strong>{dataHora(usuario.ultimoAcessoEm)}</strong>
        </div>
        <button className="button danger profile-logout" onClick={sair}>
          <LogOut size={14} aria-hidden="true" /> Sair da conta
        </button>
      </div>
    </article>
  );
}

export function Perfil() {
  const { usuario } = useSessao();
  const [busca, setBusca] = useSearchParams();
  const campo = busca.get("campo");

  // Atalhos como o cartão Empresa do menu chegam com ?campo=
  useEffect(() => {
    if (!campo) return;
    // Limpa o endereço só depois de focar; antes disso cancelaria o foco
    const t = setTimeout(() => {
      focarCampo(campo);
      setBusca({}, { replace: true });
    }, 80);
    return () => clearTimeout(t);
  }, [campo, setBusca]);

  return (
    <>
      <PageHead
        eyebrow="SUA CONTA"
        title="Meu perfil"
        subtitle="Foto, capa, dados de contato e senha."
        image="/assets/images/perfil.svg"
      />
      <section className="grid-2 profile-grid">
        <div className="profile-stack">
          <CompletarPerfil usuario={usuario} />
          <DadosConta usuario={usuario} />
          <Seguranca />
        </div>
        <CartaoPerfil usuario={usuario} />
      </section>
    </>
  );
}
