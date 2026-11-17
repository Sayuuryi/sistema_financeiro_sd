package br.edu.fluxo.relatorios;

import br.edu.fluxo.relatorios.seguranca.UsuarioAtual;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.net.http.HttpClient;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;

// Recriado a cada /actuator/refresh para valer os tempos novos de Integracao
@Component
@RefreshScope
public class LeitorRemoto {

    public record Destino(String servico, String caminho) {}

    public record Resultado(String servico, JsonNode dados, boolean disponivel) {}

    private final Map<String, RestClient> clientes = new HashMap<>();
    private final ObjectMapper json;
    private final CircuitBreakerRegistry circuitos;
    private final Retry repeticao;

    public LeitorRemoto(
        Environment env,
        ObjectMapper json,
        Integracao integracao,
        RestClient.Builder balanceado,
        CircuitBreakerRegistry circuitos,
        RetryRegistry repeticoes
    ) {
        this.json = json;
        this.circuitos = circuitos;
        this.repeticao = repeticoes.retry("leitura");
        var http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(integracao.getConexaoMs()))
            .build();
        var factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofMillis(integracao.getLeituraMs()));
        var nomes = Map.of(
            "pagar",
            "contas-pagar-service",
            "receber",
            "contas-receber-service",
            "planejamento",
            "planejamento-service",
            "cadastros",
            "cadastros-service"
        );
        nomes.forEach((nome, registro) -> {
            String url = env.getProperty("servicos." + nome + ".url");
            clientes.put(
                nome,
                Balanceamento.para(balanceado, url, registro).requestFactory(factory).build()
            );
        });
    }

    public Resultado ler(String servico, String caminho) {
        return ler(servico, caminho, UsuarioAtual.token());
    }

    // As leituras em paralelo rodam em outras threads; o token vai explícito
    private Resultado ler(String servico, String caminho, String token) {
        Supplier<JsonNode> leitura = () ->
            clientes
                .get(servico)
                .get()
                .uri(caminho)
                .headers(h -> {
                    if (token != null) {
                        h.setBearerAuth(token);
                    }
                })
                .retrieve()
                .body(JsonNode.class);
        // Retry por fora do circuito: com ele aberto, não há nova tentativa
        var protegida = Retry.decorateSupplier(
            repeticao,
            CircuitBreaker.decorateSupplier(circuitos.circuitBreaker(servico), leitura)
        );
        try {
            var body = protegida.get();
            return new Resultado(servico, body, body != null);
        } catch (CallNotPermittedException e) {
            return new Resultado(servico, null, false);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 400) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Confira os filtros informados."
                );
            }
            return new Resultado(servico, null, false);
        } catch (RestClientException e) {
            return new Resultado(servico, null, false);
        }
    }

    public Map<String, Resultado> paralelo(Map<String, Destino> destinos) {
        String token = UsuarioAtual.token();
        try (var pool = Executors.newVirtualThreadPerTaskExecutor()) {
            var pendentes = new LinkedHashMap<String, Future<Resultado>>();
            destinos.forEach((nome, destino) ->
                pendentes.put(
                    nome,
                    pool.submit(() -> ler(destino.servico(), destino.caminho(), token))
                )
            );
            var result = new LinkedHashMap<String, Resultado>();
            for (var entry : pendentes.entrySet()) {
                try {
                    result.put(entry.getKey(), entry.getValue().get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Consulta interrompida."
                    );
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof RuntimeException r) {
                        throw r;
                    }
                    throw new IllegalStateException(e);
                }
            }
            return result;
        }
    }

    public ArrayNode avisos(Collection<Resultado> resultados) {
        var nomes = Map.of(
            "pagar",
            "Contas a pagar",
            "receber",
            "Contas a receber",
            "planejamento",
            "Despesas previstas",
            "cadastros",
            "Cadastros"
        );
        var mensagens = new LinkedHashSet<String>();
        resultados
            .stream()
            .filter(r -> !r.disponivel())
            .forEach(r ->
                mensagens.add(
                    nomes.get(r.servico()) +
                        " indisponível no momento. Os valores dessa área não entram nos totais."
                )
            );
        var avisos = json.createArrayNode();
        mensagens.forEach(avisos::add);
        return avisos;
    }

    public void exigirCompleto(Collection<Resultado> resultados) {
        if (resultados.stream().anyMatch(r -> !r.disponivel())) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Não foi possível exportar agora: uma das áreas está indisponível. Tente novamente em instantes."
            );
        }
    }

    public static String periodo(LocalDate inicio, LocalDate fim) {
        var hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        inicio = inicio == null ? hoje.withDayOfMonth(1) : inicio;
        fim = fim == null ? hoje.withDayOfMonth(hoje.lengthOfMonth()) : fim;
        if (inicio.isAfter(fim)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "O início não pode ser posterior ao fim."
            );
        }
        return "inicio=" + inicio + "&fim=" + fim;
    }
}
