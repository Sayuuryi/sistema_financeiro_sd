import { escapeHtml, date, money, badge } from "../core/formatacao.js";

export function accountRows(rows, compact = false) {
  return rows.length
    ? rows
        .map(
          (c) =>
            `<tr><td><button class="table-link" data-detail="${c.id}" style="font-size:12px;text-align:left"><strong>${escapeHtml(c.descricao)}</strong></button><small>${escapeHtml(c.numeroTitulo)} · ${escapeHtml(c.pessoa.nome)}</small></td>${compact ? "" : `<td>${escapeHtml(c.categoria.nome)}</td>`}<td class="nowrap">${date(c.vencimento)}</td><td class="right nowrap"><strong>${money(c.saldo)}</strong><small>de ${money(c.valor)}</small></td><td>${badge(c.situacao)}</td><td class="right"><button class="table-link" data-detail="${c.id}" aria-label="Detalhar ${escapeHtml(c.numeroTitulo)}">↗</button></td></tr>`,
        )
        .join("")
    : `<tr><td colspan="6" class="empty">Nenhuma conta encontrada para estes filtros.</td></tr>`;
}
