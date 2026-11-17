import { useRef, useState } from "react";
import {
  Building2,
  CalendarDays,
  Camera,
  Clock,
  ImagePlus,
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
import { FUNDOS, estiloCapa, reduzirImagem } from "../core/imagens.js";
import { Avatar } from "../componentes/Avatar.jsx";
import { Field, useEnvio } from "../componentes/Formularios.jsx";
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
      <form className="panel-body" onSubmit={enviar} key={usuario.versao}>
        <div className="form-error" hidden={!erro} role="alert">
          {erro}
        </div>
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
        <div className="form-error" hidden={!erro} role="alert">
          {erro}
        </div>
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
        <div className="dialog-actions">
          <button type="submit" className="button primary" disabled={enviando}>
            Alterar senha
          </button>
        </div>
      </form>
    </article>
  );
}

// Envia foto ou capa já reduzida e atualiza a sessão
function useImagem(rota) {
  const { atualizarUsuario } = useSessao();
  const { toast } = useApp();
  const [enviando, setEnviando] = useState(false);

  async function salvar(valor, mensagem) {
    setEnviando(true);
    try {
      atualizarUsuario(await api(rota, { method: "PUT", body: { imagem: valor } }));
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
    try {
      await salvar(await reduzirImagem(arquivo, largura, altura), mensagem);
    } catch (erro) {
      toast(erro.message);
    }
  }

  return { enviando, salvar, enviarArquivo };
}

function CartaoPerfil({ usuario }) {
  const { sair } = useSessao();
  const foto = useImagem("/api/usuarios/eu/foto");
  const capa = useImagem("/api/usuarios/eu/capa");
  const [fundos, setFundos] = useState(false);
  const arquivoFoto = useRef(null);
  const arquivoCapa = useRef(null);
  const detalhe = [usuario.cargo, usuario.empresa].filter(Boolean).join(" · ");

  return (
    <article className="panel profile-card">
      <div className="profile-cover" style={estiloCapa(usuario.capa)}>
        <button
          className="profile-cover-button"
          onClick={() => setFundos((v) => !v)}
          aria-expanded={fundos}
          disabled={capa.enviando}>
          <Palette size={14} aria-hidden="true" /> Alterar capa
        </button>
      </div>
      {fundos ? (
        <div className="cover-picker" role="group" aria-label="Escolher capa">
          <div className="cover-swatches">
            {Object.entries(FUNDOS).map(([chave, fundo]) => (
              <button
                key={chave}
                className={
                  "cover-swatch" + ((usuario.capa ?? "fundo:teal") === chave ? " active" : "")
                }
                style={{ background: fundo.css }}
                title={fundo.nome}
                aria-label={"Fundo " + fundo.nome}
                onClick={() => capa.salvar(chave, "Capa atualizada.")}
              />
            ))}
          </div>
          <div className="actions">
            <button className="button small" onClick={() => arquivoCapa.current.click()}>
              <ImagePlus size={13} aria-hidden="true" /> Enviar imagem
            </button>
            {usuario.capa ? (
              <button
                className="button small danger"
                onClick={() => capa.salvar(null, "Capa padrão restaurada.")}>
                <Trash2 size={13} aria-hidden="true" /> Restaurar padrão
              </button>
            ) : null}
          </div>
          <input
            ref={arquivoCapa}
            type="file"
            accept="image/png,image/jpeg,image/webp"
            hidden
            onChange={(e) => capa.enviarArquivo(e, 1200, 360, "Capa atualizada.")}
          />
        </div>
      ) : null}
      <div className="profile-avatar-wrap">
        <Avatar usuario={usuario} tamanho={92} className="profile-avatar" />
        <button
          className="profile-photo-button"
          aria-label="Trocar foto"
          title="Trocar foto"
          disabled={foto.enviando}
          onClick={() => arquivoFoto.current.click()}>
          <Camera size={15} aria-hidden="true" />
        </button>
        <input
          ref={arquivoFoto}
          type="file"
          accept="image/png,image/jpeg,image/webp"
          hidden
          onChange={(e) => foto.enviarArquivo(e, 320, 320, "Foto atualizada.")}
        />
      </div>
      <h2>{usuario.nome}</h2>
      <p className="sub">{detalhe || "Cargo e empresa não informados"}</p>
      {usuario.foto ? (
        <button
          className="profile-remove-photo"
          disabled={foto.enviando}
          onClick={() => foto.salvar(null, "Foto removida.")}>
          Remover foto
        </button>
      ) : null}
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
          <DadosConta usuario={usuario} />
          <Seguranca />
        </div>
        <CartaoPerfil usuario={usuario} />
      </section>
    </>
  );
}
