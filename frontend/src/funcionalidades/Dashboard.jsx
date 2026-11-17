import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  LabelList,
  Pie,
  PieChart,
  ReferenceLine,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import {
  ArrowDownLeft,
  ArrowUpRight,
  ChartArea,
  ChartColumn,
  ChartPie,
  CircleAlert,
  CircleCheck,
  CreditCard,
  Inbox,
  ListChecks,
  Receipt,
  Scale,
  Sigma,
  Tags,
  Users,
} from "lucide-react";
import { useApp } from "../contexto/AppContext.jsx";
import { api, params } from "../core/api.js";
import { estadoDe } from "../core/consultas.js";
import { money, moneyCompact, percent } from "../core/formatacao.js";
import { Loading, Metric, PageHead, Period, Warnings } from "../componentes/Layout.jsx";
import {
  fluxoDeCaixa,
  indicadores,
  listarTitulos,
  maioresSaldos,
  porCategoria,
  porForma,
  porSituacao,
  resumoPrevisoes,
  totaisFluxo,
} from "./dashboard-dados.js";

// Cores fixas dos gráficos
export const CORES = {
  entrada: "#00897d",
  saida: "#b7791f",
};

const EIXO = { fontSize: 10, fill: "#8a9ca3" };
const GRADE = "#edf1f2";
const TRILHO = { fill: "#f3f7f6", radius: 4 };
const ROTULO = { fontSize: 10, fill: "#6a7e85", fontWeight: 600 };
const rotuloContagem = (v) => (v ? v : "");

// Degradê da cor sólida para uma versão mais clara
function Degrade({ id, cor, horizontal = false, de = 1, ate = 0.72 }) {
  return (
    <defs>
      <linearGradient id={id} x1="0" y1="0" x2={horizontal ? "1" : "0"} y2={horizontal ? "0" : "1"}>
        <stop offset="0%" stopColor={cor} stopOpacity={horizontal ? ate : de} />
        <stop offset="100%" stopColor={cor} stopOpacity={horizontal ? de : ate} />
      </linearGradient>
    </defs>
  );
}

function Dica({ active, payload, label, formatar = money, titulo }) {
  if (!active || !payload?.length) return null;
  return (
    <div className="chart-tip">
      <strong>{titulo ?? label ?? payload[0].payload.rotulo}</strong>
      {payload.map((p) => (
        <div key={p.dataKey + p.name}>
          <span className="chart-swatch" style={{ background: p.color || p.payload.fill }} />
          {p.name}
          <b>{formatar(p.value)}</b>
        </div>
      ))}
    </div>
  );
}

function Legenda({ itens }) {
  return (
    <div className="chart-legend">
      {itens.map(([rotulo, cor]) => (
        <span key={rotulo}>
          <span className="chart-swatch" style={{ background: cor }} />
          {rotulo}
        </span>
      ))}
    </div>
  );
}

function TabelaDados({ colunas, linhas }) {
  return (
    <details className="chart-data">
      <summary>Ver dados em tabela</summary>
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              {colunas.map((c, i) => (
                <th key={c} className={i ? "right" : ""}>
                  {c}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {linhas.map((linha, i) => (
              <tr key={i}>
                {linha.map((celula, j) => (
                  <td key={j} className={j ? "right nowrap" : ""}>
                    {celula}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </details>
  );
}

// Se o serviço do painel cair, só ele avisa
function Painel({ titulo, descricao, icone = null, lateral = null, consultas, servico, children }) {
  const estado = estadoDe(...consultas);
  return (
    <article className="panel">
      <div className="panel-head">
        <div className="panel-title">
          {icone ? <span className="panel-title-icon">{icone}</span> : null}
          <div>
            <h2>{titulo}</h2>
            <p>{descricao}</p>
          </div>
        </div>
        {lateral}
      </div>
      <div className="panel-body">
        {estado.carregando ? (
          <Loading />
        ) : estado.erro ? (
          <div className="chart-offline" role="status">
            <span>{servico} indisponível no momento.</span>
            <button className="button small" onClick={estado.recarregar}>
              Tentar novamente
            </button>
          </div>
        ) : (
          children()
        )}
      </div>
    </article>
  );
}

const CORES_PREVISAO = { CONVERTIDA: CORES.entrada, PREVISTA: CORES.saida };

// Sem dica flutuante: a fatia sob o mouse aparece no centro da rosca
function RoscaPrevisoes({ r }) {
  const [ativa, setAtiva] = useState(null);
  const fatias = r.fatias
    .filter((f) => f.valor > 0)
    .map((f) => ({ ...f, fill: CORES_PREVISAO[f.situacao] }));
  const destaque = ativa === null ? null : fatias[ativa];
  const total = fatias.reduce((t, f) => t + f.valor, 0);

  return (
    <>
      <div
        className="donut"
        role="img"
        aria-label={
          "Gráfico de rosca: " +
          percent(r.taxaConversao) +
          " do valor previsto já virou conta a pagar"
        }>
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={fatias}
              dataKey="valor"
              nameKey="rotulo"
              innerRadius={64}
              outerRadius={88}
              paddingAngle={fatias.length > 1 ? 2 : 0}
              cornerRadius={fatias.length > 1 ? 4 : 0}
              stroke="#fff"
              strokeWidth={2}
              startAngle={90}
              endAngle={-270}
              onMouseEnter={(_, i) => setAtiva(i)}
              onMouseLeave={() => setAtiva(null)}>
              {fatias.map((f, i) => (
                <Cell
                  key={f.situacao}
                  fill={f.fill}
                  fillOpacity={ativa === null || ativa === i ? 1 : 0.35}
                />
              ))}
            </Pie>
          </PieChart>
        </ResponsiveContainer>
        <div className="donut-center">
          {destaque ? (
            <>
              <strong>{money(destaque.valor)}</strong>
              <small>
                {destaque.rotulo} · {percent(total ? destaque.valor / total : 0)}
              </small>
            </>
          ) : (
            <>
              <strong>{percent(r.taxaConversao)}</strong>
              <small>convertido em conta</small>
            </>
          )}
        </div>
      </div>
      {r.fatias.map((f) => {
        const i = fatias.findIndex((x) => x.situacao === f.situacao);
        return (
          <div
            className={"mini-line donut-line" + (i >= 0 && ativa === i ? " active" : "")}
            key={f.situacao}
            onMouseEnter={() => i >= 0 && setAtiva(i)}
            onMouseLeave={() => setAtiva(null)}>
            <span>
              <span className="chart-swatch" style={{ background: CORES_PREVISAO[f.situacao] }} />
              {f.rotulo} · {f.quantidade}
            </span>
            <strong>{money(f.valor)}</strong>
          </div>
        );
      })}
    </>
  );
}

const Vazio = ({ children }) => (
  <div className="chart-empty">
    <span className="chart-empty-icon">
      <Inbox size={20} aria-hidden="true" />
    </span>
    {children}
  </div>
);

export function Dashboard() {
  const { from, to } = useApp();
  const [naturezaCategoria, setNaturezaCategoria] = useState("DESPESA");
  const periodo = params({ inicio: from, fim: to });

  const resumo = useQuery({
    queryKey: ["relatorios", "resumo", from, to],
    queryFn: () => api("/api/relatorios/resumo?" + periodo),
  });
  const movimentos = useQuery({
    queryKey: ["relatorios", "movimentos", from, to],
    queryFn: () => api("/api/relatorios/movimentos?" + periodo),
  });
  const pagar = useQuery({
    queryKey: ["dashboard", "titulos", "PAGAR", from, to],
    queryFn: () => listarTitulos("PAGAR", from, to),
  });
  const receber = useQuery({
    queryKey: ["dashboard", "titulos", "RECEBER", from, to],
    queryFn: () => listarTitulos("RECEBER", from, to),
  });
  const previsoes = useQuery({
    queryKey: ["despesas-previstas"],
    queryFn: () => api("/api/despesas-previstas"),
  });

  const itensMovimentos = movimentos.data?.itens ?? [];
  const titulosPagar = pagar.data?.itens ?? [];
  const titulosReceber = receber.data?.itens ?? [];
  const ind = indicadores(itensMovimentos, titulosPagar, titulosReceber);
  const titulosProntos = !pagar.isPending && !receber.isPending;
  const titulosIndisponiveis = pagar.isError && receber.isError;

  const avisos = [
    ...new Set([...(resumo.data?.avisos || []), ...(movimentos.data?.avisos || [])]),
    pagar.isError && !receber.isError
      ? "Contas a pagar indisponível no momento. Os gráficos de títulos mostram só as contas a receber."
      : null,
    receber.isError && !pagar.isError
      ? "Contas a receber indisponível no momento. Os gráficos de títulos mostram só as contas a pagar."
      : null,
    pagar.data?.parcial || receber.data?.parcial
      ? "O período tem mais de 1.000 títulos de um mesmo tipo. Os gráficos de títulos consideram os 1.000 primeiros."
      : null,
  ].filter(Boolean);

  const textoRelatorios = (valor) =>
    movimentos.isPending || resumo.isPending
      ? "…"
      : movimentos.isError || resumo.isError
        ? "—"
        : valor;
  const textoTitulos = (valor) => (!titulosProntos ? "…" : titulosIndisponiveis ? "—" : valor);

  return (
    <>
      <PageHead
        eyebrow="ANÁLISE"
        title="Gráficos e estatísticas"
        subtitle="Evolução do caixa, composição dos títulos e previsões do período."
        actions={<Period />}
        image="/assets/images/dashboard-charts.svg"
      />
      <Warnings messages={avisos} />

      <section className="cards" aria-label="Indicadores do período">
        <Metric
          label="Resultado de caixa"
          text={textoRelatorios(money(resumo.data?.saldoMovimentado))}
          note={resumo.isError ? "Indisponível no momento" : "Recebimentos − pagamentos no período"}
          icon={<Sigma size={16} aria-hidden="true" />}
        />
        <Metric
          label="Ticket médio por baixa"
          text={textoRelatorios(money(ind.ticketMedio))}
          note={
            movimentos.isError
              ? "Indisponível no momento"
              : ind.baixas + " baixa(s) ativa(s) no período"
          }
          icon={<Receipt size={16} aria-hidden="true" />}
        />
        <Metric
          label="Títulos quitados"
          text={textoTitulos(percent(ind.taxaQuitacao))}
          note={
            titulosIndisponiveis
              ? "Indisponível no momento"
              : ind.quitados + " de " + ind.titulos + " título(s) com vencimento no período"
          }
          icon={<CircleCheck size={16} aria-hidden="true" />}
          color="green"
        />
        <Metric
          label="Títulos vencidos"
          text={textoTitulos(String(ind.vencidos))}
          note={
            titulosIndisponiveis
              ? "Indisponível no momento"
              : "Saldo vencido: " + money(ind.saldoVencido)
          }
          icon={<CircleAlert size={16} aria-hidden="true" />}
          color="red"
        />
      </section>

      <section className="grid-2">
        <Painel
          titulo="Fluxo de caixa"
          descricao="Recebimentos e pagamentos efetivos pela data da baixa"
          icone={<ChartColumn size={16} aria-hidden="true" />}
          lateral={<span className="badge">CAIXA REALIZADO</span>}
          consultas={[movimentos]}
          servico="Relatórios">
          {() => {
            const serie = fluxoDeCaixa(itensMovimentos, from, to);
            if (!itensMovimentos.length) return <Vazio>Sem movimentos no período.</Vazio>;
            const totais = totaisFluxo(serie);
            return (
              <>
                <div className="flow-totals">
                  <div>
                    <span className="flow-totals-icon in">
                      <ArrowDownLeft size={14} aria-hidden="true" />
                    </span>
                    Recebimentos
                    <strong>{money(totais.recebimentos)}</strong>
                  </div>
                  <div>
                    <span className="flow-totals-icon out">
                      <ArrowUpRight size={14} aria-hidden="true" />
                    </span>
                    Pagamentos
                    <strong>{money(totais.pagamentos)}</strong>
                  </div>
                  <div>
                    <span className="flow-totals-icon">
                      <Scale size={14} aria-hidden="true" />
                    </span>
                    Resultado
                    <strong className={totais.resultado < 0 ? "negative" : ""}>
                      {money(totais.resultado)}
                    </strong>
                  </div>
                </div>
                <Legenda
                  itens={[
                    ["Recebimentos", CORES.entrada],
                    ["Pagamentos", CORES.saida],
                  ]}
                />
                <div
                  className="chart"
                  role="img"
                  aria-label="Gráfico de colunas com recebimentos e pagamentos ao longo do período">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart
                      data={serie}
                      barGap={2}
                      margin={{ top: 6, right: 6, left: 0, bottom: 0 }}>
                      <Degrade id="grad-fluxo-entrada" cor={CORES.entrada} />
                      <Degrade id="grad-fluxo-saida" cor={CORES.saida} />
                      <CartesianGrid vertical={false} stroke={GRADE} strokeDasharray="3 4" />
                      <XAxis
                        dataKey="rotulo"
                        tick={EIXO}
                        tickLine={false}
                        axisLine={{ stroke: GRADE }}
                        minTickGap={14}
                      />
                      <YAxis
                        tick={EIXO}
                        tickLine={false}
                        axisLine={false}
                        width={74}
                        tickFormatter={moneyCompact}
                      />
                      <Tooltip content={<Dica />} cursor={{ fill: "#f4faf8" }} />
                      <Bar
                        dataKey="recebimentos"
                        name="Recebimentos"
                        fill="url(#grad-fluxo-entrada)"
                        radius={[4, 4, 0, 0]}
                        maxBarSize={16}
                      />
                      <Bar
                        dataKey="pagamentos"
                        name="Pagamentos"
                        fill="url(#grad-fluxo-saida)"
                        radius={[4, 4, 0, 0]}
                        maxBarSize={16}
                      />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
                <TabelaDados
                  colunas={["Data", "Recebimentos", "Pagamentos", "Resultado acumulado"]}
                  linhas={serie
                    .filter((p) => p.recebimentos || p.pagamentos)
                    .map((p) => [
                      p.rotulo,
                      money(p.recebimentos),
                      money(p.pagamentos),
                      money(p.acumulado),
                    ])}
                />
              </>
            );
          }}
        </Painel>

        <Painel
          titulo="Formas de pagamento"
          descricao="Valor movimentado por forma de baixa"
          icone={<CreditCard size={16} aria-hidden="true" />}
          consultas={[movimentos]}
          servico="Relatórios">
          {() => {
            const formas = porForma(itensMovimentos);
            if (!formas.length) return <Vazio>Sem movimentos no período.</Vazio>;
            return (
              <>
                <div
                  className="chart"
                  style={{ height: Math.max(formas.length * 40 + 20, 120) }}
                  role="img"
                  aria-label="Gráfico de barras com o valor movimentado por forma de pagamento">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart
                      data={formas}
                      layout="vertical"
                      margin={{ top: 0, right: 76, left: 0, bottom: 0 }}>
                      <Degrade id="grad-formas" cor={CORES.entrada} horizontal />
                      <XAxis type="number" hide />
                      <YAxis
                        type="category"
                        dataKey="rotulo"
                        tick={{ fontSize: 11, fill: "#6a7e85" }}
                        tickLine={false}
                        axisLine={false}
                        width={92}
                      />
                      <Tooltip content={<Dica />} cursor={{ fill: "#f4faf8" }} />
                      <Bar
                        dataKey="valor"
                        name="Movimentado"
                        fill="url(#grad-formas)"
                        background={TRILHO}
                        radius={[0, 4, 4, 0]}
                        maxBarSize={14}>
                        <LabelList
                          dataKey="valor"
                          position="right"
                          formatter={moneyCompact}
                          style={{ fontSize: 11, fill: "#22353d" }}
                        />
                      </Bar>
                    </BarChart>
                  </ResponsiveContainer>
                </div>
                <TabelaDados
                  colunas={["Forma", "Baixas", "Movimentado"]}
                  linhas={formas.map((f) => [f.rotulo, f.quantidade, money(f.valor)])}
                />
              </>
            );
          }}
        </Painel>
      </section>

      <section className="grid-2">
        <Painel
          titulo="Resultado acumulado"
          descricao="Soma dos recebimentos menos pagamentos desde o início do período"
          icone={<ChartArea size={16} aria-hidden="true" />}
          consultas={[movimentos]}
          servico="Relatórios">
          {() => {
            const serie = fluxoDeCaixa(itensMovimentos, from, to);
            if (!itensMovimentos.length) return <Vazio>Sem movimentos no período.</Vazio>;
            const final = serie.at(-1).acumulado;
            return (
              <>
                <div
                  className="chart short"
                  role="img"
                  aria-label={
                    "Gráfico de área com o resultado acumulado; valor final " + money(final)
                  }>
                  <ResponsiveContainer width="100%" height="100%">
                    <AreaChart data={serie} margin={{ top: 8, right: 6, left: 0, bottom: 0 }}>
                      <Degrade id="grad-acumulado" cor={CORES.entrada} de={0.28} ate={0.02} />
                      <CartesianGrid vertical={false} stroke={GRADE} strokeDasharray="3 4" />
                      <XAxis
                        dataKey="rotulo"
                        tick={EIXO}
                        tickLine={false}
                        axisLine={{ stroke: GRADE }}
                        minTickGap={14}
                      />
                      <YAxis
                        tick={EIXO}
                        tickLine={false}
                        axisLine={false}
                        width={74}
                        tickFormatter={moneyCompact}
                      />
                      <ReferenceLine y={0} stroke="#c9d6d9" strokeDasharray="4 4" />
                      <Tooltip content={<Dica />} cursor={{ stroke: "#c9d6d9" }} />
                      <Area
                        type="monotone"
                        dataKey="acumulado"
                        name="Resultado acumulado"
                        stroke={CORES.entrada}
                        strokeWidth={2.2}
                        fill="url(#grad-acumulado)"
                        activeDot={{ r: 5, stroke: "#fff", strokeWidth: 2 }}
                      />
                    </AreaChart>
                  </ResponsiveContainer>
                </div>
                <div className="cash-total">
                  <span>Resultado no fim do período</span>
                  <strong className={final < 0 ? "negative" : ""}>{money(final)}</strong>
                </div>
              </>
            );
          }}
        </Painel>

        <Painel
          titulo="Previsões de despesa"
          descricao="Estimativas com data no período"
          icone={<ChartPie size={16} aria-hidden="true" />}
          consultas={[previsoes]}
          servico="Planejamento">
          {() => {
            const r = resumoPrevisoes(previsoes.data, from, to);
            if (!r.total) return <Vazio>Nenhuma previsão com data no período.</Vazio>;
            return <RoscaPrevisoes r={r} />;
          }}
        </Painel>
      </section>

      <section className="grid-2 even">
        <Painel
          titulo="Títulos por situação"
          descricao="Quantidade de títulos com vencimento no período"
          icone={<ListChecks size={16} aria-hidden="true" />}
          consultas={[pagar, receber].filter((c) => !c.isError || titulosIndisponiveis)}
          servico="Contas a pagar e a receber">
          {() => {
            const serie = porSituacao(titulosPagar, titulosReceber);
            if (!titulosPagar.length && !titulosReceber.length) {
              return <Vazio>Nenhum título com vencimento no período.</Vazio>;
            }
            return (
              <>
                <Legenda
                  itens={[
                    ["A receber", CORES.entrada],
                    ["A pagar", CORES.saida],
                  ]}
                />
                <div
                  className="chart short"
                  role="img"
                  aria-label="Gráfico de colunas com a quantidade de títulos a receber e a pagar por situação">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart
                      data={serie}
                      barGap={2}
                      margin={{ top: 18, right: 6, left: 0, bottom: 0 }}>
                      <Degrade id="grad-situacao-entrada" cor={CORES.entrada} />
                      <Degrade id="grad-situacao-saida" cor={CORES.saida} />
                      <CartesianGrid vertical={false} stroke={GRADE} strokeDasharray="3 4" />
                      <XAxis
                        dataKey="rotulo"
                        tick={EIXO}
                        tickLine={false}
                        axisLine={{ stroke: GRADE }}
                      />
                      <YAxis
                        tick={EIXO}
                        tickLine={false}
                        axisLine={false}
                        width={32}
                        allowDecimals={false}
                      />
                      <Tooltip
                        content={<Dica formatar={(v) => v + " título(s)"} />}
                        cursor={{ fill: "#f4faf8" }}
                      />
                      <Bar
                        dataKey="receber"
                        name="A receber"
                        fill="url(#grad-situacao-entrada)"
                        radius={[4, 4, 0, 0]}
                        maxBarSize={22}>
                        <LabelList
                          dataKey="receber"
                          position="top"
                          formatter={rotuloContagem}
                          style={ROTULO}
                        />
                      </Bar>
                      <Bar
                        dataKey="pagar"
                        name="A pagar"
                        fill="url(#grad-situacao-saida)"
                        radius={[4, 4, 0, 0]}
                        maxBarSize={22}>
                        <LabelList
                          dataKey="pagar"
                          position="top"
                          formatter={rotuloContagem}
                          style={ROTULO}
                        />
                      </Bar>
                    </BarChart>
                  </ResponsiveContainer>
                </div>
                <TabelaDados
                  colunas={["Situação", "A receber", "A pagar"]}
                  linhas={serie.map((s) => [s.rotulo, s.receber, s.pagar])}
                />
              </>
            );
          }}
        </Painel>

        <Painel
          titulo={
            naturezaCategoria === "DESPESA" ? "Despesas por categoria" : "Receitas por categoria"
          }
          descricao="Valor dos títulos com vencimento no período"
          icone={<Tags size={16} aria-hidden="true" />}
          lateral={
            <div className="tabs compact" role="group" aria-label="Natureza das categorias">
              {[
                ["DESPESA", "Despesas"],
                ["RECEITA", "Receitas"],
              ].map(([chave, rotulo]) => (
                <button
                  key={chave}
                  className={"tab" + (naturezaCategoria === chave ? " active" : "")}
                  aria-pressed={naturezaCategoria === chave}
                  onClick={() => setNaturezaCategoria(chave)}>
                  {rotulo}
                </button>
              ))}
            </div>
          }
          consultas={[naturezaCategoria === "DESPESA" ? pagar : receber]}
          servico={naturezaCategoria === "DESPESA" ? "Contas a pagar" : "Contas a receber"}>
          {() => {
            const despesa = naturezaCategoria === "DESPESA";
            const categorias = porCategoria(despesa ? titulosPagar : titulosReceber);
            if (!categorias.length) return <Vazio>Nenhum título com vencimento no período.</Vazio>;
            return (
              <>
                <div
                  className="chart"
                  style={{ height: Math.max(categorias.length * 40 + 20, 120) }}
                  role="img"
                  aria-label={
                    "Gráfico de barras com o valor de " +
                    (despesa ? "despesas" : "receitas") +
                    " por categoria"
                  }>
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart
                      data={categorias}
                      layout="vertical"
                      margin={{ top: 0, right: 76, left: 0, bottom: 0 }}>
                      <Degrade
                        id="grad-categorias"
                        cor={despesa ? CORES.saida : CORES.entrada}
                        horizontal
                      />
                      <XAxis type="number" hide />
                      <YAxis
                        type="category"
                        dataKey="rotulo"
                        tick={{ fontSize: 11, fill: "#6a7e85" }}
                        tickLine={false}
                        axisLine={false}
                        width={128}
                      />
                      <Tooltip content={<Dica />} cursor={{ fill: "#f4faf8" }} />
                      <Bar
                        dataKey="valor"
                        name={despesa ? "Despesas" : "Receitas"}
                        fill="url(#grad-categorias)"
                        background={TRILHO}
                        radius={[0, 4, 4, 0]}
                        maxBarSize={14}>
                        <LabelList
                          dataKey="valor"
                          position="right"
                          formatter={moneyCompact}
                          style={{ fontSize: 11, fill: "#22353d" }}
                        />
                      </Bar>
                    </BarChart>
                  </ResponsiveContainer>
                </div>
                <TabelaDados
                  colunas={["Categoria", "Valor"]}
                  linhas={categorias.map((c) => [c.rotulo, money(c.valor)])}
                />
              </>
            );
          }}
        </Painel>
      </section>

      <Painel
        titulo="Maiores saldos em aberto"
        descricao="Clientes e fornecedores com mais valor a quitar entre os títulos do período"
        icone={<Users size={16} aria-hidden="true" />}
        consultas={[pagar, receber].filter((c) => !c.isError || titulosIndisponiveis)}
        servico="Contas a pagar e a receber">
        {() => {
          const ranking = maioresSaldos(titulosPagar, titulosReceber);
          if (!ranking.length) return <Vazio>Nenhum saldo em aberto no período.</Vazio>;
          const maior = ranking[0].saldo || 1;
          return (
            <>
              <Legenda
                itens={[
                  ["A receber", CORES.entrada],
                  ["A pagar", CORES.saida],
                ]}
              />
              {ranking.map((p) => (
                <div className="rank-item" key={p.tipo + p.nome}>
                  <div className="bar-header">
                    <span>
                      {p.nome}
                      <small>
                        {p.tipo === "PAGAR" ? "a pagar" : "a receber"} · {p.titulos} título(s)
                      </small>
                    </span>
                    <strong>{money(p.saldo)}</strong>
                  </div>
                  <div className="bar-track">
                    <div
                      className={"bar" + (p.tipo === "PAGAR" ? " pagar" : "")}
                      style={{ width: (p.saldo / maior) * 100 + "%" }}></div>
                  </div>
                </div>
              ))}
            </>
          );
        }}
      </Painel>
    </>
  );
}
