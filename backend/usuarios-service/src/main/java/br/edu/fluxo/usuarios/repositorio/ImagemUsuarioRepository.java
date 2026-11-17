package br.edu.fluxo.usuarios.repositorio;

import br.edu.fluxo.usuarios.dominio.ImagemUsuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;

public interface ImagemUsuarioRepository extends JpaRepository<ImagemUsuario, ImagemUsuario.Chave> {
    interface Resumo {
        String getTipo();

        String getHash();

        String getMediaType();
    }

    // Só os dados do endereço, sem carregar os bytes
    @Query(
        "select i.tipo as tipo, i.hash as hash, i.mediaType as mediaType from ImagemUsuario i where i.usuarioId = ?1"
    )
    List<Resumo> resumos(Long usuarioId);

    Optional<ImagemUsuario> findByUsuarioIdAndTipoAndHash(Long usuarioId, String tipo, String hash);
}
