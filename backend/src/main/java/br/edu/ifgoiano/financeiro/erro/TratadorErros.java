package br.edu.ifgoiano.financeiro.erro;

import java.net.URI;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class TratadorErros {
    private static final Logger LOG = LoggerFactory.getLogger(TratadorErros.class);

    private ProblemDetail problema(int status, String codigo, String detalhe, HttpServletRequest request) {
        var p = ProblemDetail.forStatusAndDetail(org.springframework.http.HttpStatusCode.valueOf(status), detalhe);
        p.setTitle(switch (status) {
            case 400 -> "Dados inválidos";
            case 404 -> "Registro não encontrado";
            case 409 -> "Conflito de dados";
            case 422 -> "Regra de negócio";
            default -> "Falha na solicitação";
        });
        p.setInstance(URI.create(request.getRequestURI()));
        p.setProperty("codigo", codigo);
        return p;
    }

    @ExceptionHandler(ErroNegocio.class)
    ProblemDetail negocio(ErroNegocio e, HttpServletRequest r) {
        return problema(e.getStatus(), e.getCodigo(), e.getMessage(), r);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validacao(MethodArgumentNotValidException e, HttpServletRequest r) {
        var p = problema(400, "VALIDACAO", "Revise os campos informados.", r);
        var fields = new LinkedHashMap<String, String>();
        e.getBindingResult().getFieldErrors().forEach(f -> fields.putIfAbsent(f.getField(), f.getDefaultMessage()));
        p.setProperty("violacoes", fields);
        return p;
    }

    @ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            ServletRequestBindingException.class })
    ProblemDetail formato(Exception e, HttpServletRequest r) {
        return problema(400, "FORMATO_INVALIDO",
                "Confira o JSON, os campos, as datas ISO (AAAA-MM-DD), os valores e os parâmetros obrigatórios.", r);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail integridade(Exception e, HttpServletRequest r) {
        return problema(409, "INTEGRIDADE",
                "Operação não concluída: há um identificador duplicado ou um registro vinculado. Atualize os dados e tente novamente.",
                r);
    }

    @ExceptionHandler({ ObjectOptimisticLockingFailureException.class, PessimisticLockingFailureException.class })
    ProblemDetail concorrencia(Exception e, HttpServletRequest r) {
        return problema(409, "CONCORRENCIA",
                "O registro foi alterado ou está em uso. Atualize a tela antes de repetir a operação.", r);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail inesperado(Exception e, HttpServletRequest r) {
        if (e instanceof ErrorResponse response)
            return problema(response.getStatusCode().value(), "REQUISICAO_INVALIDA", response.getBody().getDetail(), r);
        LOG.error("Falha na operação {}", r.getRequestURI(), e);
        return problema(500, "ERRO_INTERNO",
                "Não foi possível concluir a operação. Consulte o responsável pela aplicação.", r);
    }
}
