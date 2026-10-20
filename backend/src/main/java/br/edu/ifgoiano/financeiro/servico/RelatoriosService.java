package br.edu.ifgoiano.financeiro.servico;

import br.edu.ifgoiano.financeiro.api.Dtos.*;
import br.edu.ifgoiano.financeiro.dominio.*;
import br.edu.ifgoiano.financeiro.erro.ErroNegocio;
import br.edu.ifgoiano.financeiro.repositorio.BaixaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class RelatoriosService {
    private final ContasService contas;
    private final DespesasService despesas;
    private final BaixaRepository baixas;

    public RelatoriosService(ContasService contas, DespesasService despesas, BaixaRepository baixas) {
        this.contas = contas;
        this.despesas = despesas;
        this.baixas = baixas;
    }

    private LocalDate[] periodo(LocalDate inicio, LocalDate fim) {
        var hoje = contas.hoje();
        inicio = inicio == null ? hoje.withDayOfMonth(1) : inicio;
        fim = fim == null ? hoje.withDayOfMonth(hoje.lengthOfMonth()) : fim;
        if (inicio.isAfter(fim))
            throw new ErroNegocio(400, "PERIODO_INVALIDO", "O início deve ser igual ou anterior ao fim.");
        return new LocalDate[] { inicio, fim };
    }

    public List<MovimentoResponse> movimentos(LocalDate inicio, LocalDate fim) {
        var p = periodo(inicio, fim);
        return baixas.findByEstornadaFalseAndDataBetweenOrderByDataDescIdDesc(p[0], p[1]).stream().map(b -> {
            var c = b.getConta();
            return new MovimentoResponse(b.getId(), c.getId(), c.getTipo(), c.getNumeroTitulo(), c.getDescricao(),
                    c.getPessoa().getNome(), b.getData(),
                    b.getPrincipal(), b.getAcrescimo(), b.getDesconto(), b.valorMovimentado(), b.getForma(),
                    b.getContaBancaria() == null ? "Caixa / sem conta bancária" : b.getContaBancaria().getDescricao());
        }).toList();
    }

    public ResumoFinanceiro resumo(LocalDate inicio, LocalDate fim) {
        var p = periodo(inicio, fim);
        var lista = contas.exportar(new FiltroContas(null, null, null, null, p[0], p[1], null, null, null));
        BigDecimal pagar = Dinheiro.ZERO, receber = Dinheiro.ZERO, pago = Dinheiro.ZERO, recebido = Dinheiro.ZERO,
                previsto = Dinheiro.ZERO;
        long vencidas = 0;
        for (var c : lista) {
            if (c.tipo() == TipoConta.PAGAR)
                pagar = pagar.add(c.saldo());
            else
                receber = receber.add(c.saldo());
            if (c.situacao() == SituacaoConta.VENCIDA)
                vencidas++;
        }
        for (var m : movimentos(p[0], p[1])) {
            if (m.tipo() == TipoConta.PAGAR)
                pago = pago.add(m.valorMovimentado());
            else
                recebido = recebido.add(m.valorMovimentado());
        }
        for (var d : despesas.listar())
            if (d.ativo() && d.contaGeradaId() == null && !d.dataPrevista().isBefore(p[0])
                    && !d.dataPrevista().isAfter(p[1]))
                previsto = previsto.add(d.valorPrevisto());
        return new ResumoFinanceiro(p[0], p[1], pagar, receber, pago, recebido, recebido.subtract(pago), previsto,
                vencidas);
    }

    public String contasCsv(FiltroContas filtro) {
        var out = new StringBuilder("\uFEFF");
        linha(out, "ID", "Tipo", "Número", "Pessoa", "Categoria", "Descrição", "Emissão", "Vencimento",
                "Valor original", "Principal baixado", "Saldo", "Situação");
        for (var c : contas.exportar(filtro))
            linha(out, c.id(), c.tipo(), c.numeroTitulo(), c.pessoa().nome(), c.categoria().nome(), c.descricao(),
                    c.emissao(), c.vencimento(), c.valor(), c.totalBaixado(), c.saldo(), c.situacao());
        return out.toString();
    }

    public String movimentosCsv(LocalDate inicio, LocalDate fim) {
        var out = new StringBuilder("\uFEFF");
        linha(out, "Baixa", "Conta", "Tipo", "Título", "Pessoa", "Data", "Principal", "Acréscimo", "Desconto",
                "Movimentado", "Forma", "Conta bancária");
        for (var m : movimentos(inicio, fim))
            linha(out, m.id(), m.contaId(), m.tipo(), m.numeroTitulo(), m.pessoa(), m.data(), m.principal(),
                    m.acrescimo(), m.desconto(), m.valorMovimentado(), m.forma(), m.contaBancaria());
        return out.toString();
    }

    private void linha(StringBuilder out, Object... values) {
        var cells = new ArrayList<String>();
        for (var value : values) {
            String s = value == null ? ""
                    : value instanceof BigDecimal d ? d.toPlainString().replace('.', ',') : value.toString();
            if (value instanceof String && !s.stripLeading().isEmpty()
                    && "=+-@".indexOf(s.stripLeading().charAt(0)) >= 0)
                s = "'" + s;
            cells.add("\"" + s.replace("\"", "\"\"") + "\"");
        }
        out.append(String.join(";", cells)).append("\r\n");
    }
}
