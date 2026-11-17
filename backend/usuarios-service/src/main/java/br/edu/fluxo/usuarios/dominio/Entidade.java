package br.edu.fluxo.usuarios.dominio;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class Entidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private long versao;

    public Long getId() {
        return id;
    }

    public long getVersao() {
        return versao;
    }
}
