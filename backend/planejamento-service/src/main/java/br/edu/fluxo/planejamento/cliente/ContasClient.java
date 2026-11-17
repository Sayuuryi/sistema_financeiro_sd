package br.edu.fluxo.planejamento.cliente;

import br.edu.fluxo.planejamento.api.Dtos.*;
import br.edu.fluxo.planejamento.exceptions.RespostaRemotaInvalidaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ContasClient {

    private final ClienteHttp http;

    public ContasClient(
        @Value("${servicos.contas.url}") String url,
        @Value("${integracao.timeout-ms}") int timeout
    ) {
        http = new ClienteHttp(url, "contas", timeout);
    }

    public ContaResponse converter(Long origem, ContaRequest request) {
        var conta = http.post(
            "/interno/previsoes/" + origem + "/conta",
            request,
            ContaResponse.class
        );
        if (conta.id() == null || conta.id() <= 0 || !origem.equals(conta.despesaOrigemId())) {
            throw new RespostaRemotaInvalidaException("contas");
        }
        return conta;
    }
}
