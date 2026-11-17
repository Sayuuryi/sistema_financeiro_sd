package br.edu.fluxo.usuarios.repositorio;

import br.edu.fluxo.usuarios.dominio.Sessao;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;

public interface SessaoRepository extends JpaRepository<Sessao, Long> {
    @Query("select s from Sessao s join fetch s.usuario where s.tokenHash = ?1")
    Optional<Sessao> findByTokenHash(String tokenHash);

    @Modifying
    @Query("delete from Sessao s where s.expiraEm < ?1")
    int apagarExpiradas(OffsetDateTime agora);

    @Modifying
    @Query("delete from Sessao s where s.usuario.id = ?1 and s.id <> ?2")
    int apagarOutras(Long usuarioId, Long sessaoId);
}
