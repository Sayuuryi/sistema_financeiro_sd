package br.edu.fluxo.cadastros.dominio;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "categoria")
public class Categoria extends Entidade {

    @Column(length = 20, nullable = false)
    private String codigo;

    @Column(length = 100, nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NaturezaCategoria natureza;

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

    public Categoria() {}

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public NaturezaCategoria getNatureza() {
        return natureza;
    }

    public void setNatureza(NaturezaCategoria natureza) {
        this.natureza = natureza;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
