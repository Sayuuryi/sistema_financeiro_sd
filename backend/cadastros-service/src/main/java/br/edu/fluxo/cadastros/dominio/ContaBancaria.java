package br.edu.fluxo.cadastros.dominio;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "conta_bancaria")
public class ContaBancaria extends Entidade {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agencia_id", nullable = false)
    private Agencia agencia;

    @Column(length = 30, nullable = false)
    private String numero;

    @Column(length = 120, nullable = false)
    private String descricao;

    @Column(name = "saldo_inicial", nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoInicial;

    @Column(nullable = false)
    private boolean ativo;

    @Column(nullable = false)
    private boolean excluido;

    public boolean isExcluido() {
        return excluido;
    }

    public void setExcluido(boolean value) {
        excluido = value;
    }

    public ContaBancaria() {}

    public Agencia getAgencia() {
        return agencia;
    }

    public void setAgencia(Agencia agencia) {
        this.agencia = agencia;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getSaldoInicial() {
        return saldoInicial;
    }

    public void setSaldoInicial(BigDecimal saldoInicial) {
        this.saldoInicial = saldoInicial;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
