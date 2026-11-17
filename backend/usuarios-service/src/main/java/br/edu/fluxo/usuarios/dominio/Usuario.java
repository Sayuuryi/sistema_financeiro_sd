package br.edu.fluxo.usuarios.dominio;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import org.hibernate.annotations.OptimisticLock;

@Entity
@Table(name = "usuario")
public class Usuario extends Entidade {

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @Column(length = 30)
    private String telefone;

    @Column(length = 80)
    private String cargo;

    @Column(length = 120)
    private String empresa;

    // Capa e último acesso não disputam a edição dos dados do perfil
    @OptimisticLock(excluded = true)
    @Column(columnDefinition = "text")
    private String capa;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    @OptimisticLock(excluded = true)
    @Column(name = "ultimo_acesso_em")
    private OffsetDateTime ultimoAcessoEm;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getCapa() {
        return capa;
    }

    public void setCapa(String capa) {
        this.capa = capa;
    }

    public OffsetDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(OffsetDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public OffsetDateTime getUltimoAcessoEm() {
        return ultimoAcessoEm;
    }

    public void setUltimoAcessoEm(OffsetDateTime ultimoAcessoEm) {
        this.ultimoAcessoEm = ultimoAcessoEm;
    }
}
