package br.edu.fluxo.planejamento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PlanejamentoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlanejamentoApplication.class, args);
    }
}
