package br.edu.fluxo.receber.dominio;

import jakarta.persistence.*;
import org.hibernate.annotations.TenantId;

@MappedSuperclass
public abstract class Entidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private long versao;

    // Dono do registro; preenchido e filtrado pelo Hibernate
    @TenantId
    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long usuarioId;

    public Long getId() {
        return id;
    }

    public long getVersao() {
        return versao;
    }
}
