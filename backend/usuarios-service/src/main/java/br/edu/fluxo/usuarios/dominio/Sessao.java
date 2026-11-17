package br.edu.fluxo.usuarios.dominio;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "sessao")
public class Sessao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Guarda só o SHA-256 do token; o token em si fica com o navegador
    @Column(
        name = "token_hash",
        nullable = false,
        unique = true,
        length = 64,
        columnDefinition = "bpchar(64)"
    )
    private String tokenHash;

    @Column(name = "criada_em", nullable = false)
    private OffsetDateTime criadaEm;

    @Column(name = "expira_em", nullable = false)
    private OffsetDateTime expiraEm;

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public OffsetDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(OffsetDateTime criadaEm) {
        this.criadaEm = criadaEm;
    }

    public OffsetDateTime getExpiraEm() {
        return expiraEm;
    }

    public void setExpiraEm(OffsetDateTime expiraEm) {
        this.expiraEm = expiraEm;
    }
}
