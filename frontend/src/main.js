// Inicialização e navegação. As telas recebem os dados exclusivamente pela API.
import { state, titles } from "./core/estado.js";
import { $ } from "./core/dom.js";
import { loadMasters, params, api } from "./core/api.js";
import { overview } from "./funcionalidades/visao-geral.js";
import {
  accounts,
  showAccount,
  accountForm,
} from "./funcionalidades/contas.js";
import {
  forecasts,
  forecastForm,
  generateForm,
} from "./funcionalidades/previsoes.js";
import { masters, masterForm } from "./funcionalidades/cadastros.js";
import { reports } from "./funcionalidades/relatorios.js";
import { head } from "./componentes/layout.js";
import { escapeHtml, date } from "./core/formatacao.js";
import { confirmDelete } from "./componentes/acoes-confirmacao.js";

let generation = 0;

async function navigate(view, reset = true) {
  state.view = view;
  if (reset) {
    state.page = 0;
    state.filters = {};
  }
  const token = ++generation;
  document
    .querySelectorAll("nav button")
    .forEach((b) => b.classList.toggle("active", b.dataset.view === view));
  $("#breadcrumb").innerHTML = `Financeiro <b>/</b> ${titles[view]}`;
  $("#main").innerHTML = '<div class="loading">Consultando os dados…</div>';
  try {
    await loadMasters();
    let html;
    if (view === "overview") html = await overview();
    else if (view === "payables" || view === "receivables")
      html = await accounts();
    else if (view === "forecasts") html = await forecasts();
    else if (view === "masters") html = masters();
    else html = await reports();
    if (token !== generation) return;
    $("#main").innerHTML = html;
    bindPage();
  } catch (e) {
    if (token === generation)
      $("#main").innerHTML =
        head(titles[view], "") +
        `<div class="form-error">${escapeHtml(e.message)}</div>`;
  }
}

function bindPage() {
  document
    .querySelectorAll("[data-detail]")
    .forEach((b) => (b.onclick = () => showAccount(Number(b.dataset.detail))));
  document
    .querySelectorAll("[data-action]")
    .forEach((b) => (b.onclick = () => runAction(b.dataset.action)));
  document.querySelectorAll("[data-master]").forEach(
    (b) =>
      (b.onclick = () => {
        state.master = b.dataset.master;
        navigate("masters", false);
      }),
  );
  document
    .querySelectorAll("[data-master-edit]")
    .forEach(
      (b) =>
        (b.onclick = () =>
          masterForm(
            state.data[state.master].find(
              (r) => r.id === Number(b.dataset.masterEdit),
            ),
          )),
    );
  document
    .querySelectorAll("[data-master-delete]")
    .forEach(
      (b) =>
        (b.onclick = () =>
          confirmDelete("/api/" + state.master + "/" + b.dataset.masterDelete)),
    );
  document
    .querySelectorAll("[data-forecast-edit]")
    .forEach(
      (b) =>
        (b.onclick = () =>
          forecastForm(
            state.forecasts.find(
              (r) => r.id === Number(b.dataset.forecastEdit),
            ),
          )),
    );
  document
    .querySelectorAll("[data-forecast-generate]")
    .forEach(
      (b) =>
        (b.onclick = () =>
          generateForm(
            state.forecasts.find(
              (r) => r.id === Number(b.dataset.forecastGenerate),
            ),
          )),
    );
  document
    .querySelectorAll("[data-forecast-delete]")
    .forEach(
      (b) =>
        (b.onclick = () =>
          confirmDelete("/api/despesas-previstas/" + b.dataset.forecastDelete)),
    );
  if ($("#period-form"))
    $("#period-form").onsubmit = (e) => {
      e.preventDefault();
      const f = new FormData(e.target);
      state.from = f.get("from");
      state.to = f.get("to");
      navigate(state.view, false);
    };
  if ($("#account-filters"))
    $("#account-filters").onsubmit = (e) => {
      e.preventDefault();
      state.filters = Object.fromEntries(new FormData(e.target));
      state.page = 0;
      navigate(state.view, false);
    };
}

function runAction(key) {
  if (key === "go-payables") return navigate("payables");
  if (key === "new-account") return accountForm();
  if (key === "new-master") return masterForm();
  if (key === "new-forecast") return forecastForm();
  if (key === "clear-filters") {
    state.filters = {};
    state.page = 0;
    return navigate(state.view, false);
  }
  if (key === "prev-page" || key === "next-page") {
    state.page += key === "prev-page" ? -1 : 1;
    return navigate(state.view, false);
  }
  if (key === "export-accounts")
    location.href =
      "/api/relatorios/contas.csv?" +
      params({
        tipo: state.view === "payables" ? "PAGAR" : "RECEBER",
        ...state.filters,
      });
  if (key === "export-movements")
    location.href =
      "/api/relatorios/movimentos.csv?" +
      params({ inicio: state.from, fim: state.to });
}

window.addEventListener("fluxo:atualizar", () => navigate(state.view, false));
$("#close-modal").onclick = () => $("#modal").close();
document
  .querySelectorAll("nav [data-view]")
  .forEach((b) => (b.onclick = () => navigate(b.dataset.view)));
(async () => {
  try {
    const info = await api("/api/sistema");
    state.today = info.dataAtual;
    const month = state.today.slice(0, 7);
    state.from = month + "-01";
    const d = new Date(state.today + "T12:00:00");
    state.to =
      month + "-" + new Date(d.getFullYear(), d.getMonth() + 1, 0).getDate();
    $("#today").textContent = date(state.today);
    await navigate("overview");
  } catch (e) {
    $("#main").innerHTML =
      '<div class="form-error">' + escapeHtml(e.message) + "</div>";
  }
})();
