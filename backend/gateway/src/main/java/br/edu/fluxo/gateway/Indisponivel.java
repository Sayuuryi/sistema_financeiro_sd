package br.edu.fluxo.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Resposta do circuit breaker quando o serviço da rota não responde
@RestController
public class Indisponivel {

    @RequestMapping("/indisponivel")
    public ResponseEntity<ProblemDetail> responder() {
        var problema = ProblemDetail.forStatusAndDetail(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Esta área está indisponível no momento. Tente novamente em instantes."
        );
        problema.setTitle("Serviço indisponível");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problema);
    }
}
