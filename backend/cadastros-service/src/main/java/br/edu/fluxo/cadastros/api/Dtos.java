package br.edu.fluxo.cadastros.api;

import br.edu.fluxo.cadastros.dominio.*;
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

    public record PessoaRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotNull TipoPessoa tipo,
        @Pattern(
            regexp = "(?:[0-9]{11}|[0-9]{14})?",
            message = "informe 11 ou 14 dígitos, sem pontuação"
        )
        String documento,
        @Email @Size(max = 150) String email,
        @Size(max = 30) String telefone,
        @Size(max = 100) String contato,
        @Size(max = 200) String endereco,
        @Size(max = 80) String cidade,
        @Pattern(regexp = "(?:[A-Za-z]{2})?", message = "use duas letras") String estado,
        @Pattern(regexp = "(?:[0-9]{5}-?[0-9]{3})?", message = "CEP inválido") String cep,
        Boolean ativo,
        Long versao
    ) {}

    public record CategoriaRequest(
        @NotBlank @Size(max = 20) String codigo,
        @NotBlank @Size(max = 100) String nome,
        @NotNull NaturezaCategoria natureza,
        Boolean ativo,
        Long versao
    ) {}

    public record BancoRequest(
        @NotBlank @Size(max = 10) String codigo,
        @NotBlank @Size(max = 120) String nome,
        Boolean ativo,
        Long versao
    ) {}

    public record AgenciaRequest(
        @NotNull @Positive Long bancoId,
        @NotBlank @Size(max = 20) String numero,
        @NotBlank @Size(max = 120) String nome,
        Boolean ativo,
        Long versao
    ) {}

    public record ContaBancariaRequest(
        @NotNull @Positive Long agenciaId,
        @NotBlank @Size(max = 30) String numero,
        @NotBlank @Size(max = 120) String descricao,
        @Digits(integer = 15, fraction = 2) BigDecimal saldoInicial,
        Boolean ativo,
        Long versao
    ) {}

    public record SaldoBancario(Long contaBancariaId, BigDecimal movimentoLiquido) {}
}
