package br.edu.ifgoiano.financeiro.dominio;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "conta")
public class Conta extends Entidade {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoConta tipo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "despesa_origem_id")
    private DespesaPrevista despesaOrigem;
    @Column(name = "numero_titulo", length = 60, nullable = false)
    private String numeroTitulo;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_titulo", length = 30, nullable = false)
    private TipoTitulo tipoTitulo;
    @Column(length = 180, nullable = false)
    private String descricao;
    @Column(nullable = false)
    private LocalDate emissao;
    @Column(nullable = false)
    private LocalDate vencimento;
    @Column(name = "valor", nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;
    @Column(name = "desconto_maximo", nullable = false, precision = 19, scale = 2)
    private BigDecimal descontoMaximo;
    @Column(name = "validade_desconto")
    private LocalDate validadeDesconto;
    @Column(length = 1500)
    private String observacoes;
    @Column(nullable = false)
    private boolean cancelada;
    @Column(name = "motivo_cancelamento", length = 250)
    private String motivoCancelamento;
    @Column(name = "cancelada_em")
    private Instant canceladaEm;
    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    public Conta() {
    }

    public TipoConta getTipo() {
        return tipo;
    }

    public void setTipo(TipoConta tipo) {
        this.tipo = tipo;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public void setPessoa(Pessoa pessoa) {
        this.pessoa = pessoa;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public DespesaPrevista getDespesaOrigem() {
        return despesaOrigem;
    }

    public void setDespesaOrigem(DespesaPrevista despesaOrigem) {
        this.despesaOrigem = despesaOrigem;
    }

    public String getNumeroTitulo() {
        return numeroTitulo;
    }

    public void setNumeroTitulo(String numeroTitulo) {
        this.numeroTitulo = numeroTitulo;
    }

    public TipoTitulo getTipoTitulo() {
        return tipoTitulo;
    }

    public void setTipoTitulo(TipoTitulo tipoTitulo) {
        this.tipoTitulo = tipoTitulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getEmissao() {
        return emissao;
    }

    public void setEmissao(LocalDate emissao) {
        this.emissao = emissao;
    }

    public LocalDate getVencimento() {
        return vencimento;
    }

    public void setVencimento(LocalDate vencimento) {
        this.vencimento = vencimento;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public BigDecimal getDescontoMaximo() {
        return descontoMaximo;
    }

    public void setDescontoMaximo(BigDecimal descontoMaximo) {
        this.descontoMaximo = descontoMaximo;
    }

    public LocalDate getValidadeDesconto() {
        return validadeDesconto;
    }

    public void setValidadeDesconto(LocalDate validadeDesconto) {
        this.validadeDesconto = validadeDesconto;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public boolean isCancelada() {
        return cancelada;
    }

    public void setCancelada(boolean cancelada) {
        this.cancelada = cancelada;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public void setMotivoCancelamento(String motivoCancelamento) {
        this.motivoCancelamento = motivoCancelamento;
    }

    public Instant getCanceladaEm() {
        return canceladaEm;
    }

    public void setCanceladaEm(Instant canceladaEm) {
        this.canceladaEm = canceladaEm;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    public BigDecimal saldo(BigDecimal baixado) {
        return cancelada ? BigDecimal.ZERO.setScale(2) : valor.subtract(baixado).setScale(2);
    }

    public SituacaoConta situacaoEm(LocalDate hoje, BigDecimal baixado) {
        if (cancelada)
            return SituacaoConta.CANCELADA;
        if (saldo(baixado).signum() == 0)
            return tipo == TipoConta.PAGAR ? SituacaoConta.PAGA : SituacaoConta.RECEBIDA;
        if (vencimento.isBefore(hoje))
            return SituacaoConta.VENCIDA;
        if (baixado.signum() > 0)
            return SituacaoConta.PARCIAL;
        return SituacaoConta.ABERTA;
    }
}
