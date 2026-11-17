package br.edu.fluxo.planejamento.config;

import java.io.IOException;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class Balanceamento {

    // Resolve endereços como http://usuarios-service pelo servidor de nomes
    @Bean
    @LoadBalanced
    public RestClient.Builder clienteBalanceado() {
        // Sem instância registrada é falha de rede como outra qualquer: vira 503, não 500
        return RestClient.builder().requestInterceptor((pedido, corpo, execucao) -> {
            try {
                return execucao.execute(pedido, corpo);
            } catch (IllegalStateException e) {
                throw new IOException("Nenhuma instância de " + pedido.getURI().getHost(), e);
            }
        });
    }

    // Com URL fixa (testes, execução local) vai direto; sem ela, pergunta ao servidor de nomes
    public static RestClient.Builder para(RestClient.Builder balanceado, String url, String nome) {
        return url == null || url.isBlank()
            ? balanceado.clone().baseUrl("http://" + nome)
            : RestClient.builder().baseUrl(url);
    }
}
