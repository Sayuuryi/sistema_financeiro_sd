package br.edu.fluxo.cadastros.seguranca;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Exige o token da sessão nas rotas de dados; o próprio serviço confere a assinatura
@Configuration
public class Autenticacao implements HandlerInterceptor, WebMvcConfigurer {

    private final String segredo;

    public Autenticacao(@Value("${seguranca.token-segredo}") String segredo) {
        this.segredo = Token.exigirSegredo(segredo);
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
        var id = Token.validar(token, segredo, Instant.now()).orElseThrow(() ->
            new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Sessão ausente ou expirada. Entre novamente."
            )
        );
        UsuarioAtual.definir(id, token);
        return true;
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
