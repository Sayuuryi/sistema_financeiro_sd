package br.edu.fluxo.cadastros.repositorio;

import br.edu.fluxo.cadastros.dominio.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
    boolean existsByDocumentoAndIdNot(String documento, Long id);
}
