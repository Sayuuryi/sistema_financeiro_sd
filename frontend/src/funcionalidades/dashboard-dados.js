import { accountPath, api, params } from "../core/api.js";
import { labels, shortDate } from "../core/formatacao.js";

const numero = (v) => Number(v ?? 0) || 0;
const arredondar = (v) => Math.round(v * 100) / 100;

// Busca até 10 páginas de 100 títulos
export async function listarTitulos(tipo, from, to) {
  const itens = [];
  let totalPaginas = 1;
  let total = 0;
  for (let pagina = 0; pagina < totalPaginas && pagina < 10; pagina++) {
    const resposta = await api(
      accountPath(tipo) +
        "?" +
        params({ tipo, vencimentoDe: from, vencimentoAte: to, pagina, tamanho: 100 }),
    );
    itens.push(...(resposta.itens || []));
    totalPaginas = resposta.totalPaginas || 0;
    total = resposta.total ?? itens.length;
  }
  return { itens, total, parcial: itens.length < total };
}

function diasEntre(from, to) {
  const dias = [];
  const fim = new Date(to + "T12:00:00");
  for (
    let d = new Date(from + "T12:00:00");
    d <= fim && dias.length < 800;
    d.setDate(d.getDate() + 1)
  ) {
    dias.push(d.toLocaleDateString("sv-SE"));
  }
  return dias;
}

const rotuloMes = (chave) =>
  new Date(chave + "-15T12:00:00")
    .toLocaleDateString("pt-BR", { month: "short", year: "2-digit" })
    .replace(".", "");

// Agrupa por dia ou por mês em períodos longos
export function fluxoDeCaixa(movimentos, from, to) {
  const dias = diasEntre(from, to);
  const porMes = dias.length > 45;
  const chaveDe = (data) => (porMes ? data.slice(0, 7) : data);
  const mapa = new Map();
  for (const dia of dias) {
    const chave = chaveDe(dia);
    if (!mapa.has(chave)) {
      mapa.set(chave, {
        chave,
        rotulo: porMes ? rotuloMes(chave) : shortDate(chave),
        recebimentos: 0,
        pagamentos: 0,
      });
    }
  }
  for (const m of movimentos) {
    const ponto = mapa.get(chaveDe(m.data));
    if (!ponto) continue;
    if (m.tipo === "RECEBER") ponto.recebimentos += numero(m.valorMovimentado);
    else ponto.pagamentos += numero(m.valorMovimentado);
  }
  let acumulado = 0;
  return [...mapa.values()].map((p) => {
    acumulado += p.recebimentos - p.pagamentos;
    return {
      ...p,
      recebimentos: arredondar(p.recebimentos),
      pagamentos: arredondar(p.pagamentos),
      acumulado: arredondar(acumulado),
    };
  });
}

export function totaisFluxo(serie) {
  const recebimentos = arredondar(serie.reduce((t, p) => t + p.recebimentos, 0));
  const pagamentos = arredondar(serie.reduce((t, p) => t + p.pagamentos, 0));
  return { recebimentos, pagamentos, resultado: arredondar(recebimentos - pagamentos) };
}

export function porForma(movimentos) {
  const mapa = new Map();
  for (const m of movimentos) {
    const atual = mapa.get(m.forma) || {
      forma: m.forma,
      rotulo: labels[m.forma] || m.forma,
      valor: 0,
      quantidade: 0,
    };
    atual.valor += numero(m.valorMovimentado);
    atual.quantidade += 1;
    mapa.set(m.forma, atual);
  }
  return [...mapa.values()]
    .map((f) => ({ ...f, valor: arredondar(f.valor) }))
    .sort((a, b) => b.valor - a.valor);
}

export const SITUACOES = [
  ["ABERTA", "Em aberto"],
  ["PARCIAL", "Parcial"],
  ["VENCIDA", "Vencida"],
  ["QUITADA", "Quitada"],
  ["CANCELADA", "Cancelada"],
];

const situacaoComum = (s) => (s === "PAGA" || s === "RECEBIDA" ? "QUITADA" : s);

export function porSituacao(pagar, receber) {
  const contar = (lista, situacao) =>
    lista.filter((c) => situacaoComum(c.situacao) === situacao).length;
  return SITUACOES.map(([situacao, rotulo]) => ({
    situacao,
    rotulo,
    pagar: contar(pagar, situacao),
    receber: contar(receber, situacao),
  }));
}

export function porCategoria(contas, limite = 6) {
  const mapa = new Map();
  for (const c of contas) {
    if (c.situacao === "CANCELADA") continue;
    const nome = c.categoria?.nome || "Sem categoria";
    mapa.set(nome, (mapa.get(nome) || 0) + numero(c.valor));
  }
  const ordenado = [...mapa.entries()]
    .map(([rotulo, valor]) => ({ rotulo, valor: arredondar(valor) }))
    .sort((a, b) => b.valor - a.valor);
  if (ordenado.length <= limite) return ordenado;
  const principais = ordenado.slice(0, limite - 1);
  const outras = ordenado.slice(limite - 1).reduce((soma, c) => soma + c.valor, 0);
  return [...principais, { rotulo: "Outras", valor: arredondar(outras) }];
}

export function resumoPrevisoes(previsoes, from, to) {
  const noPeriodo = previsoes.filter((p) => p.dataPrevista >= from && p.dataPrevista <= to);
  const somar = (situacao) =>
    arredondar(
      noPeriodo
        .filter((p) => p.situacao === situacao)
        .reduce((s, p) => s + numero(p.valorPrevisto), 0),
    );
  const contar = (situacao) => noPeriodo.filter((p) => p.situacao === situacao).length;
  const prevista = somar("PREVISTA");
  const convertida = somar("CONVERTIDA");
  const total = arredondar(prevista + convertida);
  return {
    total,
    quantidade: contar("PREVISTA") + contar("CONVERTIDA"),
    taxaConversao: total > 0 ? convertida / total : null,
    fatias: [
      {
        situacao: "CONVERTIDA",
        rotulo: "Convertidas em conta",
        valor: convertida,
        quantidade: contar("CONVERTIDA"),
      },
      {
        situacao: "PREVISTA",
        rotulo: "Ainda previstas",
        valor: prevista,
        quantidade: contar("PREVISTA"),
      },
    ],
  };
}

export function maioresSaldos(pagar, receber, limite = 5) {
  const mapa = new Map();
  const acumular = (lista, tipo) => {
    for (const c of lista) {
      if (c.situacao === "CANCELADA" || numero(c.saldo) <= 0) continue;
      const chave = tipo + "|" + (c.pessoa?.nome || "—");
      const atual = mapa.get(chave) || { nome: c.pessoa?.nome || "—", tipo, saldo: 0, titulos: 0 };
      atual.saldo += numero(c.saldo);
      atual.titulos += 1;
      mapa.set(chave, atual);
    }
  };
  acumular(pagar, "PAGAR");
  acumular(receber, "RECEBER");
  return [...mapa.values()]
    .map((p) => ({ ...p, saldo: arredondar(p.saldo) }))
    .sort((a, b) => b.saldo - a.saldo)
    .slice(0, limite);
}

export function indicadores(movimentos, pagar, receber) {
  const titulos = [...pagar, ...receber].filter((c) => c.situacao !== "CANCELADA");
  const quitados = titulos.filter((c) => situacaoComum(c.situacao) === "QUITADA").length;
  const vencidos = titulos.filter((c) => c.situacao === "VENCIDA");
  const movimentado = movimentos.reduce((s, m) => s + numero(m.valorMovimentado), 0);
  return {
    baixas: movimentos.length,
    ticketMedio: movimentos.length ? arredondar(movimentado / movimentos.length) : null,
    titulos: titulos.length,
    quitados,
    taxaQuitacao: titulos.length ? quitados / titulos.length : null,
    vencidos: vencidos.length,
    saldoVencido: arredondar(vencidos.reduce((s, c) => s + numero(c.saldo), 0)),
  };
}
