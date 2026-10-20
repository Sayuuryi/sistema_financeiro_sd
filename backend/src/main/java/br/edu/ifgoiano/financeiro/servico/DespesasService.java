package br.edu.ifgoiano.financeiro.servico;

import br.edu.ifgoiano.financeiro.api.Dtos.*;
import br.edu.ifgoiano.financeiro.dominio.*;
import br.edu.ifgoiano.financeiro.erro.ErroNegocio;
import br.edu.ifgoiano.financeiro.repositorio.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class DespesasService {
    private final DespesaRepository despesas;
    private final ContaRepository titulos;
    private final ContasService contas;
    private final CadastrosService cadastros;

    public DespesasService(DespesaRepository despesas, ContaRepository titulos, ContasService contas,
            CadastrosService cadastros) {
        this.despesas = despesas;
        this.titulos = titulos;
        this.contas = contas;
        this.cadastros = cadastros;
    }

    private DespesaPrevista obterEntidade(Long id) {
        return despesas.findById(id).orElseThrow(() -> ErroNegocio.ausente("Previsão"));
    }

    private DespesaPrevista bloquear(Long id) {
        return despesas.findForUpdate(id).orElseThrow(() -> ErroNegocio.ausente("Previsão"));
    }

    public List<DespesaResponse> listar() {
        return despesas.findAll().stream()
                .sorted(Comparator.comparing(DespesaPrevista::getDataPrevista).thenComparing(DespesaPrevista::getId))
                .map(this::resposta).toList();
    }

    public DespesaResponse obter(Long id) {
        return resposta(obterEntidade(id));
    }

    @Transactional
    public DespesaResponse salvar(Long id, DespesaRequest r) {
        var d = id == null ? new DespesaPrevista() : bloquear(id);
        if (id != null) {
            CadastrosService.versao(d, r.versao());
            if (titulos.findByDespesaOrigemId(id).isPresent())
                throw ErroNegocio.conflito(
                        "Previsão convertida não pode ser alterada: preserve o valor originalmente estimado.");
        }
        var p = cadastros.pessoa(r.pessoaId());
        var c = cadastros.categoria(r.categoriaId());
        contas.validarVinculos(TipoConta.PAGAR, p, c);
        d.setPessoa(p);
        d.setCategoria(c);
        d.setDescricao(r.descricao().strip());
        d.setReferencia(CadastrosService.texto(r.referencia()));
        d.setDataPrevista(r.dataPrevista());
        d.setValorPrevisto(Dinheiro.positivo(r.valorPrevisto(), "Valor previsto"));
        d.setObservacoes(CadastrosService.texto(r.observacoes()));
        d.setAtivo(r.ativo() == null || r.ativo());
        return resposta(despesas.saveAndFlush(d));
    }

    @Transactional
    public void excluir(Long id) {
        var d = bloquear(id);
        if (titulos.findByDespesaOrigemId(id).isPresent())
            throw ErroNegocio.conflito("Previsão convertida não pode ser excluída.");
        despesas.delete(d);
        despesas.flush();
    }

    @Transactional
    public ContaResponse gerarConta(Long id, GerarContaRequest r) {
        var d = bloquear(id);
        if (!d.isAtivo())
            throw ErroNegocio.regra("Ative a previsão antes de gerar uma conta.");
        if (titulos.findByDespesaOrigemId(id).isPresent())
            throw ErroNegocio.conflito("Esta previsão já gerou uma conta. Consulte o título vinculado.");
        var request = new ContaRequest(TipoConta.PAGAR, d.getPessoa().getId(), d.getCategoria().getId(),
                r.numeroTitulo(), r.tipoTitulo(), d.getDescricao(),
                r.emissao(), r.vencimento(), r.valor(), r.descontoMaximo(), r.validadeDesconto(), r.observacoes(),
                null);
        return contas.criarDeDespesa(request, d);
    }

    private DespesaResponse resposta(DespesaPrevista d) {
        var titulo = titulos.findByDespesaOrigemId(d.getId());
        return new DespesaResponse(d.getId(), d.getVersao(),
                new Referencia(d.getPessoa().getId(), d.getPessoa().getNome()), cadastros.resposta(d.getCategoria()),
                d.getDescricao(), d.getReferencia(), d.getDataPrevista(), d.getValorPrevisto(), d.getObservacoes(),
                d.isAtivo(), titulo.map(Conta::getId).orElse(null),
                titulo.isPresent() ? "CONVERTIDA" : d.isAtivo() ? "PREVISTA" : "INATIVA");
    }
}
