import { api, params } from "../core/api.js";
import { state } from "../core/estado.js";
import { head, period, metric, action } from "../componentes/layout.js";
import { date, escapeHtml, labels, money } from "../core/formatacao.js";

export async function reports() {
  const [r, m] = await Promise.all([
    api(
      "/api/relatorios/resumo?" + params({ inicio: state.from, fim: state.to }),
    ),
    api(
      "/api/relatorios/movimentos?" +
        params({ inicio: state.from, fim: state.to }),
    ),
  ]);
  return (
    head(
      "Relatórios financeiros",
      "Confira os movimentos efetivos e exporte os dados para análise.",
      period(),
    ) +
    `<section class="cards">${metric("Recebimentos", r.recebido, "Baixas ativas no período", "↙", "green")}${metric("Pagamentos", r.pago, "Baixas ativas no período", "↗", "red")}${metric("Resultado de caixa", r.saldoMovimentado, "Recebimentos − pagamentos", "∑")}${metric("Despesas previstas", r.despesasPrevistas, "Estimativas ativas ainda não convertidas", "◷")}</section><div class="notice">O movimento é calculado pela <strong>data da baixa</strong>: principal + acréscimo − desconto. Estornos são excluídos. O saldo dos títulos, na visão geral, usa a <strong>data de vencimento</strong>. Os saldos bancários incluem o saldo inicial e todos os movimentos vinculados à respectiva conta.</div>` +
    `<section class="panel"><div class="panel-head"><div><h2>Livro de movimentos</h2><p>${date(state.from)} a ${date(state.to)} · ${m.length} baixa(s) ativa(s)</p></div>${action("↓ Exportar movimentos", "export-movements", "small")}</div><div class="table-wrap"><table><thead><tr><th>Título / pessoa</th><th>Data</th><th>Forma</th><th class="right">Principal</th><th class="right">Acréscimo</th><th class="right">Desconto</th><th class="right">Movimentado</th></tr></thead><tbody>${m.map((b) => `<tr><td><button class="table-link" style="font-size:12px" data-detail="${b.contaId}"><strong>${b.tipo === "PAGAR" ? "↗" : "↙"} ${escapeHtml(b.numeroTitulo)}</strong></button><small>${escapeHtml(b.pessoa)}</small></td><td>${date(b.data)}</td><td>${labels[b.forma]}<small>${escapeHtml(b.contaBancaria)}</small></td><td class="right nowrap">${money(b.principal)}</td><td class="right nowrap">${money(b.acrescimo)}</td><td class="right nowrap">${money(b.desconto)}</td><td class="right nowrap"><strong>${money(b.valorMovimentado)}</strong></td></tr>`).join("") || '<tr><td colspan="7" class="empty">Sem movimentos no período.</td></tr>'}</tbody></table></div></section>`
  );
}
