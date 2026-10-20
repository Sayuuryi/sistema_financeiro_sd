package br.edu.ifgoiano.financeiro.repositorio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import br.edu.ifgoiano.financeiro.api.Dtos.FiltroContas;
import br.edu.ifgoiano.financeiro.dominio.Baixa;
import br.edu.ifgoiano.financeiro.dominio.Conta;
import br.edu.ifgoiano.financeiro.dominio.TipoConta;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

public final class ContaSpecifications {
    private ContaSpecifications() {
    }

    public static Specification<Conta> filtrar(FiltroContas f, LocalDate hoje) {
        return (root, query, cb) -> {
            if (f == null)
                return cb.conjunction();
            List<Predicate> p = new ArrayList<>();
            if (f.tipo() != null)
                p.add(cb.equal(root.get("tipo"), f.tipo()));
            if (f.pessoaId() != null)
                p.add(cb.equal(root.get("pessoa").get("id"), f.pessoaId()));
            if (f.categoriaId() != null)
                p.add(cb.equal(root.get("categoria").get("id"), f.categoriaId()));
            if (f.vencimentoDe() != null)
                p.add(cb.greaterThanOrEqualTo(root.get("vencimento"), f.vencimentoDe()));
            if (f.vencimentoAte() != null)
                p.add(cb.lessThanOrEqualTo(root.get("vencimento"), f.vencimentoAte()));
            if (f.emissaoDe() != null)
                p.add(cb.greaterThanOrEqualTo(root.get("emissao"), f.emissaoDe()));
            if (f.emissaoAte() != null)
                p.add(cb.lessThanOrEqualTo(root.get("emissao"), f.emissaoAte()));
            if (f.busca() != null && !f.busca().isBlank()) {
                String like = "%" + f.busca().strip().toLowerCase(Locale.ROOT).replace("!", "!!").replace("%", "!%")
                        .replace("_", "!_") + "%";
                p.add(cb.or(cb.like(cb.lower(root.get("descricao")), like, '!'),
                        cb.like(cb.lower(root.get("numeroTitulo")), like, '!'),
                        cb.like(cb.lower(root.get("pessoa").get("nome")), like, '!')));
            }
            if (f.situacao() != null) {
                var sum = query.subquery(BigDecimal.class);
                var baixa = sum.from(Baixa.class);
                sum.select(cb.coalesce(cb.sum(baixa.<BigDecimal>get("principal")), BigDecimal.ZERO));
                sum.where(cb.equal(baixa.get("conta").get("id"), root.get("id")), cb.isFalse(baixa.get("estornada")));
                Expression<BigDecimal> saldo = cb.diff(root.<BigDecimal>get("valor"), sum);
                Predicate vigente = cb.isFalse(root.get("cancelada"));
                Predicate aberto = cb.greaterThan(saldo, BigDecimal.ZERO);
                Predicate naoVencido = cb.greaterThanOrEqualTo(root.get("vencimento"), hoje);
                p.add(switch (f.situacao()) {
                    case CANCELADA -> cb.isTrue(root.get("cancelada"));
                    case PAGA ->
                        cb.and(vigente, cb.equal(saldo, BigDecimal.ZERO), cb.equal(root.get("tipo"), TipoConta.PAGAR));
                    case RECEBIDA -> cb.and(vigente, cb.equal(saldo, BigDecimal.ZERO),
                            cb.equal(root.get("tipo"), TipoConta.RECEBER));
                    case VENCIDA -> cb.and(vigente, aberto, cb.lessThan(root.get("vencimento"), hoje));
                    case PARCIAL -> cb.and(vigente, aberto, naoVencido, cb.greaterThan(sum, BigDecimal.ZERO));
                    case ABERTA -> cb.and(vigente, aberto, naoVencido, cb.equal(sum, BigDecimal.ZERO));
                });
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }
}
