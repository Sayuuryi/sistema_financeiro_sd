package br.edu.fluxo.planejamento.exceptions;

public class RespostaRemotaInvalidaException extends ErroNegocio {

    public RespostaRemotaInvalidaException(String servico) {
        super(
            502,
            "RESPOSTA_REMOTA_INVALIDA",
            "Não foi possível interpretar a resposta do serviço de " + servico + "."
        );
    }
}
