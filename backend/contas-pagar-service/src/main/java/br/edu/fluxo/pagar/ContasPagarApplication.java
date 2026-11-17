package br.edu.fluxo.pagar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ContasPagarApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContasPagarApplication.class, args);
    }
}
