package br.edu.fluxo.cadastros.repositorio;

import br.edu.fluxo.cadastros.dominio.Banco;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BancoRepository extends JpaRepository<Banco, Long> {
    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);
}
