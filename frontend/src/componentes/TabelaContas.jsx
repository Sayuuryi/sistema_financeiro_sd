import { ArrowUpRight } from "lucide-react";
import { Badge } from "./Layout.jsx";
import { date, money } from "../core/formatacao.js";

export function AccountRows({ rows, compact = false, onDetail }) {
  if (!rows.length) {
    return (
      <tr>
        <td colSpan={6} className="empty">
          Nenhuma conta encontrada para estes filtros.
        </td>
      </tr>
    );
  }
  return rows.map((c) => (
    <tr key={c.tipo + "-" + c.id}>
      <td>
        <button
          className="table-link"
          style={{ fontSize: 12, textAlign: "left" }}
          onClick={() => onDetail(c.id, c.tipo)}>
          <strong>{c.descricao}</strong>
        </button>
        <small>
          {c.numeroTitulo} · {c.pessoa.nome}
        </small>
      </td>
      {compact ? null : <td>{c.categoria.nome}</td>}
      <td className="nowrap">{date(c.vencimento)}</td>
      <td className="right nowrap">
        <strong>{money(c.saldo)}</strong>
        <small>de {money(c.valor)}</small>
      </td>
      <td>
        <Badge s={c.situacao} />
      </td>
      <td className="right">
        <button
          className="table-link"
          aria-label={"Detalhar " + c.numeroTitulo}
          onClick={() => onDetail(c.id, c.tipo)}>
          <ArrowUpRight size={17} aria-hidden="true" />
        </button>
      </td>
    </tr>
  ));
}
