import { useQuery } from "@tanstack/react-query";
import { ArrowDownLeft, ArrowUpRight, CalendarClock, Download, Sigma } from "lucide-react";
import { useApp } from "../contexto/AppContext.jsx";
import { api, download, params } from "../core/api.js";
import { estadoDe } from "../core/consultas.js";
import { date, labels, money } from "../core/formatacao.js";
import {
  ActionButton,
  Loading,
  Metric,
  Offline,
  PageHead,
  Period,
  Warnings,
} from "../componentes/Layout.jsx";
import { useAbrirConta } from "./Contas.jsx";

export function Relatorios() {
  const { from, to, toast } = useApp();
  const abrirConta = useAbrirConta();
  const periodo = params({ inicio: from, fim: to });
  const resumo = useQuery({
    queryKey: ["relatorios", "resumo", from, to],
    queryFn: () => api("/api/relatorios/resumo?" + periodo),
  });
  const movimentos = useQuery({
    queryKey: ["relatorios", "movimentos", from, to],
    queryFn: () => api("/api/relatorios/movimentos?" + periodo),
  });
  const estado = estadoDe(resumo, movimentos);

  if (estado.carregando) return <Loading />;
  if (estado.erro) {
    return <Offline title="Relatórios" message={estado.erro.message} onRetry={estado.recarregar} />;
  }

  const r = resumo.data;
  const m = movimentos.data.itens;
  const notices = [...new Set([...(r.avisos || []), ...(movimentos.data.avisos || [])])];

  function exportar() {
    download("/api/relatorios/movimentos.csv?" + periodo).catch((error) => toast(error.message));
  }

  return (
    <>
      <PageHead
        eyebrow="RELATÓRIOS"
        title="Relatórios financeiros"
        subtitle="Movimentos efetivos do período e exportação em CSV."
        image="/assets/images/relatorios.svg"
        actions={<Period />}
      />
      <Warnings messages={notices} />
      <section className="cards">
        <Metric
          label="Recebimentos"
          value={r.recebido}
          note="Baixas ativas no período"
          icon={<ArrowDownLeft size={16} aria-hidden="true" />}
          color="green"
        />
        <Metric
          label="Pagamentos"
          value={r.pago}
          note="Baixas ativas no período"
          icon={<ArrowUpRight size={16} aria-hidden="true" />}
          color="red"
        />
        <Metric
          label="Resultado de caixa"
          value={r.saldoMovimentado}
          note="Recebimentos − pagamentos"
          icon={<Sigma size={16} aria-hidden="true" />}
        />
        <Metric
          label="Despesas previstas"
          value={r.despesasPrevistas}
          note="Estimativas ativas ainda não convertidas"
          icon={<CalendarClock size={16} aria-hidden="true" />}
        />
      </section>
      <section className="panel">
        <div className="panel-head">
          <div>
            <h2>Livro de movimentos</h2>
            <p>
              {date(from)} a {date(to)} · {m.length} baixa(s) ativa(s)
            </p>
          </div>
          <ActionButton onClick={exportar} style="small">
            <Download size={13} aria-hidden="true" /> Exportar movimentos
          </ActionButton>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Título / pessoa</th>
                <th>Data</th>
                <th>Forma</th>
                <th className="right">Principal</th>
                <th className="right">Acréscimo</th>
                <th className="right">Desconto</th>
                <th className="right">Movimentado</th>
              </tr>
            </thead>
            <tbody>
              {m.length ? (
                m.map((b, i) => (
                  <tr key={b.id ?? b.tipo + "-" + b.contaId + "-" + i}>
                    <td>
                      <button
                        className="table-link"
                        style={{ fontSize: 12 }}
                        onClick={() => abrirConta(b.contaId, b.tipo)}>
                        <strong>
                          {b.tipo === "PAGAR" ? (
                            <ArrowUpRight size={13} aria-label="Pagamento" />
                          ) : (
                            <ArrowDownLeft size={13} aria-label="Recebimento" />
                          )}{" "}
                          {b.numeroTitulo}
                        </strong>
                      </button>
                      <small>{b.pessoa}</small>
                    </td>
                    <td>{date(b.data)}</td>
                    <td>
                      {labels[b.forma]}
                      <small>{b.contaBancaria}</small>
                    </td>
                    <td className="right nowrap">{money(b.principal)}</td>
                    <td className="right nowrap">{money(b.acrescimo)}</td>
                    <td className="right nowrap">{money(b.desconto)}</td>
                    <td className="right nowrap">
                      <strong>{money(b.valorMovimentado)}</strong>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={7} className="empty">
                    Sem movimentos no período.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}
