package br.edu.fluxo.pagar.repositorio;

import br.edu.fluxo.pagar.dominio.*;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ContaRepository
    extends JpaRepository<Conta, Long>, JpaSpecificationExecutor<Conta>
{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Conta c where c.id=:id")
    Optional<Conta> findForUpdate(@Param("id") Long id);

    boolean existsByPessoaId(Long id);
    boolean existsByCategoriaId(Long id);
    boolean existsByPessoaIdAndTipo(Long id, TipoConta tipo);
    boolean existsByTipoAndPessoaIdAndNumeroTituloIgnoreCaseAndIdNot(
        TipoConta tipo,
        Long pessoaId,
        String numeroTitulo,
        Long id
    );
    Optional<Conta> findByDespesaOrigemId(Long id);
}
