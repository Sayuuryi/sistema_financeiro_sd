package br.edu.fluxo.planejamento.api;

import br.edu.fluxo.planejamento.dominio.*;
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

    public record DespesaRequest(
        @NotNull @Positive Long pessoaId,
        @NotNull @Positive Long categoriaId,
        @NotBlank @Size(max = 180) String descricao,
        @Size(max = 60) String referencia,
        @NotNull LocalDate dataPrevista,
        @NotNull @DecimalMin("0.01") @Digits(integer = 15, fraction = 2) BigDecimal valorPrevisto,
        @Size(max = 1500) String observacoes,
        Boolean ativo,
        Long versao
    ) {}

    public record DespesaResponse(
        Long id,
        long versao,
        Referencia pessoa,
        CategoriaResponse categoria,
        String descricao,
        String referencia,
        LocalDate dataPrevista,
        BigDecimal valorPrevisto,
        String observacoes,
        boolean ativo,
        Long contaGeradaId,
        String situacao
    ) {}

    public record GerarContaRequest(
        @NotBlank @Size(max = 60) String numeroTitulo,
        @NotNull TipoTitulo tipoTitulo,
        @NotNull LocalDate emissao,
        @NotNull LocalDate vencimento,
        @NotNull @DecimalMin("0.01") @Digits(integer = 15, fraction = 2) BigDecimal valor,
        @DecimalMin("0.00") @Digits(integer = 15, fraction = 2) BigDecimal descontoMaximo,
        LocalDate validadeDesconto,
        @Size(max = 1500) String observacoes
    ) {}
}
