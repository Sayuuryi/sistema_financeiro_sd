package br.edu.fluxo.planejamento.repositorio;

import br.edu.fluxo.planejamento.dominio.DespesaPrevista;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface DespesaRepository extends JpaRepository<DespesaPrevista, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DespesaPrevista d where d.id=:id")
    Optional<DespesaPrevista> findForUpdate(@Param("id") Long id);

    boolean existsByPessoaId(Long id);
    boolean existsByCategoriaId(Long id);
}
