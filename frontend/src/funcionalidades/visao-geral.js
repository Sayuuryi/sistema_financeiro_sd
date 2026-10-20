import { api, params } from "../core/api.js";
import { state } from "../core/estado.js";
import { head, period, metric, action } from "../componentes/layout.js";
import { money, escapeHtml, date } from "../core/formatacao.js";
import { accountRows } from "../componentes/tabela-contas.js";

export async function overview() {
  const [r, pay, rec] = await Promise.all([
    api(
      "/api/relatorios/resumo?" + params({ inicio: state.from, fim: state.to }),
    ),
    api("/api/contas?tipo=PAGAR&tamanho=100"),
    api("/api/contas?tipo=RECEBER&tamanho=100"),
  ]);
  const pending = [...pay.itens, ...rec.itens]
    .filter((c) => Number(c.saldo) > 0)
    .sort((a, b) => a.vencimento.localeCompare(b.vencimento))
    .slice(0, 6);
  const max = Math.max(Number(r.recebido), Number(r.pago), 1);
  const banks = state.data["contas-bancarias"];
  return (
    head(
      "Seu financeiro, em perspectiva.",
      "Acompanhe compromissos, recebimentos e o que vem pela frente.",
      period(),
    ) +
    `<section class="cards" aria-label="Resumo do período">${metric("A receber", r.aReceber, "Saldo dos títulos com vencimento no período", "↙", "green")}${metric("A pagar", r.aPagar, "Saldo dos títulos com vencimento no período", "↗", "red")}${metric("Recebido no período", r.recebido, "Movimentação efetiva · data do recebimento", "✓", "green")}${metric("Pago no período", r.pago, "Movimentação efetiva · data do pagamento", "✓")}</section>` +
    `<section class="grid-2"><article class="panel"><div class="panel-head"><div><h2>Movimentação do período</h2><p>Recebimentos e pagamentos realizados, após descontos e acréscimos</p></div><span class="badge">CAIXA REALIZADO</span></div><div class="panel-body"><div class="flow-chart"><div><div class="bar-header"><span>Recebimentos</span><strong>${money(r.recebido)}</strong></div><div class="bar-track"><div class="bar" style="width:${(Number(r.recebido) / max) * 100}%"></div></div></div><div><div class="bar-header"><span>Pagamentos</span><strong>${money(r.pago)}</strong></div><div class="bar-track"><div class="bar out" style="width:${(Number(r.pago) / max) * 100}%"></div></div></div></div><div class="cash-total"><span>Resultado das movimentações</span><strong class="${Number(r.saldoMovimentado) < 0 ? "negative" : ""}">${money(r.saldoMovimentado)}</strong></div><div class="insight">Previsões ainda não convertidas: <strong>${money(r.despesasPrevistas)}</strong> no período. Valores estimados ficam separados das obrigações já confirmadas.</div></div></article>` +
    `<article class="panel"><div class="panel-head"><div><h2>Contas bancárias</h2><p>Saldos acumulados · todas as datas</p></div><span>▤</span></div><div class="panel-body">${banks.map((b) => `<div class="bank-item"><span class="bank-symbol">▥</span><div><strong>${escapeHtml(b.descricao)}</strong><small>Ag. ${escapeHtml(b.agencia.numero)} · C/C ${escapeHtml(b.numero)}</small></div><span class="balance ${Number(b.saldoAtual) < 0 ? "negative" : ""}">${money(b.saldoAtual)}</span></div>`).join("") || '<p class="sub">Cadastre uma conta bancária para acompanhar seus saldos.</p>'}<div class="mini-line"><span>Títulos vencidos no período</span><strong>${r.vencidas}</strong></div><div class="mini-line"><span>Atualizado em</span><strong>${date(state.today)}</strong></div></div></article></section>` +
    `<section class="panel"><div class="panel-head"><div><h2>Compromissos em aberto</h2><p>Primeiros vencimentos entre até 100 títulos de cada tipo, sem recorte do período acima</p></div>${action("Ver contas a pagar ↗", "go-payables", "small")}</div><div class="table-wrap"><table><thead><tr><th>Descrição / pessoa</th><th>Vencimento</th><th class="right">Saldo a quitar</th><th>Situação</th><th></th></tr></thead><tbody>${accountRows(pending, true)}</tbody></table></div><div class="table-count"><span>Consulte as listas completas para pesquisar todos os títulos.</span><span>● Dados persistidos localmente</span></div></section>`
  );
}
