package br.edu.fluxo.relatorios;

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
}
