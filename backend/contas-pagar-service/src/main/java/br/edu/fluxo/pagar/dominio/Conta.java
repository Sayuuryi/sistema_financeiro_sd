package br.edu.fluxo.pagar.dominio;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "conta")
public class Conta extends Entidade {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoConta tipo;

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

    @Column(name = "pessoa_id", nullable = false)
    private Long pessoaId;

    public Long getPessoaId() {
        return pessoaId;
    }

    public void setPessoaId(Long value) {
        pessoaId = value;
    }

    @Column(name = "pessoa_nome", nullable = false, length = 120)
    private String pessoaNome;

    public String getPessoaNome() {
        return pessoaNome;
    }

    public void setPessoaNome(String value) {
        pessoaNome = value;
    }

    @Column(name = "categoria_id", nullable = false)
    private Long categoriaId;

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long value) {
        categoriaId = value;
    }

    @Column(name = "categoria_versao", nullable = false)
    private long categoriaVersao;

    public long getCategoriaVersao() {
        return categoriaVersao;
    }

    public void setCategoriaVersao(long value) {
        categoriaVersao = value;
    }

    @Column(name = "categoria_codigo", nullable = false, length = 20)
    private String categoriaCodigo;

    public String getCategoriaCodigo() {
        return categoriaCodigo;
    }

    public void setCategoriaCodigo(String value) {
        categoriaCodigo = value;
    }

    @Column(name = "categoria_nome", nullable = false, length = 100)
    private String categoriaNome;

    public String getCategoriaNome() {
        return categoriaNome;
    }

    public void setCategoriaNome(String value) {
        categoriaNome = value;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_natureza", nullable = false, length = 20)
    private NaturezaCategoria categoriaNatureza;

    public NaturezaCategoria getCategoriaNatureza() {
        return categoriaNatureza;
    }

    public void setCategoriaNatureza(NaturezaCategoria value) {
        categoriaNatureza = value;
    }

    @Column(name = "categoria_ativo", nullable = false)
    private boolean categoriaAtivo;

    public boolean getCategoriaAtivo() {
        return categoriaAtivo;
    }

    public void setCategoriaAtivo(boolean value) {
        categoriaAtivo = value;
    }

    @Column(name = "despesa_origem_id", unique = true)
    private Long despesaOrigemId;

    public Long getDespesaOrigemId() {
        return despesaOrigemId;
    }

    public void setDespesaOrigemId(Long value) {
        despesaOrigemId = value;
    }

    @Column(name = "origem_hash", length = 64)
    private String origemHash;

    public String getOrigemHash() {
        return origemHash;
    }

    public void setOrigemHash(String value) {
        origemHash = value;
    }

    public Conta() {}

    public TipoConta getTipo() {
        return tipo;
    }

    public void setTipo(TipoConta tipo) {
        this.tipo = tipo;
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
        if (cancelada) {
            return SituacaoConta.CANCELADA;
        }
        if (saldo(baixado).signum() == 0) {
            return tipo == TipoConta.PAGAR ? SituacaoConta.PAGA : SituacaoConta.RECEBIDA;
        }
        if (vencimento.isBefore(hoje)) {
            return SituacaoConta.VENCIDA;
        }
        if (baixado.signum() > 0) {
            return SituacaoConta.PARCIAL;
        }
        return SituacaoConta.ABERTA;
    }
}
