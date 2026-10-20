package br.edu.ifgoiano.financeiro.repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.ifgoiano.financeiro.dominio.Conta;
import br.edu.ifgoiano.financeiro.dominio.TipoConta;
import jakarta.persistence.LockModeType;

public interface ContaRepository extends JpaRepository<Conta, Long>, JpaSpecificationExecutor<Conta> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Conta c where c.id=:id")
    Optional<Conta> findForUpdate(@Param("id") Long id);

    boolean existsByPessoaId(Long id);

    boolean existsByCategoriaId(Long id);

    boolean existsByPessoaIdAndTipo(Long id, TipoConta tipo);

    boolean existsByTipoAndPessoaIdAndNumeroTituloIgnoreCaseAndIdNot(TipoConta tipo, Long pessoaId, String numeroTitulo,
            Long id);

    Optional<Conta> findByDespesaOrigemId(Long id);
}
