package br.edu.fluxo.configuracao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfiguracaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfiguracaoApplication.class, args);
    }
}
