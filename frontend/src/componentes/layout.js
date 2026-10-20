import { state } from "../core/estado.js";
import { money } from "../core/formatacao.js";

export const action = (text, fn, style = "") =>
  `<button class="button ${style}" data-action="${fn}">${text}</button>`;

export function head(title, subtitle, buttons = "") {
  return `<div class="page-head"><div><span class="eyebrow">CONTROLE FINANCEIRO</span><h1>${title}</h1><p class="sub">${subtitle}</p></div><div class="actions">${buttons}</div></div>`;
}

export function period() {
  return `<form id="period-form" class="period"><span>Período</span><input type="date" aria-label="Início do período" name="from" value="${state.from}" required><span>—</span><input type="date" aria-label="Fim do período" name="to" value="${state.to}" required><button class="button small" type="submit">Aplicar</button></form>`;
}

export function metric(label, value, note, icon, color = "") {
  return `<article class="card"><div class="metric-top">${label}<span class="metric-icon ${color}">${icon}</span></div><div class="metric-value">${money(value)}</div><div class="metric-note">${note}</div></article>`;
}
