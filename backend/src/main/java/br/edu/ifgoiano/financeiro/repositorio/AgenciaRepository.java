package br.edu.ifgoiano.financeiro.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifgoiano.financeiro.dominio.Agencia;

public interface AgenciaRepository extends JpaRepository<Agencia, Long> {
    boolean existsByBancoId(Long bancoId);

    boolean existsByBancoIdAndNumeroIgnoreCaseAndIdNot(Long bancoId, String numero, Long id);
}
