package br.edu.fluxo.receber.cliente;

import br.edu.fluxo.receber.exceptions.*;
import feign.FeignException;
import feign.RetryableException;
import java.util.function.Supplier;

// Falha de rede vira 503 (conta para o circuito); resposta vazia ou ilegível vira 502
final class Chamada {

    private Chamada() {}

    static <T> T executar(String servico, Supplier<T> chamada) {
        try {
            T resultado = chamada.get();
            if (resultado == null) {
                throw new RespostaRemotaInvalidaException(servico);
            }
            return resultado;
        } catch (RetryableException e) {
            throw new ServicoIndisponivelException(servico);
        } catch (FeignException e) {
            throw new RespostaRemotaInvalidaException(servico);
        }
    }
}
