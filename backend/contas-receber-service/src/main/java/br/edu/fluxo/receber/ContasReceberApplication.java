package br.edu.fluxo.receber;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ContasReceberApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContasReceberApplication.class, args);
    }
}
