package br.edu.fluxo.planejamento.dominio;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "despesa_prevista")
public class DespesaPrevista extends Entidade {

    @Column(length = 180, nullable = false)
    private String descricao;

    @Column(length = 60)
    private String referencia;

    @Column(name = "data_prevista", nullable = false)
    private LocalDate dataPrevista;

    @Column(name = "valor_previsto", nullable = false, precision = 19, scale = 2)
    private BigDecimal valorPrevisto;

    @Column(length = 1500)
    private String observacoes;

    @Column(nullable = false)
    private boolean ativo;

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

    @Column(name = "conta_gerada_id", unique = true)
    private Long contaGeradaId;

    public Long getContaGeradaId() {
        return contaGeradaId;
    }

    public void setContaGeradaId(Long value) {
        contaGeradaId = value;
    }

    public DespesaPrevista() {}

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public LocalDate getDataPrevista() {
        return dataPrevista;
    }

    public void setDataPrevista(LocalDate dataPrevista) {
        this.dataPrevista = dataPrevista;
    }

    public BigDecimal getValorPrevisto() {
        return valorPrevisto;
    }

    public void setValorPrevisto(BigDecimal valorPrevisto) {
        this.valorPrevisto = valorPrevisto;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
