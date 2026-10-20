package br.edu.ifgoiano.financeiro.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifgoiano.financeiro.dominio.Banco;

public interface BancoRepository extends JpaRepository<Banco, Long> {
    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);
}
