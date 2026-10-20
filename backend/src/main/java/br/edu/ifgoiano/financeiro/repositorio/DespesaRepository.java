package br.edu.ifgoiano.financeiro.repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.ifgoiano.financeiro.dominio.DespesaPrevista;
import jakarta.persistence.LockModeType;

public interface DespesaRepository extends JpaRepository<DespesaPrevista, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DespesaPrevista d where d.id=:id")
    Optional<DespesaPrevista> findForUpdate(@Param("id") Long id);

    boolean existsByPessoaId(Long id);

    boolean existsByCategoriaId(Long id);
}
