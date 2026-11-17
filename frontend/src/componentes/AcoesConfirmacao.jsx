import { useCallback } from "react";
import { useApp } from "../contexto/AppContext.jsx";
import { api } from "../core/api.js";
import { Field, FormEditor } from "./Formularios.jsx";

function ReasonForm({ path, description }) {
  return (
    <>
      <div className="notice warning">{description}</div>
      <FormEditor submitLabel="Confirmar" onSubmit={(o) => api(path, { method: "POST", body: o })}>
        <Field name="motivo" label="Motivo *" required minLength={5} maxLength={250} wide />
      </FormEditor>
    </>
  );
}

function DeleteForm({ path, description, submitLabel }) {
  return (
    <>
      <div className="notice warning">{description}</div>
      <FormEditor submitLabel={submitLabel} onSubmit={() => api(path, { method: "DELETE" })} />
    </>
  );
}

export function useConfirmacoes() {
  const { openModal } = useApp();

  const reasonForm = useCallback(
    (title, path, description) =>
      openModal(title, <ReasonForm path={path} description={description} />),
    [openModal],
  );

  const confirmDelete = useCallback(
    (path) => {
      const arquivar = /^\/api\/(pessoas|categorias|contas-bancarias)\/\d+$/.test(path);
      const description = arquivar
        ? "O cadastro sairá das listas e não poderá ser usado em novas operações. " +
          "Os títulos e pagamentos anteriores preservarão as informações registradas."
        : "A exclusão remove o registro. Operações com vínculos ou histórico são protegidas; " +
          "nesse caso, use inativação ou cancelamento.";
      openModal(
        arquivar ? "Arquivar cadastro" : "Excluir registro",
        <DeleteForm
          path={path}
          description={description}
          submitLabel={arquivar ? "Confirmar arquivamento" : "Confirmar exclusão"}
        />,
      );
    },
    [openModal],
  );

  return { reasonForm, confirmDelete };
}
