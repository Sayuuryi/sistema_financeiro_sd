package br.edu.fluxo.cadastros.repositorio;

import br.edu.fluxo.cadastros.dominio.Agencia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgenciaRepository extends JpaRepository<Agencia, Long> {
    boolean existsByBancoId(Long bancoId);
    boolean existsByBancoIdAndNumeroIgnoreCaseAndIdNot(Long bancoId, String numero, Long id);
}
