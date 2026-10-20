package br.edu.ifgoiano.financeiro.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifgoiano.financeiro.dominio.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);
}
