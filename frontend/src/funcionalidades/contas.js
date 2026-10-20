import { state, titles } from "../core/estado.js";
import { api, params } from "../core/api.js";
import { head, action } from "../componentes/layout.js";
import { escapeHtml, labels, badge, money, date } from "../core/formatacao.js";
import { accountRows } from "../componentes/tabela-contas.js";
import {
  select,
  field,
  enums,
  textarea,
  formMarkup,
  bindForm,
  numberIds,
} from "../componentes/formularios.js";
import { modal, toast } from "../componentes/dialogo.js";
import { $ } from "../core/dom.js";
import { reasonForm, confirmDelete } from "../componentes/acoes-confirmacao.js";

export async function accounts() {
  const type = state.view === "payables" ? "PAGAR" : "RECEBER",
    f = state.filters;
  const data = await api(
    "/api/contas?" +
      params({ tipo: type, ...f, pagina: state.page, tamanho: 10 }),
  );
  state.list = data;
  const status = [
    "ABERTA",
    "PARCIAL",
    "VENCIDA",
    type === "PAGAR" ? "PAGA" : "RECEBIDA",
    "CANCELADA",
  ];
  return (
    head(
      titles[state.view],
      type === "PAGAR"
        ? "Organize seus compromissos e registre cada pagamento."
        : "Acompanhe seus clientes e registre cada recebimento.",
      action("↓ Exportar CSV", "export-accounts") +
        action("+ Nova conta", "new-account", "primary"),
    ) +
    `<section class="panel"><form id="account-filters" class="filters"><input type="search" name="busca" placeholder="Buscar descrição, título ou pessoa" aria-label="Buscar contas" value="${escapeHtml(f.busca || "")}"><select name="situacao" aria-label="Situação"><option value="">Todas as situações</option>${status.map((s) => `<option value="${s}" ${f.situacao === s ? "selected" : ""}>${labels[s]}</option>`).join("")}</select><select name="pessoaId" aria-label="Pessoa"><option value="">Todas as pessoas</option>${state.data.pessoas
      .filter(
        (p) =>
          p.tipo === "AMBOS" ||
          p.tipo === (type === "PAGAR" ? "FORNECEDOR" : "CLIENTE"),
      )
      .map(
        (p) =>
          `<option value="${p.id}" ${String(p.id) === f.pessoaId ? "selected" : ""}>${escapeHtml(p.nome)}</option>`,
      )
      .join(
        "",
      )}</select><select name="categoriaId" aria-label="Categoria"><option value="">Todas as categorias</option>${state.data.categorias
      .filter((c) => c.natureza === (type === "PAGAR" ? "DESPESA" : "RECEITA"))
      .map(
        (c) =>
          `<option value="${c.id}" ${String(c.id) === f.categoriaId ? "selected" : ""}>${escapeHtml(c.nome)}</option>`,
      )
      .join(
        "",
      )}</select><label>Vencimento de <input type="date" name="vencimentoDe" value="${f.vencimentoDe || ""}"></label><label>até <input type="date" name="vencimentoAte" value="${f.vencimentoAte || ""}"></label><label>Emissão de <input type="date" name="emissaoDe" value="${f.emissaoDe || ""}"></label><label>até <input type="date" name="emissaoAte" value="${f.emissaoAte || ""}"></label><button class="button small" type="submit">Filtrar</button><button class="button small" type="button" data-action="clear-filters">Limpar</button></form><div class="table-wrap"><table><thead><tr><th>Título / pessoa</th><th>Categoria</th><th>Vencimento</th><th class="right">Saldo a quitar</th><th>Situação</th><th></th></tr></thead><tbody>${accountRows(data.itens)}</tbody></table></div><div class="table-count"><span>${data.total} título(s) · página ${data.totalPaginas ? data.pagina + 1 : 0} de ${data.totalPaginas}</span><div class="actions"><button class="button small" data-action="prev-page" ${state.page === 0 ? "disabled" : ""}>← Anterior</button><button class="button small" data-action="next-page" ${state.page + 1 >= data.totalPaginas ? "disabled" : ""}>Próxima →</button></div></div></section>`
  );
}

export function accountFields(row = {}, conversion = false) {
  const type = row.tipo || (state.view === "receivables" ? "RECEBER" : "PAGAR");
  let fields = "";
  if (!conversion)
    fields =
      select(
        "pessoaId",
        type === "PAGAR" ? "Fornecedor *" : "Cliente *",
        state.data.pessoas
          .filter(
            (p) =>
              (p.ativo || p.id === row.pessoa?.id) &&
              (p.tipo === "AMBOS" ||
                p.tipo === (type === "PAGAR" ? "FORNECEDOR" : "CLIENTE")),
          )
          .map((p) => [p.id, p.nome]),
        row.pessoa?.id,
      ) +
      select(
        "categoriaId",
        "Categoria *",
        state.data.categorias
          .filter(
            (c) =>
              (c.ativo || c.id === row.categoria?.id) &&
              c.natureza === (type === "PAGAR" ? "DESPESA" : "RECEITA"),
          )
          .map((c) => [c.id, c.codigo + " · " + c.nome]),
        row.categoria?.id,
      ) +
      field(
        "descricao",
        "Descrição *",
        "text",
        row.descricao,
        'required maxlength="180"',
        true,
      );
  fields +=
    field(
      "numeroTitulo",
      "Número do título *",
      "text",
      row.numeroTitulo,
      'required maxlength="60"',
    ) +
    select(
      "tipoTitulo",
      "Documento *",
      enums(["DUPLICATA", "NOTA_PROMISSORIA", "OUTROS"]),
      row.tipoTitulo || "DUPLICATA",
    ) +
    field(
      "emissao",
      "Data de emissão *",
      "date",
      row.emissao || state.today,
      "required",
    ) +
    field(
      "vencimento",
      "Vencimento *",
      "date",
      row.vencimento || row.dataPrevista || state.today,
      "required",
    ) +
    field(
      "valor",
      "Valor definitivo (R$) *",
      "number",
      row.valor ?? row.valorPrevisto ?? "",
      'required min="0.01" step="0.01"',
    ) +
    field(
      "descontoMaximo",
      "Desconto máximo acumulado (R$)",
      "number",
      row.descontoMaximo ?? "0",
      'min="0" step="0.01"',
    ) +
    field(
      "validadeDesconto",
      "Validade do desconto",
      "date",
      row.validadeDesconto,
    ) +
    textarea("observacoes", "Observações", row.observacoes);
  return fields;
}

export function accountForm(row = {}) {
  modal(
    row.id
      ? "Editar título"
      : "Nova conta " +
          (state.view === "receivables" ? "a receber" : "a pagar"),
    formMarkup(accountFields(row)),
  );
  bindForm(async (o) => {
    numberIds(o);
    o.tipo = row.tipo || (state.view === "receivables" ? "RECEBER" : "PAGAR");
    o.validadeDesconto = o.validadeDesconto || null;
    o.descontoMaximo = o.descontoMaximo || "0";
    if (row.id) o.versao = row.versao;
    await api("/api/contas" + (row.id ? "/" + row.id : ""), {
      method: row.id ? "PUT" : "POST",
      body: o,
    });
  });
}

export async function showAccount(id) {
  try {
    const { conta: c, historico: h } = await api("/api/contas/" + id);
    const canSettle = Number(c.saldo) > 0 && c.situacao !== "CANCELADA";
    modal(
      "Título " + c.numeroTitulo,
      `<span class="eyebrow">${c.tipo === "PAGAR" ? "CONTA A PAGAR" : "CONTA A RECEBER"}</span><h2 style="margin:9px 0">${escapeHtml(c.descricao)}</h2><p class="sub">${escapeHtml(c.pessoa.nome)} · ${escapeHtml(c.categoria.nome)} · ${badge(c.situacao)}</p><div class="detail-metrics"><div>Valor original<strong>${money(c.valor)}</strong></div><div>Principal baixado<strong>${money(c.totalBaixado)}</strong></div><div>Saldo a quitar<strong>${money(c.saldo)}</strong></div></div><p class="sub">Emissão: ${date(c.emissao)} · Vencimento: ${date(c.vencimento)}<br>Desconto permitido: ${money(c.descontoMaximo)} até ${date(c.validadeDesconto)}<br>Descontos utilizados: ${money(c.totalDesconto)} · Acréscimos: ${money(c.totalAcrescimo)}<br>${c.despesaOrigemId ? "Origem: previsão #" + c.despesaOrigemId + "<br>" : ""}${escapeHtml(c.observacoes || "")}${c.motivoCancelamento ? "<br>Cancelamento: " + escapeHtml(c.motivoCancelamento) : ""}</p><div class="actions" style="margin:20px 0">${canSettle ? '<button class="button primary" id="settle">' + (c.tipo === "PAGAR" ? "Registrar pagamento" : "Registrar recebimento") + "</button>" : ""}${!c.possuiHistorico && c.situacao !== "CANCELADA" ? '<button class="button" id="edit-title">Editar título</button>' : ""}${c.situacao !== "CANCELADA" ? '<button class="button danger" id="cancel-title">Cancelar título</button>' : ""}${!c.possuiHistorico && !c.despesaOrigemId ? '<button class="button danger" id="delete-title">Excluir</button>' : ""}</div><h3>Histórico de pagamentos / recebimentos</h3>${h.length ? h.map((b) => `<div class="history-item ${b.estornada ? "reversed" : ""}"><div class="history-title"><strong>${date(b.data)} · ${money(b.valorMovimentado)}</strong>${b.estornada ? badge("ESTORNADA") : `<button class="button small danger" data-reverse="${b.id}">Estornar</button>`}</div><p>Principal ${money(b.principal)} + acréscimo ${money(b.acrescimo)} − desconto ${money(b.desconto)}<br>${labels[b.forma]} · ${escapeHtml(b.contaBancaria?.nome || "Sem conta bancária")}<br>${escapeHtml(b.observacoes || "")}${b.estornada ? "<br>Estornada: " + escapeHtml(b.motivoEstorno) : ""}</p></div>`).join("") : '<p class="sub" style="margin-top:14px">Nenhuma baixa registrada.</p>'}`,
    );
    if ($("#settle")) $("#settle").onclick = () => settlementForm(c);
    if ($("#edit-title")) $("#edit-title").onclick = () => accountForm(c);
    if ($("#cancel-title"))
      $("#cancel-title").onclick = () =>
        reasonForm(
          "Cancelar título",
          "/api/contas/" + id + "/cancelamento",
          "Estorne todas as baixas ativas antes de cancelar. O título e seu histórico serão preservados.",
        );
    if ($("#delete-title"))
      $("#delete-title").onclick = () => confirmDelete("/api/contas/" + id);
    document
      .querySelectorAll("[data-reverse]")
      .forEach(
        (b) =>
          (b.onclick = () =>
            reasonForm(
              "Estornar baixa",
              "/api/contas/" + id + "/baixas/" + b.dataset.reverse + "/estorno",
              "O movimento deixará de compor o saldo e o título será recalculado. A baixa permanecerá no histórico.",
            )),
      );
  } catch (e) {
    toast(e.message);
  }
}

export function settlementForm(c) {
  const key = crypto.randomUUID();
  let fields =
    field(
      "data",
      "Data da baixa *",
      "date",
      state.today,
      `required min="${c.emissao}" max="${state.today}"`,
    ) +
    select(
      "forma",
      "Forma *",
      enums(["PIX", "TRANSFERENCIA", "DINHEIRO", "BOLETO", "CARTAO", "CHEQUE"]),
      "PIX",
    ) +
    field(
      "principal",
      "Principal a quitar (R$) *",
      "number",
      c.saldo,
      `required min="0.01" max="${c.saldo}" step="0.01"`,
    ) +
    select(
      "contaBancariaId",
      "Conta bancária",
      state.data["contas-bancarias"]
        .filter((b) => b.ativo && b.agencia.ativo && b.agencia.banco.ativo)
        .map((b) => [b.id, b.descricao + " · " + b.numero]),
      "",
      false,
    ) +
    field("acrescimo", "Acréscimo (R$)", "number", "0", 'min="0" step="0.01"') +
    field("desconto", "Desconto (R$)", "number", "0", 'min="0" step="0.01"') +
    textarea("observacoes", "Observações");
  modal(
    c.tipo === "PAGAR" ? "Registrar pagamento" : "Registrar recebimento",
    `<div class="notice">Saldo do título: <strong>${money(c.saldo)}</strong>. O principal reduz o saldo; o movimento efetivo será principal + acréscimo − desconto. PIX, transferência e cheque exigem conta bancária.</div>` +
      formMarkup(fields, "Confirmar baixa"),
  );
  bindForm(async (o) => {
    numberIds(o);
    o.acrescimo = o.acrescimo || "0";
    o.desconto = o.desconto || "0";
    await api("/api/contas/" + c.id + "/baixas", {
      method: "POST",
      headers: { "Idempotency-Key": key },
      body: o,
    });
  });
}
