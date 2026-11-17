package br.edu.fluxo.planejamento.seguranca;

import br.edu.fluxo.planejamento.config.Balanceamento;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Exige token válido e sessão aberta no serviço de usuários em toda rota de dados
@Configuration
public class Autenticacao implements HandlerInterceptor, WebMvcConfigurer {

    private static final String SEM_SESSAO = "Sessão ausente ou expirada. Entre novamente.";

    private final String segredo;
    private final RestClient usuarios;

    public Autenticacao(
        @Value("${seguranca.token-segredo}") String segredo,
        @Value("${servicos.usuarios.url}") String urlUsuarios,
        RestClient.Builder balanceado
    ) {
        this.segredo = Token.exigirSegredo(segredo);
        var factory = new JdkClientHttpRequestFactory(
            HttpClient.newBuilder().connectTimeout(Duration.ofMillis(1500)).build()
        );
        factory.setReadTimeout(Duration.ofMillis(2000));
        this.usuarios = Balanceamento.para(balanceado, urlUsuarios, "usuarios-service")
            .requestFactory(factory)
            .build();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).addPathPatterns("/api/**", "/interno/**");
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        if (CorsUtils.isPreFlightRequest(req)) {
            return true;
        }
        String cabecalho = req.getHeader("Authorization");
        String token =
            cabecalho != null && cabecalho.startsWith("Bearer ")
                ? cabecalho.substring(7).strip()
                : null;
        // A assinatura descarta tokens falsos sem precisar consultar a rede
        long id = Token.validar(token, segredo, Instant.now()).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.UNAUTHORIZED, SEM_SESSAO)
        );
        if (sessaoAberta(token) != id) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, SEM_SESSAO);
        }
        UsuarioAtual.definir(id, token);
        return true;
    }

    // Logout, troca de senha e expiração encerram a sessão; o token sozinho não basta
    private long sessaoAberta(String token) {
        try {
            var resposta = usuarios
                .get()
                .uri("/interno/sessoes/atual")
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .body(JsonNode.class);
            return resposta == null ? -1 : resposta.path("usuarioId").asLong(-1);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, SEM_SESSAO);
            }
            throw indisponivel();
        } catch (RestClientException e) {
            throw indisponivel();
        }
    }

    private static ResponseStatusException indisponivel() {
        return new ResponseStatusException(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Não foi possível confirmar seu login agora. Tente novamente em instantes."
        );
    }

    @Override
    public void afterCompletion(
        HttpServletRequest req,
        HttpServletResponse res,
        Object handler,
        Exception ex
    ) {
        UsuarioAtual.limpar();
    }
}
