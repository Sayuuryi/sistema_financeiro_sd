// Campos compartilhados. O evento fluxo:atualizar evita dependência circular com a navegação.
import { escapeHtml, labels } from "../core/formatacao.js";
import { $ } from "../core/dom.js";
import { toast } from "./dialogo.js";

export function field(
  name,
  label,
  type = "text",
  value = "",
  attrs = "",
  wide = false,
) {
  return `<label class="field ${wide ? "wide" : ""}">${label}<input name="${name}" type="${type}" value="${escapeHtml(value ?? "")}" ${attrs}></label>`;
}

export function select(name, label, options, value = "", required = true) {
  return `<label class="field">${label}<select name="${name}" aria-label="${escapeHtml(label)}" ${required ? "required" : ""}>${required ? "" : '<option value="">Sem vínculo</option>'}${options.map(([v, t]) => `<option value="${escapeHtml(v)}" ${String(v) === String(value) ? "selected" : ""}>${escapeHtml(t)}</option>`).join("")}</select></label>`;
}

export function enums(keys) {
  return keys.map((k) => [k, labels[k] || k]);
}

export function textarea(name, label, value = "") {
  return `<label class="field wide">${label}<textarea name="${name}" maxlength="1000">${escapeHtml(value || "")}</textarea></label>`;
}

export function activeField(value = true) {
  return select(
    "ativo",
    "Situação",
    [
      [true, "Ativo"],
      [false, "Inativo"],
    ],
    value,
  );
}

export function formMarkup(fields, submit = "Salvar") {
  return `<form id="editor"><div id="form-error" class="form-error" hidden role="alert"></div><div class="form-grid">${fields}</div><div class="dialog-actions"><button type="button" class="button" id="cancel-editor">Cancelar</button><button type="submit" class="button primary">${submit}</button></div></form>`;
}

export function bindForm(onSubmit) {
  $("#cancel-editor").onclick = () => $("#modal").close();
  $("#editor").onsubmit = async (e) => {
    e.preventDefault();
    const button = e.submitter;
    button.disabled = true;
    $("#form-error").hidden = true;
    try {
      await onSubmit(Object.fromEntries(new FormData(e.target)));
      $("#modal").close();
      toast("Operação concluída.");
      window.dispatchEvent(new CustomEvent("fluxo:atualizar"));
    } catch (error) {
      $("#form-error").textContent = error.message;
      $("#form-error").hidden = false;
    } finally {
      button.disabled = false;
    }
  };
}

export function numberIds(o) {
  for (const k of Object.keys(o))
    if (k.endsWith("Id")) o[k] = o[k] ? Number(o[k]) : null;
  return o;
}
