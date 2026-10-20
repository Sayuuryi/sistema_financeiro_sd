import { modal } from "./dialogo.js";
import { formMarkup, field, bindForm } from "./formularios.js";
import { api } from "../core/api.js";

export function reasonForm(title, path, description) {
  modal(
    title,
    `<div class="notice warning">${description}</div>` +
      formMarkup(
        field(
          "motivo",
          "Motivo *",
          "text",
          "",
          'required minlength="5" maxlength="250"',
          true,
        ),
        "Confirmar",
      ),
  );
  bindForm(async (o) => api(path, { method: "POST", body: o }));
}

export function confirmDelete(path) {
  modal(
    "Excluir registro",
    `<div class="notice warning">A exclusão remove o registro. Operações com vínculos ou histórico são protegidas pelo sistema; nesse caso, use inativação ou cancelamento.</div>` +
      formMarkup("", "Confirmar exclusão"),
  );
  bindForm(async () => api(path, { method: "DELETE" }));
}
