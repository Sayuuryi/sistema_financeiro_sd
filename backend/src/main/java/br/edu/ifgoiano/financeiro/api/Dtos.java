package br.edu.ifgoiano.financeiro.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import br.edu.ifgoiano.financeiro.dominio.FormaPagamento;
import br.edu.ifgoiano.financeiro.dominio.NaturezaCategoria;
import br.edu.ifgoiano.financeiro.dominio.SituacaoConta;
import br.edu.ifgoiano.financeiro.dominio.TipoConta;
import br.edu.ifgoiano.financeiro.dominio.TipoPessoa;
import br.edu.ifgoiano.financeiro.dominio.TipoTitulo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public final class Dtos {
    private Dtos() {
    }

    public record PessoaRequest(
            @NotBlank @Size(max = 120) String nome, @NotNull TipoPessoa tipo,
            @Pattern(regexp = "(?:[0-9]{11}|[0-9]{14})?", message = "informe 11 ou 14 dígitos, sem pontuação") String documento,
            @Email @Size(max = 150) String email, @Size(max = 30) String telefone,
            @Size(max = 100) String contato, @Size(max = 200) String endereco, @Size(max = 80) String cidade,
            @Pattern(regexp = "(?:[A-Za-z]{2})?", message = "use duas letras") String estado,
            @Pattern(regexp = "(?:[0-9]{5}-?[0-9]{3})?", message = "CEP inválido") String cep,
            Boolean ativo, Long versao) {
    }

    public record PessoaResponse(Long id, long versao, String nome, TipoPessoa tipo, String documento, String email,
            String telefone, String contato, String endereco, String cidade, String estado, String cep, boolean ativo) {
    }

    public record CategoriaRequest(@NotBlank @Size(max = 20) String codigo, @NotBlank @Size(max = 100) String nome,
            @NotNull NaturezaCategoria natureza, Boolean ativo, Long versao) {
    }

    public record CategoriaResponse(Long id, long versao, String codigo, String nome, NaturezaCategoria natureza,
            boolean ativo) {
    }

    public record BancoRequest(@NotBlank @Size(max = 10) String codigo, @NotBlank @Size(max = 120) String nome,
            Boolean ativo, Long versao) {
    }

    public record BancoResponse(Long id, long versao, String codigo, String nome, boolean ativo) {
    }

    public record AgenciaRequest(@NotNull @Positive Long bancoId, @NotBlank @Size(max = 20) String numero,
            @NotBlank @Size(max = 120) String nome, Boolean ativo, Long versao) {
    }

    public record AgenciaResponse(Long id, long versao, BancoResponse banco, String numero, String nome,
            boolean ativo) {
    }

    public record ContaBancariaRequest(@NotNull @Positive Long agenciaId, @NotBlank @Size(max = 30) String numero,
            @NotBlank @Size(max = 120) String descricao, @Digits(integer = 15, fraction = 2) BigDecimal saldoInicial,
            Boolean ativo, Long versao) {
    }

    public record ContaBancariaResponse(Long id, long versao, AgenciaResponse agencia, String numero,
            String descricao, BigDecimal saldoInicial, BigDecimal saldoAtual, boolean ativo) {
    }

    public record Referencia(Long id, String nome) {
    }

    public record DespesaRequest(@NotNull @Positive Long pessoaId, @NotNull @Positive Long categoriaId,
            @NotBlank @Size(max = 180) String descricao, @Size(max = 60) String referencia,
            @NotNull LocalDate dataPrevista,
            @NotNull @DecimalMin("0.01") @Digits(integer = 15, fraction = 2) BigDecimal valorPrevisto,
            @Size(max = 1500) String observacoes, Boolean ativo, Long versao) {
    }

    public record DespesaResponse(Long id, long versao, Referencia pessoa, CategoriaResponse categoria,
            String descricao, String referencia, LocalDate dataPrevista, BigDecimal valorPrevisto, String observacoes,
            boolean ativo, Long contaGeradaId, String situacao) {
    }

    public record ContaRequest(@NotNull TipoConta tipo, @NotNull @Positive Long pessoaId,
            @NotNull @Positive Long categoriaId, @NotBlank @Size(max = 60) String numeroTitulo,
            @NotNull TipoTitulo tipoTitulo, @NotBlank @Size(max = 180) String descricao,
            @NotNull LocalDate emissao, @NotNull LocalDate vencimento,
            @NotNull @DecimalMin("0.01") @Digits(integer = 15, fraction = 2) BigDecimal valor,
            @DecimalMin("0.00") @Digits(integer = 15, fraction = 2) BigDecimal descontoMaximo,
            LocalDate validadeDesconto, @Size(max = 1500) String observacoes, Long versao) {
    }

    public record GerarContaRequest(@NotBlank @Size(max = 60) String numeroTitulo, @NotNull TipoTitulo tipoTitulo,
            @NotNull LocalDate emissao, @NotNull LocalDate vencimento,
            @NotNull @DecimalMin("0.01") @Digits(integer = 15, fraction = 2) BigDecimal valor,
            @DecimalMin("0.00") @Digits(integer = 15, fraction = 2) BigDecimal descontoMaximo,
            LocalDate validadeDesconto, @Size(max = 1500) String observacoes) {
    }

    public record ContaResponse(Long id, long versao, TipoConta tipo, Referencia pessoa, CategoriaResponse categoria,
            Long despesaOrigemId, String numeroTitulo, TipoTitulo tipoTitulo, String descricao,
            LocalDate emissao, LocalDate vencimento, BigDecimal valor, BigDecimal descontoMaximo,
            LocalDate validadeDesconto, String observacoes, BigDecimal totalBaixado, BigDecimal totalDesconto,
            BigDecimal totalAcrescimo, BigDecimal totalMovimentado, BigDecimal saldo, SituacaoConta situacao,
            boolean possuiHistorico, String motivoCancelamento, Instant criadoEm) {
    }

    public record BaixaRequest(@NotNull LocalDate data,
            @NotNull @DecimalMin("0.01") @Digits(integer = 15, fraction = 2) BigDecimal principal,
            @DecimalMin("0.00") @Digits(integer = 15, fraction = 2) BigDecimal acrescimo,
            @DecimalMin("0.00") @Digits(integer = 15, fraction = 2) BigDecimal desconto,
            @NotNull FormaPagamento forma, @Positive Long contaBancariaId, @Size(max = 1000) String observacoes) {
    }

    public record BaixaResponse(Long id, Long contaId, LocalDate data, BigDecimal principal,
            BigDecimal acrescimo, BigDecimal desconto, BigDecimal valorMovimentado, FormaPagamento forma,
            Referencia contaBancaria, String chaveIdempotencia, String observacoes, boolean estornada,
            String motivoEstorno, Instant estornadaEm, Instant criadoEm) {
    }

    public record ContaDetalhe(ContaResponse conta, List<BaixaResponse> historico) {
    }

    public record ResultadoBaixa(BaixaResponse baixa, boolean repeticao) {
    }

    public record MotivoRequest(@NotBlank @Size(min = 5, max = 250) String motivo) {
    }

    public record FiltroContas(TipoConta tipo, Long pessoaId, Long categoriaId, SituacaoConta situacao,
            LocalDate vencimentoDe, LocalDate vencimentoAte, LocalDate emissaoDe, LocalDate emissaoAte, String busca) {
    }

    public record Pagina<T>(List<T> itens, long total, int pagina, int tamanho, int totalPaginas) {
    }

    public record ResumoFinanceiro(LocalDate inicio, LocalDate fim, BigDecimal aPagar, BigDecimal aReceber,
            BigDecimal pago, BigDecimal recebido, BigDecimal saldoMovimentado, BigDecimal despesasPrevistas,
            long vencidas) {
    }

    public record MovimentoResponse(Long id, Long contaId, TipoConta tipo, String numeroTitulo, String descricao,
            String pessoa, LocalDate data, BigDecimal principal, BigDecimal acrescimo, BigDecimal desconto,
            BigDecimal valorMovimentado, FormaPagamento forma, String contaBancaria) {
    }
}
