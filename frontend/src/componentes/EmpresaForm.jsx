import { useState } from "react";
import { Trash2 } from "lucide-react";
import { useApp } from "../contexto/AppContext.jsx";
import { useSessao } from "../contexto/SessaoContext.jsx";
import { api } from "../core/api.js";
import { Field, useEnvio } from "./Formularios.jsx";

// Edição rápida da empresa, aberta pelo cartão do menu lateral
export function EmpresaForm() {
  const { usuario, atualizarUsuario } = useSessao();
  const { closeModal, toast } = useApp();

  async function salvar(empresa) {
    const atualizado = await api("/api/usuarios/eu", {
      method: "PUT",
      body: {
        nome: usuario.nome,
        email: usuario.email,
        telefone: usuario.telefone ?? "",
        cargo: usuario.cargo ?? "",
        empresa,
        versao: usuario.versao,
      },
    });
    atualizarUsuario(atualizado);
    closeModal();
    toast(empresa ? "Empresa salva." : "Empresa removida.");
  }

  const gravar = useEnvio((d) => salvar(d.empresa.trim()));
  const [removendo, setRemovendo] = useState(false);
  const [erroRemocao, setErroRemocao] = useState("");
  const erro = gravar.erro || erroRemocao;
  const enviando = gravar.enviando || removendo;

  async function remover() {
    setRemovendo(true);
    setErroRemocao("");
    try {
      await salvar("");
    } catch (e) {
      setErroRemocao(e.message);
    } finally {
      setRemovendo(false);
    }
  }

  return (
    <form onSubmit={gravar.enviar}>
      <div className="form-error" hidden={!erro} role="alert">
        {erro}
      </div>
      <div className="form-grid">
        <Field
          name="empresa"
          label="Nome da empresa"
          value={usuario.empresa}
          required
          maxLength={120}
          placeholder="Ex.: Ateliê Norte"
          autoFocus
          wide
        />
      </div>
      <p className="sub empresa-dica">Aparece no menu lateral e no seu perfil.</p>
      <div className="dialog-actions">
        {usuario.empresa ? (
          <button
            type="button"
            className="button danger empresa-remover"
            disabled={enviando}
            onClick={remover}>
            <Trash2 size={14} aria-hidden="true" /> Remover empresa
          </button>
        ) : null}
        <button type="button" className="button" onClick={closeModal}>
          Cancelar
        </button>
        <button type="submit" className="button primary" disabled={enviando}>
          Salvar
        </button>
      </div>
    </form>
  );
}
