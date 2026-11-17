import { useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import {
  ArrowDownLeft,
  ArrowUpRight,
  CalendarClock,
  ChartColumn,
  Check,
  Landmark,
  Wallet,
} from "lucide-react";
import { useApp } from "../contexto/AppContext.jsx";
import { api, params } from "../core/api.js";
import { estadoDe } from "../core/consultas.js";
import { date, money } from "../core/formatacao.js";
import {
  ActionButton,
  Loading,
  Metric,
  Offline,
  PageHead,
  Period,
  Warnings,
} from "../componentes/Layout.jsx";
import { AccountRows } from "../componentes/TabelaContas.jsx";
import { useAbrirConta } from "./Contas.jsx";

export function VisaoGeral() {
  const { from, to, today } = useApp();
  const navigate = useNavigate();
  const abrirConta = useAbrirConta();
  const consulta = useQuery({
    queryKey: ["visao-geral", from, to],
    queryFn: () => api("/api/visao-geral?" + params({ inicio: from, fim: to })),
  });
  const estado = estadoDe(consulta);

  if (estado.carregando) return <Loading />;
  if (estado.erro) {
    return (
      <Offline title="Visão geral" message={estado.erro.message} onRetry={estado.recarregar} />
    );
  }

  const r = consulta.data;
  const pending = r.contas
    .filter((c) => Number(c.saldo) > 0)
    .sort((a, b) => a.vencimento.localeCompare(b.vencimento))
    .slice(0, 6);
  const max = Math.max(Number(r.recebido), Number(r.pago), 1);
  const banks = r.contasBancarias;
  const banksOffline = r.avisos?.some((message) => message.startsWith("Cadastros"));

  return (
    <>
      <PageHead
        eyebrow="RESUMO"
        title="Visão geral"
        subtitle="Saldos, compromissos e movimentações do período."
        actions={<Period />}
        image="/assets/images/financial-overview.svg"
      />
      <Warnings messages={r.avisos} />
      <section className="cards" aria-label="Resumo do período">
        <Metric
          label="A receber"
          value={r.aReceber}
          note="Saldo dos títulos com vencimento no período"
          icon={<ArrowDownLeft size={16} aria-hidden="true" />}
          color="green"
        />
        <Metric
          label="A pagar"
          value={r.aPagar}
          note="Saldo dos títulos com vencimento no período"
          icon={<ArrowUpRight size={16} aria-hidden="true" />}
          color="red"
        />
        <Metric
          label="Recebido no período"
          value={r.recebido}
          note="Movimentação efetiva · data do recebimento"
          icon={<Check size={16} aria-hidden="true" />}
          color="green"
        />
        <Metric
          label="Pago no período"
          value={r.pago}
          note="Movimentação efetiva · data do pagamento"
          icon={<Check size={16} aria-hidden="true" />}
        />
      </section>
      <section className="grid-2">
        <article className="panel">
          <div className="panel-head">
            <div className="panel-title">
              <span className="panel-title-icon">
                <ChartColumn size={16} aria-hidden="true" />
              </span>
              <div>
                <h2>Movimentação do período</h2>
                <p>Recebimentos e pagamentos realizados, após descontos e acréscimos</p>
              </div>
            </div>
            <span className="badge">CAIXA REALIZADO</span>
          </div>
          <div className="panel-body">
            <div className="flow-chart">
              <div>
                <div className="bar-header">
                  <span className="flow-label">
                    <ArrowDownLeft size={14} aria-hidden="true" /> Recebimentos
                  </span>
                  <strong>{money(r.recebido)}</strong>
                </div>
                <div className="bar-track">
                  <div
                    className="bar"
                    style={{ width: (Number(r.recebido) / max) * 100 + "%" }}></div>
                </div>
              </div>
              <div>
                <div className="bar-header">
                  <span className="flow-label out">
                    <ArrowUpRight size={14} aria-hidden="true" /> Pagamentos
                  </span>
                  <strong>{money(r.pago)}</strong>
                </div>
                <div className="bar-track">
                  <div
                    className="bar out"
                    style={{ width: (Number(r.pago) / max) * 100 + "%" }}></div>
                </div>
              </div>
            </div>
            <div className="cash-total">
              <span>Resultado das movimentações</span>
              <strong className={Number(r.saldoMovimentado) < 0 ? "negative" : ""}>
                {money(r.saldoMovimentado)}
              </strong>
            </div>
            <div className="insight">
              Previsões ainda não convertidas no período:{" "}
              <strong>{money(r.despesasPrevistas)}</strong>
            </div>
          </div>
        </article>
        <article className="panel">
          <div className="panel-head">
            <div className="panel-title">
              <span className="panel-title-icon">
                <Wallet size={16} aria-hidden="true" />
              </span>
              <div>
                <h2>Contas bancárias</h2>
                <p>Saldos acumulados · todas as datas</p>
              </div>
            </div>
          </div>
          <div className="panel-body">
            {banks.length ? (
              banks.map((b) => (
                <div className="bank-item" key={b.id ?? b.numero}>
                  <span className="bank-symbol">
                    <Landmark size={16} aria-hidden="true" />
                  </span>
                  <div>
                    <strong>{b.descricao}</strong>
                    <small>
                      Ag. {b.agencia.numero} · C/C {b.numero}
                    </small>
                  </div>
                  <span className={"balance" + (Number(b.saldoAtual) < 0 ? " negative" : "")}>
                    {money(b.saldoAtual)}
                  </span>
                </div>
              ))
            ) : banksOffline ? (
              <p className="sub">Contas bancárias indisponíveis no momento.</p>
            ) : (
              <p className="sub">Cadastre uma conta bancária para acompanhar seus saldos.</p>
            )}
            <div className="mini-line">
              <span>Títulos vencidos no período</span>
              <strong>{r.vencidas ?? "—"}</strong>
            </div>
            <div className="mini-line">
              <span>Atualizado em</span>
              <strong>{date(today)}</strong>
            </div>
          </div>
        </article>
      </section>
      <section className="panel">
        <div className="panel-head">
          <div className="panel-title">
            <span className="panel-title-icon">
              <CalendarClock size={16} aria-hidden="true" />
            </span>
            <div>
              <h2>Compromissos em aberto</h2>
              <p>Próximos vencimentos de contas a pagar e a receber</p>
            </div>
          </div>
          <ActionButton onClick={() => navigate("/contas-pagar")} style="small">
            Ver contas a pagar <ArrowUpRight size={13} aria-hidden="true" />
          </ActionButton>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Descrição / pessoa</th>
                <th>Vencimento</th>
                <th className="right">Saldo a quitar</th>
                <th>Situação</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <AccountRows rows={pending} compact onDetail={abrirConta} />
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}
