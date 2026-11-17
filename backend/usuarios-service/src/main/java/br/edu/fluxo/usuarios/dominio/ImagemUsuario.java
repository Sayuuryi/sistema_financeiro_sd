package br.edu.fluxo.usuarios.dominio;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "imagem_usuario")
@IdClass(ImagemUsuario.Chave.class)
public class ImagemUsuario {

    public record Chave(Long usuarioId, String tipo) implements Serializable {
        public Chave() {
            this(null, null);
        }
    }

    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;

    @Id
    @Column(length = 10)
    private String tipo;

    @Column(nullable = false)
    private byte[] conteudo;

    @Column(name = "media_type", nullable = false, length = 30)
    private String mediaType;

    @Column(nullable = false, length = 64, columnDefinition = "bpchar(64)")
    private String hash;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public byte[] getConteudo() {
        return conteudo;
    }

    public void setConteudo(byte[] conteudo) {
        this.conteudo = conteudo;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public OffsetDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(OffsetDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
