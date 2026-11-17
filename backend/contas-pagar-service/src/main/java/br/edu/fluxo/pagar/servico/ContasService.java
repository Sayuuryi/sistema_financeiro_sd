package br.edu.fluxo.pagar.servico;

import br.edu.fluxo.pagar.api.Dtos.*;
import br.edu.fluxo.pagar.cliente.CadastrosClient;
import br.edu.fluxo.pagar.dominio.*;
import br.edu.fluxo.pagar.exceptions.ErroNegocio;
import br.edu.fluxo.pagar.repositorio.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ContasService {

    private final ContaRepository contas;
    private final BaixaRepository baixas;
    private final CadastrosClient cadastros;
    private final Clock clock;
    private final TipoConta tipo;

    public ContasService(
        ContaRepository contas,
        BaixaRepository baixas,
        CadastrosClient cadastros,
        Clock clock,
        @org.springframework.beans.factory.annotation.Value("${contas.tipo}") TipoConta tipo
    ) {
        this.contas = contas;
        this.baixas = baixas;
        this.cadastros = cadastros;
        this.clock = clock;
        this.tipo = tipo;
    }

    public LocalDate hoje() {
        return LocalDate.now(clock);
    }

    public Conta conta(Long id) {
        return contas
            .findById(id)
            .filter(c -> c.getTipo() == tipo)
            .orElseThrow(() -> ErroNegocio.ausente("Conta"));
    }

    private Conta bloquear(Long id) {
        return contas
            .findForUpdate(id)
            .filter(c -> c.getTipo() == tipo)
            .orElseThrow(() -> ErroNegocio.ausente("Conta"));
    }

    public TipoConta tipo() {
        return tipo;
    }

    private FiltroContas escopo(FiltroContas f) {
        if (f.tipo() != null && f.tipo() != tipo) {
            throw new ErroNegocio(
                400,
                "TIPO_INVALIDO",
                "Use o serviço correspondente ao tipo da conta."
            );
        }
        return new FiltroContas(
            tipo,
            f.pessoaId(),
            f.categoriaId(),
            f.situacao(),
            f.vencimentoDe(),
            f.vencimentoAte(),
            f.emissaoDe(),
            f.emissaoAte(),
            f.busca()
        );
    }

    public void validarFiltro(FiltroContas f) {
        if (f == null) {
            return;
        }
        if (
            (f.vencimentoDe() != null &&
                f.vencimentoAte() != null &&
                f.vencimentoDe().isAfter(f.vencimentoAte())) ||
            (f.emissaoDe() != null &&
                f.emissaoAte() != null &&
                f.emissaoDe().isAfter(f.emissaoAte()))
        ) {
            throw new ErroNegocio(
                400,
                "PERIODO_INVALIDO",
                "O início do período não pode ser posterior ao fim."
            );
        }
        if (f.busca() != null && f.busca().length() > 180) {
            throw new ErroNegocio(400, "BUSCA_INVALIDA", "Use no máximo 180 caracteres na busca.");
        }
    }

    public Pagina<ContaResponse> listar(FiltroContas f, int pagina, int tamanho) {
        f = escopo(f);
        validarFiltro(f);
        if (pagina < 0 || tamanho < 1 || tamanho > 100) {
            throw new ErroNegocio(
                400,
                "PAGINACAO_INVALIDA",
                "Página deve ser não negativa e tamanho deve ficar entre 1 e 100."
            );
        }
        var page = contas.findAll(
            ContaSpecifications.filtrar(f, hoje()),
            PageRequest.of(pagina, tamanho, Sort.by("vencimento").ascending().and(Sort.by("id")))
        );
        return new Pagina<>(
            respostas(page.getContent()),
            page.getTotalElements(),
            pagina,
            tamanho,
            page.getTotalPages()
        );
    }

    public List<ContaResponse> exportar(FiltroContas f) {
        validarFiltro(f);
        var page = contas.findAll(
            ContaSpecifications.filtrar(f, hoje()),
            PageRequest.of(0, 10001, Sort.by("vencimento", "id"))
        );
        if (page.getTotalElements() > 10000) {
            throw ErroNegocio.regra("Refine os filtros para exportar até 10.000 contas por vez.");
        }
        return respostas(page.getContent());
    }

    private List<ContaResponse> respostas(List<Conta> entities) {
        if (entities.isEmpty()) {
            return List.of();
        }
        var ids = entities.stream().map(Conta::getId).toList();
        var sums = baixas
            .totaisPorConta(ids)
            .stream()
            .collect(
                Collectors.toMap(BaixaRepository.TotaisConta::getContaId, Function.identity())
            );
        var history = new HashSet<>(baixas.contasComHistorico(ids));
        return entities
            .stream()
            .map(c -> resposta(c, sums.get(c.getId()), history.contains(c.getId())))
            .toList();
    }

    public ContaDetalhe obter(Long id) {
        var c = conta(id);
        var list = baixas.findByContaIdOrderByIdAsc(id);
        var sum = baixas.totaisPorConta(List.of(id)).stream().findFirst().orElse(null);
        return new ContaDetalhe(
            resposta(c, sum, !list.isEmpty()),
            list.stream().map(this::resposta).toList()
        );
    }

    @Transactional
    public ContaResponse criar(ContaRequest request) {
        var c = new Conta();
        preencher(c, request);
        c.setCriadoEm(clock.instant());
        return resposta(contas.saveAndFlush(c), null, false);
    }

    public record CriacaoOrigem(ContaResponse conta, boolean repeticao) {}

    @Transactional
    public CriacaoOrigem criarDePrevisao(Long origem, ContaRequest request) {
        if (origem == null || origem <= 0 || request.tipo() != TipoConta.PAGAR) {
            throw ErroNegocio.regra(
                "A conversão deve gerar uma obrigação a pagar com origem válida."
            );
        }
        String hash = hashOrigem(request);
        var existente = contas.findByDespesaOrigemId(origem);
        if (existente.isPresent()) {
            if (!hash.equals(existente.get().getOrigemHash())) {
                throw ErroNegocio.conflito(
                    "Esta origem já recebeu outra solicitação. Repita exatamente os dados originais."
                );
            }
            return new CriacaoOrigem(obter(existente.get().getId()).conta(), true);
        }
        var c = new Conta();
        preencher(c, request);
        c.setDespesaOrigemId(origem);
        c.setOrigemHash(hash);
        c.setCriadoEm(clock.instant());
        return new CriacaoOrigem(resposta(contas.saveAndFlush(c), null, false), false);
    }

    private String hashOrigem(ContaRequest r) {
        try {
            var values = java.util.List.of(
                r.tipo().name(),
                r.pessoaId().toString(),
                r.categoriaId().toString(),
                r.numeroTitulo().strip().toUpperCase(Locale.ROOT),
                r.tipoTitulo().name(),
                r.descricao().strip(),
                r.emissao().toString(),
                r.vencimento().toString(),
                Dinheiro.positivo(r.valor(), "Valor").toPlainString(),
                Dinheiro.naoNegativo(r.descontoMaximo(), "Desconto").toPlainString(),
                Objects.toString(r.validadeDesconto(), ""),
                Objects.toString(RegistroUtils.texto(r.observacoes()), "")
            );
            byte[] json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsBytes(
                values
            );
            return java.util.HexFormat.of().formatHex(
                java.security.MessageDigest.getInstance("SHA-256").digest(json)
            );
        } catch (ErroNegocio e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Falha na identificação da conversão", e);
        }
    }

    @Transactional
    public ContaResponse editar(Long id, ContaRequest r) {
        var c = bloquear(id);
        RegistroUtils.versao(c, r.versao());
        if (c.isCancelada() || baixas.existsByContaId(id)) {
            throw ErroNegocio.conflito(
                "Conta cancelada ou com histórico de baixas não pode ser editada. " +
                    "Preserve o histórico e registre um novo título, se necessário."
            );
        }
        if (
            c.getDespesaOrigemId() != null &&
            (r.tipo() != TipoConta.PAGAR ||
                !r.pessoaId().equals(c.getPessoaId()) ||
                !r.categoriaId().equals(c.getCategoriaId()))
        ) {
            throw ErroNegocio.conflito(
                "Preserve o fornecedor e a categoria da previsão que gerou esta conta."
            );
        }
        preencher(c, r);
        return resposta(contas.saveAndFlush(c), null, false);
    }

    private void preencher(Conta c, ContaRequest r) {
        if (r.tipo() != tipo) {
            throw ErroNegocio.regra("Este serviço não registra contas desse tipo.");
        }
        var pessoa = cadastros.pessoa(r.pessoaId());
        var categoria = cadastros.categoria(r.categoriaId());
        validarVinculos(r.tipo(), pessoa, categoria);
        if (r.emissao().isAfter(r.vencimento())) {
            throw ErroNegocio.regra("Vencimento não pode ser anterior à emissão.");
        }
        var valor = Dinheiro.positivo(r.valor(), "Valor do título");
        var desconto = Dinheiro.naoNegativo(r.descontoMaximo(), "Desconto máximo");
        if (desconto.compareTo(valor) > 0) {
            throw ErroNegocio.regra("Desconto máximo não pode ultrapassar o valor do título.");
        }
        if (
            desconto.signum() > 0 &&
            (r.validadeDesconto() == null || r.validadeDesconto().isBefore(r.emissao()))
        ) {
            throw ErroNegocio.regra(
                "Informe a validade do desconto, igual ou posterior à emissão."
            );
        }
        String numero = r.numeroTitulo().strip().toUpperCase(Locale.ROOT);
        if (
            contas.existsByTipoAndPessoaIdAndNumeroTituloIgnoreCaseAndIdNot(
                r.tipo(),
                pessoa.id(),
                numero,
                c.getId() == null ? -1L : c.getId()
            )
        ) {
            throw ErroNegocio.conflito("Já existe um título com este número, tipo e pessoa.");
        }
        c.setTipo(r.tipo());
        c.setPessoaId(pessoa.id());
        c.setPessoaNome(pessoa.nome());
        c.setCategoriaId(categoria.id());
        c.setCategoriaVersao(categoria.versao());
        c.setCategoriaCodigo(categoria.codigo());
        c.setCategoriaNome(categoria.nome());
        c.setCategoriaNatureza(categoria.natureza());
        c.setCategoriaAtivo(categoria.ativo());
        c.setNumeroTitulo(numero);
        c.setTipoTitulo(r.tipoTitulo());
        c.setDescricao(r.descricao().strip());
        c.setEmissao(r.emissao());
        c.setVencimento(r.vencimento());
        c.setValor(valor);
        c.setDescontoMaximo(desconto);
        c.setValidadeDesconto(desconto.signum() > 0 ? r.validadeDesconto() : null);
        c.setObservacoes(RegistroUtils.texto(r.observacoes()));
    }

    public void validarVinculos(TipoConta tipo, PessoaResponse p, CategoriaResponse c) {
        if (!p.ativo() || !c.ativo()) {
            throw ErroNegocio.regra("Selecione uma pessoa e uma categoria ativas.");
        }
        if (
            (tipo == TipoConta.PAGAR && p.tipo() == TipoPessoa.CLIENTE) ||
            (tipo == TipoConta.RECEBER && p.tipo() == TipoPessoa.FORNECEDOR)
        ) {
            throw ErroNegocio.regra(
                "Contas a pagar exigem fornecedor; contas a receber exigem cliente. O tipo AMBOS atende às duas operações."
            );
        }
        if (
            (tipo == TipoConta.PAGAR && c.natureza() != NaturezaCategoria.DESPESA) ||
            (tipo == TipoConta.RECEBER && c.natureza() != NaturezaCategoria.RECEITA)
        ) {
            throw ErroNegocio.regra("A natureza da categoria deve corresponder ao tipo da conta.");
        }
    }

    @Transactional
    public void excluir(Long id) {
        var c = bloquear(id);
        if (baixas.existsByContaId(id)) {
            throw ErroNegocio.conflito(
                "Conta com histórico não pode ser excluída. Use cancelamento após estornar as baixas."
            );
        }
        if (c.getDespesaOrigemId() != null) {
            throw ErroNegocio.conflito(
                "Esta conta foi gerada por uma previsão. Use cancelamento para preservar a origem."
            );
        }
        contas.delete(c);
        contas.flush();
    }

    @Transactional
    public ContaResponse cancelar(Long id, MotivoRequest r) {
        var c = bloquear(id);
        if (c.isCancelada()) {
            return obter(id).conta();
        }
        var total = baixas
            .findByContaIdOrderByIdAsc(id)
            .stream()
            .filter(b -> !b.isEstornada())
            .map(Baixa::getPrincipal)
            .reduce(Dinheiro.ZERO, BigDecimal::add);
        if (total.signum() > 0) {
            throw ErroNegocio.conflito("Estorne as baixas ativas antes de cancelar a conta.");
        }
        c.setCancelada(true);
        c.setMotivoCancelamento(r.motivo().strip());
        c.setCanceladaEm(clock.instant());
        contas.saveAndFlush(c);
        return obter(id).conta();
    }

    @Transactional
    public ResultadoBaixa baixar(Long id, String rawKey, BaixaRequest r) {
        var c = bloquear(id);
        String key;
        try {
            key = UUID.fromString(rawKey).toString();
            if (!key.equalsIgnoreCase(rawKey)) {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            throw new ErroNegocio(
                400,
                "IDEMPOTENCIA_INVALIDA",
                "Idempotency-Key deve conter um UUID completo."
            );
        }
        var existente = baixas.findByChaveIdempotencia(key);
        if (existente.isPresent()) {
            var b = existente.get();
            if (!mesmaSolicitacao(b, c, r)) {
                throw ErroNegocio.conflito(
                    "A chave de idempotência já foi usada em outra solicitação. Não altere os dados de uma repetição."
                );
            }
            return new ResultadoBaixa(resposta(b), true);
        }
        if (c.isCancelada()) {
            throw ErroNegocio.conflito("Conta cancelada não admite pagamento ou recebimento.");
        }
        if (r.data().isBefore(c.getEmissao()) || r.data().isAfter(hoje())) {
            throw ErroNegocio.regra("A data da baixa deve ficar entre a emissão e a data atual.");
        }
        var list = baixas
            .findByContaIdOrderByIdAsc(id)
            .stream()
            .filter(b -> !b.isEstornada())
            .toList();
        var principalBaixado = list
            .stream()
            .map(Baixa::getPrincipal)
            .reduce(Dinheiro.ZERO, BigDecimal::add);
        var descontoUsado = list
            .stream()
            .map(Baixa::getDesconto)
            .reduce(Dinheiro.ZERO, BigDecimal::add);
        var principal = Dinheiro.positivo(r.principal(), "Principal da baixa");
        var acrescimo = Dinheiro.naoNegativo(r.acrescimo(), "Acréscimo");
        var desconto = Dinheiro.naoNegativo(r.desconto(), "Desconto");
        if (principal.compareTo(c.saldo(principalBaixado)) > 0) {
            throw ErroNegocio.conflito("O principal informado excede o saldo em aberto da conta.");
        }
        if (
            desconto.signum() > 0 &&
            (c.getValidadeDesconto() == null || r.data().isAfter(c.getValidadeDesconto()))
        ) {
            throw ErroNegocio.regra("O desconto não está disponível na data da baixa.");
        }
        if (descontoUsado.add(desconto).compareTo(c.getDescontoMaximo()) > 0) {
            throw ErroNegocio.regra("O desconto acumulado excede o limite do título.");
        }
        if (principal.add(acrescimo).subtract(desconto).signum() <= 0) {
            throw ErroNegocio.regra("O valor efetivamente movimentado deve ser maior que zero.");
        }
        ContaBancariaResponse bancaria =
            r.contaBancariaId() == null ? null : cadastros.contaBancaria(r.contaBancariaId());
        if (
            bancaria != null &&
            (!bancaria.ativo() ||
                !bancaria.agencia().ativo() ||
                !bancaria.agencia().banco().ativo())
        ) {
            throw ErroNegocio.regra("A conta bancária, a agência e o banco devem estar ativos.");
        }
        if (
            bancaria == null &&
            Set.of(
                FormaPagamento.PIX,
                FormaPagamento.PIX_AUTOMATICO,
                FormaPagamento.TRANSFERENCIA,
                FormaPagamento.DEBITO_AUTOMATICO,
                FormaPagamento.CHEQUE
            ).contains(r.forma())
        ) {
            throw ErroNegocio.regra(
                "Pix, Pix Automático, transferência, débito automático e cheque exigem uma conta bancária."
            );
        }
        var b = new Baixa();
        b.setConta(c);
        b.setContaBancariaId(bancaria == null ? null : bancaria.id());
        b.setContaBancariaDescricao(
            bancaria == null ? null : bancaria.descricao() + " / " + bancaria.numero()
        );
        b.setChaveIdempotencia(key);
        b.setData(r.data());
        b.setPrincipal(principal);
        b.setAcrescimo(acrescimo);
        b.setDesconto(desconto);
        b.setForma(r.forma());
        b.setObservacoes(RegistroUtils.texto(r.observacoes()));
        b.setCriadoEm(clock.instant());
        return new ResultadoBaixa(resposta(baixas.saveAndFlush(b)), false);
    }

    private boolean mesmaSolicitacao(Baixa b, Conta c, BaixaRequest r) {
        return (
            b.getConta().getId().equals(c.getId()) &&
            b.getData().equals(r.data()) &&
            b.getPrincipal().compareTo(Dinheiro.valor(r.principal())) == 0 &&
            b.getAcrescimo().compareTo(Dinheiro.valor(r.acrescimo())) == 0 &&
            b.getDesconto().compareTo(Dinheiro.valor(r.desconto())) == 0 &&
            b.getForma() == r.forma() &&
            Objects.equals(b.getContaBancariaId(), r.contaBancariaId()) &&
            Objects.equals(b.getObservacoes(), RegistroUtils.texto(r.observacoes()))
        );
    }

    @Transactional
    public BaixaResponse estornar(Long id, Long baixaId, MotivoRequest r) {
        bloquear(id);
        var b = baixas.findById(baixaId).orElseThrow(() -> ErroNegocio.ausente("Baixa"));
        if (!b.getConta().getId().equals(id)) {
            throw ErroNegocio.ausente("Baixa desta conta");
        }
        if (!b.isEstornada()) {
            b.setEstornada(true);
            b.setMotivoEstorno(r.motivo().strip());
            b.setEstornadaEm(clock.instant());
            baixas.saveAndFlush(b);
        }
        return resposta(b);
    }

    private ContaResponse resposta(Conta c, BaixaRepository.TotaisConta t, boolean historico) {
        var principal = t == null ? Dinheiro.ZERO : t.getPrincipal();
        var desconto = t == null ? Dinheiro.ZERO : t.getDesconto();
        var acrescimo = t == null ? Dinheiro.ZERO : t.getAcrescimo();
        return new ContaResponse(
            c.getId(),
            c.getVersao(),
            c.getTipo(),
            new Referencia(c.getPessoaId(), c.getPessoaNome()),
            new CategoriaResponse(
                c.getCategoriaId(),
                c.getCategoriaVersao(),
                c.getCategoriaCodigo(),
                c.getCategoriaNome(),
                c.getCategoriaNatureza(),
                c.getCategoriaAtivo()
            ),
            c.getDespesaOrigemId(),
            c.getNumeroTitulo(),
            c.getTipoTitulo(),
            c.getDescricao(),
            c.getEmissao(),
            c.getVencimento(),
            c.getValor(),
            c.getDescontoMaximo(),
            c.getValidadeDesconto(),
            c.getObservacoes(),
            principal,
            desconto,
            acrescimo,
            principal.add(acrescimo).subtract(desconto),
            c.saldo(principal),
            c.situacaoEm(hoje(), principal),
            historico,
            c.getMotivoCancelamento(),
            c.getCriadoEm()
        );
    }

    public BaixaResponse resposta(Baixa b) {
        return new BaixaResponse(
            b.getId(),
            b.getConta().getId(),
            b.getData(),
            b.getPrincipal(),
            b.getAcrescimo(),
            b.getDesconto(),
            b.valorMovimentado(),
            b.getForma(),
            b.getContaBancariaId() == null
                ? null
                : new Referencia(b.getContaBancariaId(), b.getContaBancariaDescricao()),
            b.getChaveIdempotencia(),
            b.getObservacoes(),
            b.isEstornada(),
            b.getMotivoEstorno(),
            b.getEstornadaEm(),
            b.getCriadoEm()
        );
    }
}
