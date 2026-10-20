package br.edu.ifgoiano.financeiro.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "banco")
public class Banco extends Entidade {
    @Column(length = 10, nullable = false)
    private String codigo;
    @Column(length = 120, nullable = false)
    private String nome;
    @Column(nullable = false)
    private boolean ativo;

    public Banco() {
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

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
