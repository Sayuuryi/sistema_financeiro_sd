package br.edu.ifgoiano.financeiro.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

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

    public Categoria() {
    }

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
