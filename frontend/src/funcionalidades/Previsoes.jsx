import { useQuery } from "@tanstack/react-query";
import { Eye, FilePlus2, Pencil, Plus, Trash2 } from "lucide-react";
import { useApp } from "../contexto/AppContext.jsx";
import { api } from "../core/api.js";
import { ROTAS_REFERENCIA, estadoDe, useCadastros } from "../core/consultas.js";
import { date, money } from "../core/formatacao.js";
import {
  AVISO_CADASTROS_OFFLINE,
  ActionButton,
  Badge,
  Loading,
  Offline,
  PageHead,
} from "../componentes/Layout.jsx";
import {
  ActiveField,
  Field,
  FormEditor,
  SelectField,
  TextArea,
  numberIds,
} from "../componentes/Formularios.jsx";
import { useConfirmacoes } from "../componentes/AcoesConfirmacao.jsx";
import { AccountFields, useAbrirConta } from "./Contas.jsx";

export function Previsoes() {
  const { openModal, toast } = useApp();
  const { confirmDelete } = useConfirmacoes();
  const abrirConta = useAbrirConta();
  const referencias = useCadastros(ROTAS_REFERENCIA, { timeout: 1800 });
  const lista = useQuery({
    queryKey: ["despesas-previstas"],
    queryFn: () => api("/api/despesas-previstas"),
  });
  const estado = estadoDe(lista);

  if (estado.carregando || referencias.carregando) return <Loading />;
  if (estado.erro) {
    return (
      <Offline
        title="Despesas previstas"
        message={estado.erro.message}
        onRetry={estado.recarregar}
      />
    );
  }

  const rows = lista.data;

  function novaPrevisao() {
    if (!referencias.disponiveis) {
      toast("Cadastros indisponível no momento. Você pode continuar consultando os registros.");
      return;
    }
    openModal("Nova despesa prevista", <PrevisaoForm />);
  }

  return (
    <>
      {referencias.disponiveis ? null : (
        <div className="notice warning" role="status">
          {AVISO_CADASTROS_OFFLINE}
        </div>
      )}
      <PageHead
        eyebrow="PLANEJAMENTO"
        title="Despesas previstas"
        subtitle="Valores estimados que viram conta a pagar quando o documento chega."
        image="/assets/images/despesas-previstas.svg"
        actions={
          <ActionButton onClick={novaPrevisao} style="primary">
            <Plus size={15} aria-hidden="true" /> Nova previsão
          </ActionButton>
        }
      />
      <div className="notice">
        Previsões não entram no saldo a pagar. Quando o documento chegar, use{" "}
        <strong>Gerar conta</strong>.
      </div>
      <section className="panel">
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Previsão / fornecedor</th>
                <th>Data prevista</th>
                <th className="right">Valor estimado</th>
                <th>Situação</th>
                <th className="right">Ações</th>
              </tr>
            </thead>
            <tbody>
              {rows.length ? (
                rows.map((d) => (
                  <tr key={d.id}>
                    <td>
                      <strong>{d.descricao}</strong>
                      <small>
                        {d.pessoa.nome} · {d.categoria.nome}
                      </small>
                    </td>
                    <td>{date(d.dataPrevista)}</td>
                    <td className="right">{money(d.valorPrevisto)}</td>
                    <td>
                      <Badge s={d.situacao} />
                    </td>
                    <td className="right">
                      <div className="actions" style={{ justifyContent: "flex-end" }}>
                        {d.contaGeradaId ? (
                          <button
                            className="button small"
                            onClick={() => abrirConta(d.contaGeradaId, "PAGAR")}>
                            <Eye size={13} aria-hidden="true" /> Ver conta #{d.contaGeradaId}
                          </button>
                        ) : (
                          <>
                            <button
                              className="button small"
                              onClick={() =>
                                openModal("Editar previsão", <PrevisaoForm row={d} />)
                              }>
                              <Pencil size={13} aria-hidden="true" /> Editar
                            </button>
                            <button
                              className="button small"
                              disabled={!d.ativo}
                              onClick={() =>
                                openModal(
                                  "Confirmar despesa e gerar conta",
                                  <GerarContaForm row={d} />,
                                )
                              }>
                              <FilePlus2 size={13} aria-hidden="true" /> Gerar conta
                            </button>
                            <button
                              className="button small danger"
                              onClick={() => confirmDelete("/api/despesas-previstas/" + d.id)}>
                              <Trash2 size={13} aria-hidden="true" /> Excluir
                            </button>
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={5} className="empty">
                    Nenhuma previsão cadastrada.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}

function PrevisaoForm({ row = {} }) {
  const { today } = useApp();
  const referencias = useCadastros(["pessoas", "categorias"], { timeout: 1800 });
  if (referencias.carregando) return <Loading>Carregando cadastros…</Loading>;
  const { pessoas, categorias } = referencias.dados;

  async function salvar(o) {
    numberIds(o);
    o.ativo = o.ativo === "true";
    if (row.id) {
      o.versao = row.versao;
    }
    await api("/api/despesas-previstas" + (row.id ? "/" + row.id : ""), {
      method: row.id ? "PUT" : "POST",
      body: o,
    });
  }

  return (
    <FormEditor onSubmit={salvar}>
      <SelectField
        name="pessoaId"
        label="Fornecedor *"
        options={pessoas.filter((p) => p.ativo && p.tipo !== "CLIENTE").map((p) => [p.id, p.nome])}
        value={row.pessoa?.id}
      />
      <SelectField
        name="categoriaId"
        label="Categoria de despesa *"
        options={categorias
          .filter((c) => c.ativo && c.natureza === "DESPESA")
          .map((c) => [c.id, c.nome])}
        value={row.categoria?.id}
      />
      <Field
        name="descricao"
        label="Descrição *"
        value={row.descricao}
        required
        maxLength={180}
        wide
      />
      <Field name="referencia" label="Referência" value={row.referencia} maxLength={60} />
      <Field
        name="dataPrevista"
        label="Data prevista *"
        type="date"
        value={row.dataPrevista || today}
        required
      />
      <Field
        name="valorPrevisto"
        label="Valor estimado (R$) *"
        type="number"
        value={row.valorPrevisto}
        required
        min="0.01"
        step="0.01"
      />
      <ActiveField value={row.ativo ?? true} />
      <TextArea name="observacoes" label="Observações" value={row.observacoes} />
    </FormEditor>
  );
}

function GerarContaForm({ row }) {
  async function gerar(o) {
    o.validadeDesconto = o.validadeDesconto || null;
    o.descontoMaximo = o.descontoMaximo || "0";
    await api("/api/despesas-previstas/" + row.id + "/gerar-conta", { method: "POST", body: o });
  }

  return (
    <>
      <div className="notice">
        {row.descricao}
        <br />
        Estimativa original: <strong>{money(row.valorPrevisto)}</strong>. Informe o valor real do
        documento; a previsão será preservada.
      </div>
      <FormEditor submitLabel="Gerar conta a pagar" onSubmit={gerar}>
        <AccountFields row={row} conversion tipo="PAGAR" />
      </FormEditor>
    </>
  );
}
