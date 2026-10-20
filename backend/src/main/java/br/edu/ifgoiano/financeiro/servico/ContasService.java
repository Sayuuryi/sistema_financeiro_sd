package br.edu.ifgoiano.financeiro.servico;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifgoiano.financeiro.api.Dtos.*;
import br.edu.ifgoiano.financeiro.api.Dtos.BaixaRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.BaixaResponse;
import br.edu.ifgoiano.financeiro.api.Dtos.ContaDetalhe;
import br.edu.ifgoiano.financeiro.api.Dtos.ContaRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.ContaResponse;
import br.edu.ifgoiano.financeiro.api.Dtos.FiltroContas;
import br.edu.ifgoiano.financeiro.api.Dtos.MotivoRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.Pagina;
import br.edu.ifgoiano.financeiro.api.Dtos.Referencia;
import br.edu.ifgoiano.financeiro.api.Dtos.ResultadoBaixa;
import br.edu.ifgoiano.financeiro.dominio.Baixa;
import br.edu.ifgoiano.financeiro.dominio.Categoria;
import br.edu.ifgoiano.financeiro.dominio.Conta;
import br.edu.ifgoiano.financeiro.dominio.ContaBancaria;
import br.edu.ifgoiano.financeiro.dominio.DespesaPrevista;
import br.edu.ifgoiano.financeiro.dominio.FormaPagamento;
import br.edu.ifgoiano.financeiro.dominio.NaturezaCategoria;
import br.edu.ifgoiano.financeiro.dominio.Pessoa;
import br.edu.ifgoiano.financeiro.dominio.TipoConta;
import br.edu.ifgoiano.financeiro.dominio.TipoPessoa;
import br.edu.ifgoiano.financeiro.erro.ErroNegocio;
import br.edu.ifgoiano.financeiro.repositorio.BaixaRepository;
import br.edu.ifgoiano.financeiro.repositorio.ContaRepository;
import br.edu.ifgoiano.financeiro.repositorio.ContaSpecifications;

@Service
@Transactional(readOnly = true)
public class ContasService {
    private final ContaRepository contas;
    private final BaixaRepository baixas;
    private final CadastrosService cadastros;
    private final Clock clock;

    public ContasService(ContaRepository contas, BaixaRepository baixas, CadastrosService cadastros, Clock clock) {
        this.contas = contas;
        this.baixas = baixas;
        this.cadastros = cadastros;
        this.clock = clock;
    }

    public LocalDate hoje() {
        return LocalDate.now(clock);
    }

    public Conta conta(Long id) {
        return contas.findById(id).orElseThrow(() -> ErroNegocio.ausente("Conta"));
    }

    private Conta bloquear(Long id) {
        return contas.findForUpdate(id).orElseThrow(() -> ErroNegocio.ausente("Conta"));
    }

    public void validarFiltro(FiltroContas f) {
        if (f == null)
            return;
        if ((f.vencimentoDe() != null && f.vencimentoAte() != null && f.vencimentoDe().isAfter(f.vencimentoAte())) ||
                (f.emissaoDe() != null && f.emissaoAte() != null && f.emissaoDe().isAfter(f.emissaoAte())))
            throw new ErroNegocio(400, "PERIODO_INVALIDO", "O início do período não pode ser posterior ao fim.");
        if (f.busca() != null && f.busca().length() > 180)
            throw new ErroNegocio(400, "BUSCA_INVALIDA", "Use no máximo 180 caracteres na busca.");
    }

    public Pagina<ContaResponse> listar(FiltroContas f, int pagina, int tamanho) {
        validarFiltro(f);
        if (pagina < 0 || tamanho < 1 || tamanho > 100)
            throw new ErroNegocio(400, "PAGINACAO_INVALIDA",
                    "Página deve ser não negativa e tamanho deve ficar entre 1 e 100.");
        var page = contas.findAll(ContaSpecifications.filtrar(f, hoje()),
                PageRequest.of(pagina, tamanho, Sort.by("vencimento").ascending().and(Sort.by("id"))));
        return new Pagina<>(respostas(page.getContent()), page.getTotalElements(), pagina, tamanho,
                page.getTotalPages());
    }

    public List<ContaResponse> exportar(FiltroContas f) {
        validarFiltro(f);
        var page = contas.findAll(ContaSpecifications.filtrar(f, hoje()),
                PageRequest.of(0, 10001, Sort.by("vencimento", "id")));
        if (page.getTotalElements() > 10000)
            throw ErroNegocio.regra("Refine os filtros para exportar até 10.000 contas por vez.");
        return respostas(page.getContent());
    }

    private List<ContaResponse> respostas(List<Conta> entities) {
        if (entities.isEmpty())
            return List.of();
        var ids = entities.stream().map(Conta::getId).toList();
        var sums = baixas.totaisPorConta(ids).stream()
                .collect(Collectors.toMap(BaixaRepository.TotaisConta::getContaId, Function.identity()));
        var history = new HashSet<>(baixas.contasComHistorico(ids));
        return entities.stream().map(c -> resposta(c, sums.get(c.getId()), history.contains(c.getId()))).toList();
    }

    public ContaDetalhe obter(Long id) {
        var c = conta(id);
        var list = baixas.findByContaIdOrderByIdAsc(id);
        var sum = baixas.totaisPorConta(List.of(id)).stream().findFirst().orElse(null);
        return new ContaDetalhe(resposta(c, sum, !list.isEmpty()), list.stream().map(this::resposta).toList());
    }

    @Transactional
    public ContaResponse criar(ContaRequest request) {
        return criar(request, null);
    }

    @Transactional
    public ContaResponse criarDeDespesa(ContaRequest request, DespesaPrevista origem) {
        return criar(request, origem);
    }

    private ContaResponse criar(ContaRequest r, DespesaPrevista origem) {
        var c = new Conta();
        preencher(c, r);
        c.setDespesaOrigem(origem);
        c.setCriadoEm(clock.instant());
        return resposta(contas.saveAndFlush(c), null, false);
    }

    @Transactional
    public ContaResponse editar(Long id, ContaRequest r) {
        var c = bloquear(id);
        CadastrosService.versao(c, r.versao());
        if (c.isCancelada() || baixas.existsByContaId(id))
            throw ErroNegocio.conflito(
                    "Conta cancelada ou com histórico de baixas não pode ser editada. Preserve o histórico e registre um novo título, se necessário.");
        if (c.getDespesaOrigem() != null && (r.tipo() != TipoConta.PAGAR || !r.pessoaId().equals(c.getPessoa().getId())
                || !r.categoriaId().equals(c.getCategoria().getId())))
            throw ErroNegocio.conflito("Preserve o fornecedor e a categoria da previsão que gerou esta conta.");
        preencher(c, r);
        return resposta(contas.saveAndFlush(c), null, false);
    }

    private void preencher(Conta c, ContaRequest r) {
        var pessoa = cadastros.pessoa(r.pessoaId());
        var categoria = cadastros.categoria(r.categoriaId());
        validarVinculos(r.tipo(), pessoa, categoria);
        if (r.emissao().isAfter(r.vencimento()))
            throw ErroNegocio.regra("Vencimento não pode ser anterior à emissão.");
        var valor = Dinheiro.positivo(r.valor(), "Valor do título");
        var desconto = Dinheiro.naoNegativo(r.descontoMaximo(), "Desconto máximo");
        if (desconto.compareTo(valor) > 0)
            throw ErroNegocio.regra("Desconto máximo não pode ultrapassar o valor do título.");
        if (desconto.signum() > 0 && (r.validadeDesconto() == null || r.validadeDesconto().isBefore(r.emissao())))
            throw ErroNegocio.regra("Informe a validade do desconto, igual ou posterior à emissão.");
        String numero = r.numeroTitulo().strip().toUpperCase(Locale.ROOT);
        if (contas.existsByTipoAndPessoaIdAndNumeroTituloIgnoreCaseAndIdNot(r.tipo(), pessoa.getId(), numero,
                c.getId() == null ? -1L : c.getId()))
            throw ErroNegocio.conflito("Já existe um título com este número, tipo e pessoa.");
        c.setTipo(r.tipo());
        c.setPessoa(pessoa);
        c.setCategoria(categoria);
        c.setNumeroTitulo(numero);
        c.setTipoTitulo(r.tipoTitulo());
        c.setDescricao(r.descricao().strip());
        c.setEmissao(r.emissao());
        c.setVencimento(r.vencimento());
        c.setValor(valor);
        c.setDescontoMaximo(desconto);
        c.setValidadeDesconto(desconto.signum() > 0 ? r.validadeDesconto() : null);
        c.setObservacoes(CadastrosService.texto(r.observacoes()));
    }

    public void validarVinculos(TipoConta tipo, Pessoa p, Categoria c) {
        if (!p.isAtivo() || !c.isAtivo())
            throw ErroNegocio.regra("Selecione uma pessoa e uma categoria ativas.");
        if ((tipo == TipoConta.PAGAR && p.getTipo() == TipoPessoa.CLIENTE)
                || (tipo == TipoConta.RECEBER && p.getTipo() == TipoPessoa.FORNECEDOR))
            throw ErroNegocio.regra(
                    "Contas a pagar exigem fornecedor; contas a receber exigem cliente. O tipo AMBOS atende às duas operações.");
        if ((tipo == TipoConta.PAGAR && c.getNatureza() != NaturezaCategoria.DESPESA)
                || (tipo == TipoConta.RECEBER && c.getNatureza() != NaturezaCategoria.RECEITA))
            throw ErroNegocio.regra("A natureza da categoria deve corresponder ao tipo da conta.");
    }

    @Transactional
    public void excluir(Long id) {
        var c = bloquear(id);
        if (baixas.existsByContaId(id))
            throw ErroNegocio
                    .conflito("Conta com histórico não pode ser excluída. Use cancelamento após estornar as baixas.");
        if (c.getDespesaOrigem() != null)
            throw ErroNegocio
                    .conflito("Esta conta foi gerada por uma previsão. Use cancelamento para preservar a origem.");
        contas.delete(c);
        contas.flush();
    }

    @Transactional
    public ContaResponse cancelar(Long id, MotivoRequest r) {
        var c = bloquear(id);
        if (c.isCancelada())
            return obter(id).conta();
        var total = baixas.findByContaIdOrderByIdAsc(id).stream().filter(b -> !b.isEstornada()).map(Baixa::getPrincipal)
                .reduce(Dinheiro.ZERO, BigDecimal::add);
        if (total.signum() > 0)
            throw ErroNegocio.conflito("Estorne as baixas ativas antes de cancelar a conta.");
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
            if (!key.equalsIgnoreCase(rawKey))
                throw new IllegalArgumentException();
        } catch (Exception e) {
            throw new ErroNegocio(400, "IDEMPOTENCIA_INVALIDA", "Idempotency-Key deve conter um UUID completo.");
        }
        var existente = baixas.findByChaveIdempotencia(key);
        if (existente.isPresent()) {
            var b = existente.get();
            if (!mesmaSolicitacao(b, c, r))
                throw ErroNegocio.conflito(
                        "A chave de idempotência já foi usada em outra solicitação. Não altere os dados de uma repetição.");
            return new ResultadoBaixa(resposta(b), true);
        }
        if (c.isCancelada())
            throw ErroNegocio.conflito("Conta cancelada não admite pagamento ou recebimento.");
        if (r.data().isBefore(c.getEmissao()) || r.data().isAfter(hoje()))
            throw ErroNegocio.regra("A data da baixa deve ficar entre a emissão e a data atual.");
        var list = baixas.findByContaIdOrderByIdAsc(id).stream().filter(b -> !b.isEstornada()).toList();
        var principalBaixado = list.stream().map(Baixa::getPrincipal).reduce(Dinheiro.ZERO, BigDecimal::add);
        var descontoUsado = list.stream().map(Baixa::getDesconto).reduce(Dinheiro.ZERO, BigDecimal::add);
        var principal = Dinheiro.positivo(r.principal(), "Principal da baixa");
        var acrescimo = Dinheiro.naoNegativo(r.acrescimo(), "Acréscimo");
        var desconto = Dinheiro.naoNegativo(r.desconto(), "Desconto");
        if (principal.compareTo(c.saldo(principalBaixado)) > 0)
            throw ErroNegocio.conflito("O principal informado excede o saldo em aberto da conta.");
        if (desconto.signum() > 0 && (c.getValidadeDesconto() == null || r.data().isAfter(c.getValidadeDesconto())))
            throw ErroNegocio.regra("O desconto não está disponível na data da baixa.");
        if (descontoUsado.add(desconto).compareTo(c.getDescontoMaximo()) > 0)
            throw ErroNegocio.regra("O desconto acumulado excede o limite do título.");
        if (principal.add(acrescimo).subtract(desconto).signum() <= 0)
            throw ErroNegocio.regra("O valor efetivamente movimentado deve ser maior que zero.");
        ContaBancaria bancaria = r.contaBancariaId() == null ? null : cadastros.contaBancaria(r.contaBancariaId());
        if (bancaria != null && (!bancaria.isAtivo() || !bancaria.getAgencia().isAtivo()
                || !bancaria.getAgencia().getBanco().isAtivo()))
            throw ErroNegocio.regra("A conta bancária, a agência e o banco devem estar ativos.");
        if (bancaria == null
                && Set.of(FormaPagamento.PIX, FormaPagamento.TRANSFERENCIA, FormaPagamento.CHEQUE).contains(r.forma()))
            throw ErroNegocio.regra("PIX, transferência e cheque exigem uma conta bancária.");
        var b = new Baixa();
        b.setConta(c);
        b.setContaBancaria(bancaria);
        b.setChaveIdempotencia(key);
        b.setData(r.data());
        b.setPrincipal(principal);
        b.setAcrescimo(acrescimo);
        b.setDesconto(desconto);
        b.setForma(r.forma());
        b.setObservacoes(CadastrosService.texto(r.observacoes()));
        b.setCriadoEm(clock.instant());
        return new ResultadoBaixa(resposta(baixas.saveAndFlush(b)), false);
    }

    private boolean mesmaSolicitacao(Baixa b, Conta c, BaixaRequest r) {
        return b.getConta().getId().equals(c.getId()) && b.getData().equals(r.data())
                && b.getPrincipal().compareTo(Dinheiro.valor(r.principal())) == 0
                && b.getAcrescimo().compareTo(Dinheiro.valor(r.acrescimo())) == 0
                && b.getDesconto().compareTo(Dinheiro.valor(r.desconto())) == 0 && b.getForma() == r.forma()
                && Objects.equals(b.getContaBancaria() == null ? null : b.getContaBancaria().getId(),
                        r.contaBancariaId())
                && Objects.equals(b.getObservacoes(), CadastrosService.texto(r.observacoes()));
    }

    @Transactional
    public BaixaResponse estornar(Long id, Long baixaId, MotivoRequest r) {
        bloquear(id);
        var b = baixas.findById(baixaId).orElseThrow(() -> ErroNegocio.ausente("Baixa"));
        if (!b.getConta().getId().equals(id))
            throw ErroNegocio.ausente("Baixa desta conta");
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
        return new ContaResponse(c.getId(), c.getVersao(), c.getTipo(),
                new Referencia(c.getPessoa().getId(), c.getPessoa().getNome()), cadastros.resposta(c.getCategoria()),
                c.getDespesaOrigem() == null ? null : c.getDespesaOrigem().getId(), c.getNumeroTitulo(),
                c.getTipoTitulo(), c.getDescricao(), c.getEmissao(), c.getVencimento(),
                c.getValor(), c.getDescontoMaximo(), c.getValidadeDesconto(), c.getObservacoes(), principal, desconto,
                acrescimo, principal.add(acrescimo).subtract(desconto), c.saldo(principal),
                c.situacaoEm(hoje(), principal), historico, c.getMotivoCancelamento(), c.getCriadoEm());
    }

    public BaixaResponse resposta(Baixa b) {
        var contaBanco = b.getContaBancaria();
        return new BaixaResponse(b.getId(), b.getConta().getId(), b.getData(), b.getPrincipal(), b.getAcrescimo(),
                b.getDesconto(), b.valorMovimentado(), b.getForma(),
                contaBanco == null ? null
                        : new Referencia(contaBanco.getId(),
                                contaBanco.getDescricao() + " / " + contaBanco.getNumero()),
                b.getChaveIdempotencia(), b.getObservacoes(),
                b.isEstornada(), b.getMotivoEstorno(), b.getEstornadaEm(), b.getCriadoEm());
    }
}
