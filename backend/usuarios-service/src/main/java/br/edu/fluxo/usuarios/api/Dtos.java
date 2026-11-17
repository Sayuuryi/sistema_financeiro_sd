package br.edu.fluxo.usuarios.api;

import br.edu.fluxo.usuarios.dominio.Usuario;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

public final class Dtos {

    private Dtos() {}

    public record CadastroRequest(
        @NotBlank @Size(min = 2, max = 120) String nome,
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank @Size(min = 8, max = 72, message = "use de 8 a 72 caracteres") String senha
    ) {}

    public record LoginRequest(@NotBlank String email, @NotBlank String senha) {}

    public record PerfilRequest(
        @NotBlank @Size(min = 2, max = 120) String nome,
        @NotBlank @Email @Size(max = 160) String email,
        @Size(max = 30) String telefone,
        @Size(max = 80) String cargo,
        @Size(max = 120) String empresa,
        @NotNull Long versao
    ) {}

    public record SenhaRequest(
        @NotBlank String senhaAtual,
        @NotBlank @Size(min = 8, max = 72, message = "use de 8 a 72 caracteres") String novaSenha
    ) {}

    public record FundoRequest(@NotBlank String fundo) {}

    public record PerfilResponse(
        Long id,
        long versao,
        String nome,
        String email,
        String telefone,
        String cargo,
        String empresa,
        String foto,
        String capa,
        OffsetDateTime criadoEm,
        OffsetDateTime ultimoAcessoEm
    ) {
        // foto e capa chegam como endereço; a capa também pode ser um fundo pronto
        public static PerfilResponse de(Usuario u, String foto, String capa) {
            return new PerfilResponse(
                u.getId(),
                u.getVersao(),
                u.getNome(),
                u.getEmail(),
                u.getTelefone(),
                u.getCargo(),
                u.getEmpresa(),
                foto,
                capa,
                u.getCriadoEm(),
                u.getUltimoAcessoEm()
            );
        }
    }

    public record SessaoResponse(String token, OffsetDateTime expiraEm, PerfilResponse usuario) {}
}
