package br.edu.ifgoiano.financeiro.repositorio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.ifgoiano.financeiro.dominio.Baixa;

public interface BaixaRepository extends JpaRepository<Baixa, Long> {
    Optional<Baixa> findByChaveIdempotencia(String chave);

    List<Baixa> findByContaIdOrderByIdAsc(Long id);

    boolean existsByContaId(Long id);

    boolean existsByContaBancariaId(Long id);

    List<Baixa> findByContaBancariaIdAndEstornadaFalse(Long id);

    List<Baixa> findByEstornadaFalseAndDataBetweenOrderByDataDescIdDesc(LocalDate inicio, LocalDate fim);

    @Query("select b.conta.id as contaId, sum(b.principal) as principal, sum(b.acrescimo) as acrescimo, sum(b.desconto) as desconto from Baixa b where b.estornada=false and b.conta.id in :ids group by b.conta.id")
    List<TotaisConta> totaisPorConta(@Param("ids") Collection<Long> ids);

    @Query("select distinct b.conta.id from Baixa b where b.conta.id in :ids")
    List<Long> contasComHistorico(@Param("ids") Collection<Long> ids);

    interface TotaisConta {
        Long getContaId();

        BigDecimal getPrincipal();

        BigDecimal getAcrescimo();

        BigDecimal getDesconto();
    }
}
