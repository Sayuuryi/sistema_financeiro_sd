package br.edu.fluxo.planejamento.exceptions;

public class ServicoIndisponivelException extends ErroNegocio {

    public ServicoIndisponivelException(String servico) {
        super(
            503,
            "SERVICO_INDISPONIVEL",
            "O serviço de " + servico + " não está disponível. Tente novamente em instantes."
        );
    }
}
