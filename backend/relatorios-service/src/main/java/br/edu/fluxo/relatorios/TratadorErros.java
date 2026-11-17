package br.edu.fluxo.relatorios;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class TratadorErros {

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail tratar(ResponseStatusException erro) {
        var problema = ProblemDetail.forStatusAndDetail(erro.getStatusCode(), erro.getReason());
        problema.setTitle(
            erro.getStatusCode().value() == 503 ? "Área indisponível" : "Confira os dados"
        );
        return problema;
    }

    @ExceptionHandler(BulkheadFullException.class)
    public ResponseEntity<ProblemDetail> semVaga(BulkheadFullException erro) {
        var problema = ProblemDetail.forStatusAndDetail(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Há outras exportações em andamento. Tente de novo em alguns segundos."
        );
        problema.setTitle("Exportação ocupada");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .header(HttpHeaders.RETRY_AFTER, "5")
            .body(problema);
    }
}
