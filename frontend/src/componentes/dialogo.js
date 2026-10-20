import { $ } from "../core/dom.js";

let toastTimer;

export function toast(message) {
  const el = $("#toast");
  el.textContent = message;
  el.hidden = false;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => (el.hidden = true), 5500);
}

export function modal(title, html) {
  $("#modal-title").textContent = title;
  $("#modal-body").innerHTML = `<div class="dialog-content">${html}</div>`;
  if (!$("#modal").open) $("#modal").showModal();
}
