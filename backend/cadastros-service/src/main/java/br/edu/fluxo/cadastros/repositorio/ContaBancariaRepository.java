package br.edu.fluxo.cadastros.repositorio;

import br.edu.fluxo.cadastros.dominio.ContaBancaria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContaBancariaRepository extends JpaRepository<ContaBancaria, Long> {
    boolean existsByAgenciaId(Long agenciaId);
    boolean existsByAgenciaIdAndNumeroIgnoreCaseAndIdNot(Long agenciaId, String numero, Long id);
}
