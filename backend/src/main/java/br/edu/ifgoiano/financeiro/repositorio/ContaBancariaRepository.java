package br.edu.ifgoiano.financeiro.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifgoiano.financeiro.dominio.ContaBancaria;

public interface ContaBancariaRepository extends JpaRepository<ContaBancaria, Long> {
    boolean existsByAgenciaId(Long agenciaId);

    boolean existsByAgenciaIdAndNumeroIgnoreCaseAndIdNot(Long agenciaId, String numero, Long id);
}
