package br.edu.fluxo.visaogeral;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// Tempos das leituras nos outros serviços; mudam sem reiniciar via /actuator/refresh
@Component
@ConfigurationProperties("integracao")
public class Integracao {

    private int conexaoMs = 1500;
    private int leituraMs = 2000;

    public int getConexaoMs() {
        return conexaoMs;
    }

    public void setConexaoMs(int conexaoMs) {
        this.conexaoMs = conexaoMs;
    }

    public int getLeituraMs() {
        return leituraMs;
    }

    public void setLeituraMs(int leituraMs) {
        this.leituraMs = leituraMs;
    }
}
