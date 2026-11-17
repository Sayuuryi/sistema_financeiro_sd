package br.edu.fluxo.gateway;

import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

// Única porta de entrada da API: cada caminho vai para o serviço registrado no servidor de nomes
@Configuration
public class Rotas {

    @Bean
    public RouteLocator rotasDaApi(RouteLocatorBuilder rotas) {
        return rotas
            .routes()
            .route("cadastros", r ->
                r
                    .path(
                        "/api/pessoas/**",
                        "/api/categorias/**",
                        "/api/bancos/**",
                        "/api/agencias/**",
                        "/api/contas-bancarias/**"
                    )
                    .filters(f -> protegida(f, "cadastros"))
                    .uri("lb://cadastros-service")
            )
            .route("contas-pagar", r ->
                r
                    .path("/api/contas-pagar/**")
                    .filters(f ->
                        protegida(
                            f.rewritePath("/api/contas-pagar(?<resto>.*)", "/api/contas${resto}"),
                            "contas-pagar"
                        )
                    )
                    .uri("lb://contas-pagar-service")
            )
            .route("contas-receber", r ->
                r
                    .path("/api/contas-receber/**")
                    .filters(f ->
                        protegida(
                            f.rewritePath("/api/contas-receber(?<resto>.*)", "/api/contas${resto}"),
                            "contas-receber"
                        )
                    )
                    .uri("lb://contas-receber-service")
            )
            .route("planejamento", r ->
                r
                    .path("/api/despesas-previstas/**")
                    .filters(f -> protegida(f, "planejamento"))
                    .uri("lb://planejamento-service")
            )
            .route("relatorios", r ->
                r
                    .path("/api/relatorios/**")
                    .filters(f -> protegida(f, "relatorios"))
                    .uri("lb://relatorios-service")
            )
            .route("visao-geral", r ->
                r
                    .path("/api/visao-geral/**")
                    .filters(f -> protegida(f, "visao-geral"))
                    .uri("lb://visao-geral-service")
            )
            .route("usuarios", r ->
                r
                    .path("/api/usuarios/**")
                    .filters(f -> protegida(f, "usuarios"))
                    .uri("lb://usuarios-service")
            )
            .build();
    }

    // O CORS fica aqui; sem o Origin, os serviços não repetem os cabeçalhos
    private GatewayFilterSpec protegida(GatewayFilterSpec f, String servico) {
        return f
            .removeRequestHeader("Origin")
            .circuitBreaker(c -> c.setName(servico).setFallbackUri("forward:/indisponivel"))
            // Só 502 e falha de conexão (instância caiu); 503 é resposta do serviço e não se repete
            .retry(c ->
                c
                    .setRetries(2)
                    .setMethods(HttpMethod.GET)
                    .setSeries()
                    .setStatuses(HttpStatus.BAD_GATEWAY)
                    .setBackoff(Duration.ofMillis(200), Duration.ofSeconds(1), 2, false)
            );
    }

    @Bean
    public CorsWebFilter cors(@Value("${frontend.origens}") List<String> origens) {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(origens);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Idempotency-Key", "Authorization"));
        config.setExposedHeaders(List.of("Location", "Content-Disposition", "ETag", "Retry-After"));
        config.setMaxAge(3600L);
        var fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/api/**", config);
        return new CorsWebFilter(fonte);
    }
}
