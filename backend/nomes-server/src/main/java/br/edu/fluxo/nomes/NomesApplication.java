package br.edu.fluxo.nomes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class NomesApplication {

    public static void main(String[] args) {
        SpringApplication.run(NomesApplication.class, args);
    }
}
