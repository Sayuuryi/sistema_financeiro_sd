package br.edu.fluxo.receber.api;

import br.edu.fluxo.receber.dominio.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

public final class Dtos {

    private Dtos() {}

    public record Referencia(Long id, String nome) {}

    public record PessoaResponse(
        Long id,
        long versao,
        String nome,
        TipoPessoa tipo,
        String documento,
        String email,
        String telefone,
        String contato,
        String endereco,
        String cidade,
        String estado,
        String cep,
        boolean ativo
    ) {}

    public record CategoriaResponse(
        Long id,
        long versao,
        String codigo,
        String nome,
        NaturezaCategoria natureza,
        boolean ativo
    ) {}

    public record BancoResponse(Long id, long versao, String codigo, String nome, boolean ativo) {}

    public record AgenciaResponse(
        Long id,
        long versao,
        BancoResponse banco,
        String numero,
        String nome,
        boolean ativo
    ) {}

    public record ContaBancariaResponse(
        Long id,
        long versao,
        AgenciaResponse agencia,
        String numero,
        String descricao,
        BigDecimal saldoInicial,
        BigDecimal saldoAtual,
        boolean ativo
    ) {}

    public record ContaRequest(
        @NotNull TipoConta tipo,
        @NotNull @Positive Long pessoaId,
        @NotNull @Positive Long categoriaId,
        @NotBlank @Size(max = 60) String numeroTitulo,
        @NotNull TipoTitulo tipoTitulo,
        @NotBlank @Size(max = 180) String descricao,
        @NotNull LocalDate emissao,
        @NotNull LocalDate vencimento,
        @NotNull @DecimalMin("0.01") @Digits(integer = 15, fraction = 2) BigDecimal valor,
        @DecimalMin("0.00") @Digits(integer = 15, fraction = 2) BigDecimal descontoMaximo,
        LocalDate validadeDesconto,
        @Size(max = 1500) String observacoes,
        Long versao
    ) {}

    public record ContaResponse(
        Long id,
        long versao,
        TipoConta tipo,
        Referencia pessoa,
        CategoriaResponse categoria,
        Long despesaOrigemId,
        String numeroTitulo,
        TipoTitulo tipoTitulo,
        String descricao,
        LocalDate emissao,
        LocalDate vencimento,
        BigDecimal valor,
        BigDecimal descontoMaximo,
        LocalDate validadeDesconto,
        String observacoes,
        BigDecimal totalBaixado,
        BigDecimal totalDesconto,
        BigDecimal totalAcrescimo,
        BigDecimal totalMovimentado,
        BigDecimal saldo,
        SituacaoConta situacao,
        boolean possuiHistorico,
        String motivoCancelamento,
        Instant criadoEm
    ) {}

    public record BaixaRequest(
        @NotNull LocalDate data,
        @NotNull @DecimalMin("0.01") @Digits(integer = 15, fraction = 2) BigDecimal principal,
        @DecimalMin("0.00") @Digits(integer = 15, fraction = 2) BigDecimal acrescimo,
        @DecimalMin("0.00") @Digits(integer = 15, fraction = 2) BigDecimal desconto,
        @NotNull FormaPagamento forma,
        @Positive Long contaBancariaId,
        @Size(max = 1000) String observacoes
    ) {}

    public record BaixaResponse(
        Long id,
        Long contaId,
        LocalDate data,
        BigDecimal principal,
        BigDecimal acrescimo,
        BigDecimal desconto,
        BigDecimal valorMovimentado,
        FormaPagamento forma,
        Referencia contaBancaria,
        String chaveIdempotencia,
        String observacoes,
        boolean estornada,
        String motivoEstorno,
        Instant estornadaEm,
        Instant criadoEm
    ) {}

    public record ContaDetalhe(ContaResponse conta, List<BaixaResponse> historico) {}

    public record ResultadoBaixa(BaixaResponse baixa, boolean repeticao) {}

    public record MotivoRequest(@NotBlank @Size(min = 5, max = 250) String motivo) {}

    public record FiltroContas(
        TipoConta tipo,
        Long pessoaId,
        Long categoriaId,
        SituacaoConta situacao,
        LocalDate vencimentoDe,
        LocalDate vencimentoAte,
        LocalDate emissaoDe,
        LocalDate emissaoAte,
        String busca
    ) {}

    public record Pagina<T>(List<T> itens, long total, int pagina, int tamanho, int totalPaginas) {}

    public record ResumoFinanceiro(
        LocalDate inicio,
        LocalDate fim,
        BigDecimal aPagar,
        BigDecimal aReceber,
        BigDecimal pago,
        BigDecimal recebido,
        BigDecimal saldoMovimentado,
        BigDecimal despesasPrevistas,
        long vencidas
    ) {}

    public record MovimentoResponse(
        Long id,
        Long contaId,
        TipoConta tipo,
        String numeroTitulo,
        String descricao,
        String pessoa,
        LocalDate data,
        BigDecimal principal,
        BigDecimal acrescimo,
        BigDecimal desconto,
        BigDecimal valorMovimentado,
        FormaPagamento forma,
        String contaBancaria
    ) {}

    public record SaldoBancario(Long contaBancariaId, BigDecimal movimentoLiquido) {}
}
