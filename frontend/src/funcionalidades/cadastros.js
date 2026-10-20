import { state } from "../core/estado.js";
import { head, action } from "../componentes/layout.js";
import { labels, money, escapeHtml, badge } from "../core/formatacao.js";
import {
  field,
  select,
  enums,
  activeField,
  formMarkup,
  bindForm,
  numberIds,
} from "../componentes/formularios.js";
import { modal } from "../componentes/dialogo.js";
import { api } from "../core/api.js";

export const masterLabels = {
  pessoas: "Pessoas",
  categorias: "Categorias",
  bancos: "Bancos",
  agencias: "Agências",
  "contas-bancarias": "Contas bancárias",
};

export function masters() {
  const type = state.master,
    rows = state.data[type];
  return (
    head(
      "Cadastros",
      "Mantenha pessoas, classificações e vínculos bancários organizados.",
      action("+ Novo cadastro", "new-master", "primary"),
    ) +
    `<div class="tabs">${Object.entries(masterLabels)
      .map(
        ([key, label]) =>
          `<button class="tab ${key === type ? "active" : ""}" data-master="${key}">${label}</button>`,
      )
      .join("")}</div>` +
    (type === "categorias"
      ? '<div class="notice">As categorias classificam receitas e despesas. Os códigos são organizadores; este protótipo não realiza escrituração contábil de partidas dobradas.</div>'
      : "") +
    `<section class="panel"><div class="panel-head"><h2>${masterLabels[type]}</h2><span class="sub">${rows.length} registro(s)</span></div><div class="table-wrap"><table><thead><tr><th>Identificação</th><th>Informações</th><th>Situação</th><th class="right">Ações</th></tr></thead><tbody>${
      rows
        .map((r) => {
          let main,
            info,
            detail = "";
          if (type === "pessoas") {
            main = r.nome;
            info = labels[r.tipo];
            detail =
              r.email || r.telefone || r.documento || "Sem contato informado";
          } else if (type === "categorias") {
            main = r.codigo + " · " + r.nome;
            info = labels[r.natureza];
          } else if (type === "bancos") {
            main = r.codigo + " · " + r.nome;
            info = "Banco";
          } else if (type === "agencias") {
            main = r.numero + " · " + r.nome;
            info = r.banco.nome;
          } else {
            main = r.descricao;
            info = money(r.saldoAtual);
            detail =
              r.agencia.banco.nome +
              " · Ag. " +
              r.agencia.numero +
              " · C/C " +
              r.numero;
          }
          return `<tr><td><strong>${escapeHtml(main)}</strong><small>${escapeHtml(detail)}</small></td><td>${escapeHtml(info)}</td><td>${badge(r.ativo ? "ATIVO" : "INATIVA")}</td><td class="right"><button class="button small" data-master-edit="${r.id}">Editar</button> <button class="button small danger" data-master-delete="${r.id}">Excluir</button></td></tr>`;
        })
        .join("") ||
      '<tr><td colspan="4" class="empty">Nenhum cadastro encontrado.</td></tr>'
    }</tbody></table></div></section><p class="sub">Cadastros vinculados a operações não podem ser excluídos. Edite e marque como inativo para preservar o histórico.</p>`
  );
}

export function masterForm(row = {}) {
  const type = state.master;
  let fields = "";
  if (type === "pessoas")
    fields =
      field(
        "nome",
        "Nome *",
        "text",
        row.nome,
        'required maxlength="120"',
        true,
      ) +
      select(
        "tipo",
        "Tipo de pessoa *",
        enums(["CLIENTE", "FORNECEDOR", "AMBOS"]),
        row.tipo,
      ) +
      field(
        "documento",
        "CPF / CNPJ (somente dígitos)",
        "text",
        row.documento,
        'pattern="[0-9]{11}|[0-9]{14}" maxlength="14"',
      ) +
      field("email", "E-mail", "email", row.email, 'maxlength="150"') +
      field("telefone", "Telefone", "text", row.telefone, 'maxlength="30"') +
      field(
        "contato",
        "Pessoa de contato",
        "text",
        row.contato,
        'maxlength="100"',
      ) +
      field(
        "endereco",
        "Endereço",
        "text",
        row.endereco,
        'maxlength="200"',
        true,
      ) +
      field("cidade", "Cidade", "text", row.cidade, 'maxlength="80"') +
      field(
        "estado",
        "UF",
        "text",
        row.estado,
        'pattern="[A-Za-z]{2}" maxlength="2"',
      ) +
      field(
        "cep",
        "CEP",
        "text",
        row.cep,
        'pattern="[0-9]{5}-?[0-9]{3}" maxlength="9"',
      );
  if (type === "categorias")
    fields =
      field(
        "codigo",
        "Código *",
        "text",
        row.codigo,
        'required maxlength="20"',
      ) +
      select(
        "natureza",
        "Natureza *",
        enums(["DESPESA", "RECEITA"]),
        row.natureza,
      ) +
      field(
        "nome",
        "Nome *",
        "text",
        row.nome,
        'required maxlength="100"',
        true,
      );
  if (type === "bancos")
    fields =
      field(
        "codigo",
        "Código *",
        "text",
        row.codigo,
        'required maxlength="10"',
      ) + field("nome", "Nome *", "text", row.nome, 'required maxlength="120"');
  if (type === "agencias")
    fields =
      select(
        "bancoId",
        "Banco *",
        state.data.bancos
          .filter((b) => b.ativo || b.id === row.banco?.id)
          .map((b) => [b.id, b.codigo + " · " + b.nome]),
        row.banco?.id,
      ) +
      field(
        "numero",
        "Número *",
        "text",
        row.numero,
        'required maxlength="20"',
      ) +
      field(
        "nome",
        "Nome *",
        "text",
        row.nome,
        'required maxlength="120"',
        true,
      );
  if (type === "contas-bancarias")
    fields =
      select(
        "agenciaId",
        "Agência *",
        state.data.agencias
          .filter((a) => a.ativo || a.id === row.agencia?.id)
          .map((a) => [a.id, a.banco.nome + " · " + a.numero]),
        row.agencia?.id,
      ) +
      field(
        "numero",
        "Conta / dígito *",
        "text",
        row.numero,
        'required maxlength="30"',
      ) +
      field(
        "descricao",
        "Descrição *",
        "text",
        row.descricao,
        'required maxlength="120"',
      ) +
      field(
        "saldoInicial",
        "Saldo inicial (R$)",
        "number",
        row.saldoInicial ?? "0",
        'step="0.01" required',
      );
  fields += activeField(row.ativo ?? true);
  modal(
    (row.id ? "Editar" : "Novo") + " · " + masterLabels[type],
    formMarkup(fields),
  );
  bindForm(async (o) => {
    numberIds(o);
    o.ativo = o.ativo === "true";
    if ("saldoInicial" in o) o.saldoInicial = o.saldoInicial || "0";
    for (const k in o) if (o[k] === "") o[k] = null;
    if (row.id) o.versao = row.versao;
    await api("/api/" + type + (row.id ? "/" + row.id : ""), {
      method: row.id ? "PUT" : "POST",
      body: o,
    });
  });
}
