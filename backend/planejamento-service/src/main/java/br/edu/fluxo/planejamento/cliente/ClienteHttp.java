package br.edu.fluxo.planejamento.cliente;

import br.edu.fluxo.planejamento.exceptions.*;
import br.edu.fluxo.planejamento.seguranca.UsuarioAtual;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.function.Supplier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.*;

/** Escritas não são repetidas automaticamente. */
public class ClienteHttp {

    private final RestClient client;
    private final String servico;

    public ClienteHttp(String url, String servico, int timeoutMs) {
        this.servico = servico;
        var factory = new JdkClientHttpRequestFactory(
            HttpClient.newBuilder().connectTimeout(Duration.ofMillis(timeoutMs)).build()
        );
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        this.client = RestClient.builder()
            .baseUrl(url)
            .requestFactory(factory)
            .requestInterceptor((pedido, corpo, execucao) -> {
                String token = UsuarioAtual.token();
                if (token != null) {
                    pedido.getHeaders().setBearerAuth(token);
                }
                return execucao.execute(pedido, corpo);
            })
            .build();
    }

    public <T> T get(String path, Class<T> type) {
        return executar(() -> client.get().uri(path).retrieve().body(type));
    }

    public <T> T get(String path, ParameterizedTypeReference<T> type) {
        return executar(() -> client.get().uri(path).retrieve().body(type));
    }

    public <T> T post(String path, Object body, Class<T> type) {
        return executar(() -> client.post().uri(path).body(body).retrieve().body(type));
    }

    private <T> T executar(Supplier<T> action) {
        try {
            T result = action.get();
            if (result == null) {
                throw new RespostaRemotaInvalidaException(servico);
            }
            return result;
        } catch (RestClientResponseException e) {
            int status = e.getStatusCode().value();
            if (status >= 500) {
                throw new ServicoIndisponivelException(servico);
            }
            if (status == 404) {
                throw ErroNegocio.regra("Referência não encontrada no serviço de " + servico + ".");
            }
            if (status == 409) {
                throw ErroNegocio.conflito(
                    "A operação conflita com o estado já registrado no serviço de " +
                        servico +
                        ". Preserve os dados da solicitação original."
                );
            }
            throw new ErroNegocio(
                status,
                "SOLICITACAO_REMOTA_RECUSADA",
                "O serviço de " + servico + " recusou os dados informados."
            );
        } catch (ResourceAccessException e) {
            throw new ServicoIndisponivelException(servico);
        } catch (RestClientException e) {
            throw new RespostaRemotaInvalidaException(servico);
        }
    }
}
