package br.edu.fluxo.pagar.cliente;

import br.edu.fluxo.pagar.exceptions.*;
import br.edu.fluxo.pagar.seguranca.UsuarioAtual;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestInterceptor;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import feign.optionals.OptionalDecoder;
import java.util.List;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

// Vale para todos os clientes Feign deste serviço
@Configuration
public class ConfiguracaoFeign {

    @Bean
    public RequestInterceptor repassarToken() {
        return pedido -> {
            String token = UsuarioAtual.token();
            if (token != null) {
                pedido.header("Authorization", "Bearer " + token);
            }
        };
    }

    // Campos novos na resposta de outro serviço não quebram a leitura
    @Bean
    public Decoder leitorTolerante(ObjectMapper json) {
        var mapper = json.copy().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        var conversores = new HttpMessageConverters(
            false,
            List.of(new MappingJackson2HttpMessageConverter(mapper))
        );
        return new OptionalDecoder(new ResponseEntityDecoder(new SpringDecoder(() -> conversores)));
    }

    @Bean
    public ErrorDecoder errosRemotos() {
        return (metodo, resposta) -> {
            String servico = resposta
                .request()
                .requestTemplate()
                .feignTarget()
                .name()
                .replace("-service", "");
            int status = resposta.status();
            if (status >= 500) {
                return new ServicoIndisponivelException(servico);
            }
            if (status == 404) {
                return ErroNegocio.regra(
                    "Referência não encontrada no serviço de " + servico + "."
                );
            }
            if (status == 409) {
                return ErroNegocio.conflito(
                    "A operação conflita com o estado já registrado no serviço de " +
                        servico +
                        ". Preserve os dados da solicitação original."
                );
            }
            return new ErroNegocio(
                status,
                "SOLICITACAO_REMOTA_RECUSADA",
                "O serviço de " + servico + " recusou os dados informados."
            );
        };
    }
}
