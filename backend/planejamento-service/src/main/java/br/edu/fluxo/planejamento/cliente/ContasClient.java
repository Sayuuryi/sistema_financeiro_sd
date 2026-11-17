package br.edu.fluxo.planejamento.cliente;

import br.edu.fluxo.planejamento.api.Dtos.*;
import br.edu.fluxo.planejamento.exceptions.RespostaRemotaInvalidaException;
import br.edu.fluxo.planejamento.exceptions.ServicoIndisponivelException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;

// Escrita: protegida pelo circuito, mas sem nova tentativa para não criar a conta duas vezes
@Component
public class ContasClient {

    private static final String SERVICO = "contas";

    private final ContasApi api;

    public ContasClient(ContasApi api) {
        this.api = api;
    }

    @CircuitBreaker(name = SERVICO, fallbackMethod = "converterSemContas")
    public ContaResponse converter(Long origem, ContaRequest request) {
        var conta = Chamada.executar(SERVICO, () -> api.converter(origem, request));
        if (conta.id() == null || conta.id() <= 0 || !origem.equals(conta.despesaOrigemId())) {
            throw new RespostaRemotaInvalidaException(SERVICO);
        }
        return conta;
    }

    private ContaResponse converterSemContas(
        Long origem,
        ContaRequest request,
        CallNotPermittedException e
    ) {
        throw new ServicoIndisponivelException(SERVICO);
    }
}
