import { useState } from "react";
import { useApp } from "../contexto/AppContext.jsx";

export function Field({ name, label, type = "text", value = "", wide = false, ...attrs }) {
  return (
    <label className={"field" + (wide ? " wide" : "")}>
      {label}
      <input name={name} type={type} defaultValue={value ?? ""} {...attrs} />
    </label>
  );
}

export function SelectField({ name, label, options, value = "", required = true, ...attrs }) {
  const valores = options.map(([v]) => String(v));
  const inicial = valores.includes(String(value ?? "")) ? String(value) : undefined;
  return (
    <label className="field">
      {label}
      <select name={name} aria-label={label} required={required} defaultValue={inicial} {...attrs}>
        {required ? null : <option value="">Sem vínculo</option>}
        {options.map(([v, t]) => (
          <option key={String(v)} value={String(v)}>
            {t}
          </option>
        ))}
      </select>
    </label>
  );
}

export function TextArea({ name, label, value = "" }) {
  return (
    <label className="field wide">
      {label}
      <textarea name={name} maxLength={1000} defaultValue={value || ""} />
    </label>
  );
}

export function ActiveField({ value = true }) {
  return (
    <SelectField
      name="ativo"
      label="Situação"
      options={[
        ["true", "Ativo"],
        ["false", "Inativo"],
      ]}
      value={String(value)}
    />
  );
}

// Formulário padrão das janelas de cadastro
export function FormEditor({ children, submitLabel = "Salvar", onSubmit }) {
  const { closeModal, toast, atualizar } = useApp();
  const [erro, setErro] = useState("");
  const [enviando, setEnviando] = useState(false);

  async function enviar(evento) {
    evento.preventDefault();
    setEnviando(true);
    setErro("");
    try {
      await onSubmit(Object.fromEntries(new FormData(evento.target)));
    } catch (error) {
      setErro(error.message);
      setEnviando(false);
      return;
    }
    closeModal();
    toast("Operação concluída.");
    atualizar();
  }

  return (
    <form id="editor" onSubmit={enviar}>
      <div id="form-error" className="form-error" hidden={!erro} role="alert">
        {erro}
      </div>
      <div className="form-grid">{children}</div>
      <div className="dialog-actions">
        <button type="button" className="button" id="cancel-editor" onClick={closeModal}>
          Cancelar
        </button>
        <button type="submit" className="button primary" disabled={enviando}>
          {submitLabel}
        </button>
      </div>
    </form>
  );
}

export function numberIds(o) {
  for (const k of Object.keys(o)) {
    if (k.endsWith("Id")) {
      o[k] = o[k] ? Number(o[k]) : null;
    }
  }
  return o;
}

// Envio de formulário fora das janelas: guarda erro e estado de envio
export function useEnvio(acao) {
  const [erro, setErro] = useState("");
  const [enviando, setEnviando] = useState(false);

  async function enviar(evento) {
    evento.preventDefault();
    const formulario = evento.currentTarget;
    setEnviando(true);
    setErro("");
    try {
      await acao(Object.fromEntries(new FormData(formulario)), formulario);
    } catch (error) {
      setErro(error.message);
    } finally {
      setEnviando(false);
    }
  }

  return { erro, enviando, enviar };
}
