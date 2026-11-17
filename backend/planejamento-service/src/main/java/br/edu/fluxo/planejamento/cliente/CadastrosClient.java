package br.edu.fluxo.planejamento.cliente;

import br.edu.fluxo.planejamento.api.Dtos.*;
import br.edu.fluxo.planejamento.exceptions.RespostaRemotaInvalidaException;
import br.edu.fluxo.planejamento.exceptions.ServicoIndisponivelException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;

// Leituras: tentam de novo com espera crescente; com o circuito aberto, respondem 503 na hora
@Component
public class CadastrosClient {

    private static final String SERVICO = "cadastros";

    private final CadastrosApi api;

    public CadastrosClient(CadastrosApi api) {
        this.api = api;
    }

    @Retry(name = SERVICO, fallbackMethod = "pessoaSemCadastros")
    @CircuitBreaker(name = SERVICO)
    public PessoaResponse pessoa(Long id) {
        var p = Chamada.executar(SERVICO, () -> api.pessoa(id));
        if (p.id() == null || p.nome() == null || p.tipo() == null) {
            throw new RespostaRemotaInvalidaException(SERVICO);
        }
        return p;
    }

    @Retry(name = SERVICO, fallbackMethod = "categoriaSemCadastros")
    @CircuitBreaker(name = SERVICO)
    public CategoriaResponse categoria(Long id) {
        var c = Chamada.executar(SERVICO, () -> api.categoria(id));
        if (c.id() == null || c.nome() == null || c.natureza() == null || c.codigo() == null) {
            throw new RespostaRemotaInvalidaException(SERVICO);
        }
        return c;
    }

    private PessoaResponse pessoaSemCadastros(Long id, CallNotPermittedException e) {
        throw new ServicoIndisponivelException(SERVICO);
    }

    private CategoriaResponse categoriaSemCadastros(Long id, CallNotPermittedException e) {
        throw new ServicoIndisponivelException(SERVICO);
    }
}
