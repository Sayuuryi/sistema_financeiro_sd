package br.edu.fluxo.cadastros.repositorio;

import br.edu.fluxo.cadastros.dominio.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);
}
