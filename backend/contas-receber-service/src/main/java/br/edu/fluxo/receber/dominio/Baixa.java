package br.edu.fluxo.receber.dominio;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "baixa")
public class Baixa extends Entidade {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_id", nullable = false)
    private Conta conta;

    @Column(name = "chave_idempotencia", length = 36, nullable = false)
    private String chaveIdempotencia;

    @Column(nullable = false)
    private LocalDate data;

    @Column(name = "principal", nullable = false, precision = 19, scale = 2)
    private BigDecimal principal;

    @Column(name = "acrescimo", nullable = false, precision = 19, scale = 2)
    private BigDecimal acrescimo;

    @Column(name = "desconto", nullable = false, precision = 19, scale = 2)
    private BigDecimal desconto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private FormaPagamento forma;

    @Column(length = 1000)
    private String observacoes;

    @Column(nullable = false)
    private boolean estornada;

    @Column(name = "motivo_estorno", length = 250)
    private String motivoEstorno;

    @Column(name = "estornada_em")
    private Instant estornadaEm;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "conta_bancaria_id")
    private Long contaBancariaId;

    public Long getContaBancariaId() {
        return contaBancariaId;
    }

    public void setContaBancariaId(Long value) {
        contaBancariaId = value;
    }

    @Column(name = "conta_bancaria_descricao", length = 180)
    private String contaBancariaDescricao;

    public String getContaBancariaDescricao() {
        return contaBancariaDescricao;
    }

    public void setContaBancariaDescricao(String value) {
        contaBancariaDescricao = value;
    }

    public Baixa() {}

    public Conta getConta() {
        return conta;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    public String getChaveIdempotencia() {
        return chaveIdempotencia;
    }

    public void setChaveIdempotencia(String chaveIdempotencia) {
        this.chaveIdempotencia = chaveIdempotencia;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public void setPrincipal(BigDecimal principal) {
        this.principal = principal;
    }

    public BigDecimal getAcrescimo() {
        return acrescimo;
    }

    public void setAcrescimo(BigDecimal acrescimo) {
        this.acrescimo = acrescimo;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public void setDesconto(BigDecimal desconto) {
        this.desconto = desconto;
    }

    public FormaPagamento getForma() {
        return forma;
    }

    public void setForma(FormaPagamento forma) {
        this.forma = forma;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public boolean isEstornada() {
        return estornada;
    }

    public void setEstornada(boolean estornada) {
        this.estornada = estornada;
    }

    public String getMotivoEstorno() {
        return motivoEstorno;
    }

    public void setMotivoEstorno(String motivoEstorno) {
        this.motivoEstorno = motivoEstorno;
    }

    public Instant getEstornadaEm() {
        return estornadaEm;
    }

    public void setEstornadaEm(Instant estornadaEm) {
        this.estornadaEm = estornadaEm;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    public BigDecimal valorMovimentado() {
        return principal.add(acrescimo).subtract(desconto).setScale(2);
    }
}
