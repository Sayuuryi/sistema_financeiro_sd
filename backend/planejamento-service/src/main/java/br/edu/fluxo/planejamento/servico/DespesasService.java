package br.edu.fluxo.planejamento.servico;

import br.edu.fluxo.planejamento.api.Dtos.*;
import br.edu.fluxo.planejamento.cliente.*;
import br.edu.fluxo.planejamento.dominio.*;
import br.edu.fluxo.planejamento.exceptions.ErroNegocio;
import br.edu.fluxo.planejamento.repositorio.DespesaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DespesasService {

    private final DespesaRepository despesas;
    private final CadastrosClient cadastros;
    private final ContasClient contas;

    public DespesasService(
        DespesaRepository despesas,
        CadastrosClient cadastros,
        ContasClient contas
    ) {
        this.despesas = despesas;
        this.cadastros = cadastros;
        this.contas = contas;
    }

    private DespesaPrevista entidade(Long id) {
        return despesas.findById(id).orElseThrow(() -> ErroNegocio.ausente("Previsão"));
    }

    private DespesaPrevista bloquear(Long id) {
        return despesas.findForUpdate(id).orElseThrow(() -> ErroNegocio.ausente("Previsão"));
    }

    public List<DespesaResponse> listar() {
        return despesas
            .findAll()
            .stream()
            .sorted(
                Comparator.comparing(DespesaPrevista::getDataPrevista).thenComparing(
                    DespesaPrevista::getId
                )
            )
            .map(this::resposta)
            .toList();
    }

    public DespesaResponse obter(Long id) {
        return resposta(entidade(id));
    }

    @Transactional
    public DespesaResponse salvar(Long id, DespesaRequest r) {
        var d = id == null ? new DespesaPrevista() : bloquear(id);
        if (id != null) {
            RegistroUtils.versao(d, r.versao());
            if (d.getContaGeradaId() != null) {
                throw ErroNegocio.conflito(
                    "Previsão convertida não pode ser alterada: preserve a estimativa original."
                );
            }
        }
        var p = cadastros.pessoa(r.pessoaId());
        var c = cadastros.categoria(r.categoriaId());
        if (
            !p.ativo() ||
            !c.ativo() ||
            p.tipo() == TipoPessoa.CLIENTE ||
            c.natureza() != NaturezaCategoria.DESPESA
        ) {
            throw ErroNegocio.regra("Selecione fornecedor e categoria de despesa ativos.");
        }
        d.setPessoaId(p.id());
        d.setPessoaNome(p.nome());
        d.setCategoriaId(c.id());
        d.setCategoriaVersao(c.versao());
        d.setCategoriaCodigo(c.codigo());
        d.setCategoriaNome(c.nome());
        d.setCategoriaNatureza(c.natureza());
        d.setCategoriaAtivo(c.ativo());
        d.setDescricao(r.descricao().strip());
        d.setReferencia(RegistroUtils.texto(r.referencia()));
        d.setDataPrevista(r.dataPrevista());
        d.setValorPrevisto(Dinheiro.positivo(r.valorPrevisto(), "Valor previsto"));
        d.setObservacoes(RegistroUtils.texto(r.observacoes()));
        d.setAtivo(r.ativo() == null || r.ativo());
        return resposta(despesas.saveAndFlush(d));
    }

    @Transactional
    public void excluir(Long id) {
        var d = bloquear(id);
        if (d.getContaGeradaId() != null) {
            throw ErroNegocio.conflito("Previsão convertida não pode ser excluída.");
        }
        despesas.delete(d);
        despesas.flush();
    }

    @Transactional
    public ContaResponse gerarConta(Long id, GerarContaRequest r) {
        var d = bloquear(id);
        if (!d.isAtivo()) {
            throw ErroNegocio.regra("Ative a previsão antes de gerar a conta.");
        }
        if (d.getContaGeradaId() != null) {
            throw ErroNegocio.conflito(
                "Esta previsão já gerou uma conta. Consulte o título vinculado."
            );
        }
        var request = new ContaRequest(
            TipoConta.PAGAR,
            d.getPessoaId(),
            d.getCategoriaId(),
            r.numeroTitulo(),
            r.tipoTitulo(),
            d.getDescricao(),
            r.emissao(),
            r.vencimento(),
            r.valor(),
            r.descontoMaximo(),
            r.validadeDesconto(),
            r.observacoes(),
            null
        );
        // A origem única evita duplicação ao repetir uma conversão.
        var criada = contas.converter(d.getId(), request);
        d.setContaGeradaId(criada.id());
        despesas.saveAndFlush(d);
        return criada;
    }

    public BigDecimal total(LocalDate inicio, LocalDate fim) {
        if (inicio.isAfter(fim)) {
            throw new ErroNegocio(
                400,
                "PERIODO_INVALIDO",
                "O início não pode ser posterior ao fim."
            );
        }
        return despesas
            .findAll()
            .stream()
            .filter(
                d ->
                    d.isAtivo() &&
                    d.getContaGeradaId() == null &&
                    !d.getDataPrevista().isBefore(inicio) &&
                    !d.getDataPrevista().isAfter(fim)
            )
            .map(DespesaPrevista::getValorPrevisto)
            .reduce(Dinheiro.ZERO, BigDecimal::add);
    }

    private DespesaResponse resposta(DespesaPrevista d) {
        return new DespesaResponse(
            d.getId(),
            d.getVersao(),
            new Referencia(d.getPessoaId(), d.getPessoaNome()),
            new CategoriaResponse(
                d.getCategoriaId(),
                d.getCategoriaVersao(),
                d.getCategoriaCodigo(),
                d.getCategoriaNome(),
                d.getCategoriaNatureza(),
                d.getCategoriaAtivo()
            ),
            d.getDescricao(),
            d.getReferencia(),
            d.getDataPrevista(),
            d.getValorPrevisto(),
            d.getObservacoes(),
            d.isAtivo(),
            d.getContaGeradaId(),
            d.getContaGeradaId() != null ? "CONVERTIDA" : d.isAtivo() ? "PREVISTA" : "INATIVA"
        );
    }
}
