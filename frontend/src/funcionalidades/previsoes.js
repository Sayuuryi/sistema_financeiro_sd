import { api } from "../core/api.js";
import { state } from "../core/estado.js";
import { head, action } from "../componentes/layout.js";
import { escapeHtml, date, money, badge } from "../core/formatacao.js";
import {
  select,
  field,
  activeField,
  textarea,
  formMarkup,
  bindForm,
  numberIds,
} from "../componentes/formularios.js";
import { modal } from "../componentes/dialogo.js";
import { accountFields } from "./contas.js";

export async function forecasts() {
  const rows = await api("/api/despesas-previstas");
  state.forecasts = rows;
  return (
    head(
      "Despesas previstas",
      "Planeje o valor estimado e transforme a previsão em um título definitivo.",
      action("+ Nova previsão", "new-forecast", "primary"),
    ) +
    `<div class="notice">Uma previsão não é uma conta a pagar. Ao receber o documento, confirme o valor e o vencimento em <strong>Gerar conta</strong>. O valor estimado original será preservado, sem duplicar o saldo a pagar.</div><section class="panel"><div class="table-wrap"><table><thead><tr><th>Previsão / fornecedor</th><th>Data prevista</th><th class="right">Valor estimado</th><th>Situação</th><th class="right">Ações</th></tr></thead><tbody>${rows.map((d) => `<tr><td><strong>${escapeHtml(d.descricao)}</strong><small>${escapeHtml(d.pessoa.nome)} · ${escapeHtml(d.categoria.nome)}</small></td><td>${date(d.dataPrevista)}</td><td class="right">${money(d.valorPrevisto)}</td><td>${badge(d.situacao)}</td><td class="right"><div class="actions" style="justify-content:flex-end">${d.contaGeradaId ? `<button class="button small" data-detail="${d.contaGeradaId}">Ver conta #${d.contaGeradaId}</button>` : `<button class="button small" data-forecast-edit="${d.id}">Editar</button><button class="button small" data-forecast-generate="${d.id}" ${d.ativo ? "" : "disabled"}>Gerar conta ↗</button><button class="button small danger" data-forecast-delete="${d.id}">Excluir</button>`}</div></td></tr>`).join("") || '<tr><td colspan="5" class="empty">Nenhuma previsão cadastrada.</td></tr>'}</tbody></table></div></section>`
  );
}

export function forecastForm(row = {}) {
  let fields =
    select(
      "pessoaId",
      "Fornecedor *",
      state.data.pessoas
        .filter((p) => p.ativo && p.tipo !== "CLIENTE")
        .map((p) => [p.id, p.nome]),
      row.pessoa?.id,
    ) +
    select(
      "categoriaId",
      "Categoria de despesa *",
      state.data.categorias
        .filter((c) => c.ativo && c.natureza === "DESPESA")
        .map((c) => [c.id, c.nome]),
      row.categoria?.id,
    ) +
    field(
      "descricao",
      "Descrição *",
      "text",
      row.descricao,
      'required maxlength="180"',
      true,
    ) +
    field(
      "referencia",
      "Referência",
      "text",
      row.referencia,
      'maxlength="60"',
    ) +
    field(
      "dataPrevista",
      "Data prevista *",
      "date",
      row.dataPrevista || state.today,
      "required",
    ) +
    field(
      "valorPrevisto",
      "Valor estimado (R$) *",
      "number",
      row.valorPrevisto,
      'required min="0.01" step="0.01"',
    ) +
    activeField(row.ativo ?? true) +
    textarea("observacoes", "Observações", row.observacoes);
  modal(
    row.id ? "Editar previsão" : "Nova despesa prevista",
    formMarkup(fields),
  );
  bindForm(async (o) => {
    numberIds(o);
    o.ativo = o.ativo === "true";
    if (row.id) o.versao = row.versao;
    await api("/api/despesas-previstas" + (row.id ? "/" + row.id : ""), {
      method: row.id ? "PUT" : "POST",
      body: o,
    });
  });
}

export function generateForm(row) {
  modal(
    "Confirmar despesa e gerar conta",
    `<div class="notice">${escapeHtml(row.descricao)}<br>Estimativa original: <strong>${money(row.valorPrevisto)}</strong>. Informe o valor real do documento; a previsão será preservada.</div>` +
      formMarkup(accountFields(row, true), "Gerar conta a pagar"),
  );
  bindForm(async (o) => {
    o.validadeDesconto = o.validadeDesconto || null;
    o.descontoMaximo = o.descontoMaximo || "0";
    await api("/api/despesas-previstas/" + row.id + "/gerar-conta", {
      method: "POST",
      body: o,
    });
  });
}
