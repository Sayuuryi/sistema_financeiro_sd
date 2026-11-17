package br.edu.fluxo.planejamento.dominio;

public enum FormaPagamento {
    DINHEIRO,
    PIX,
    PIX_AUTOMATICO,
    TRANSFERENCIA,
    BOLETO,
    // Valor antigo, mantido para baixas registradas antes da separação crédito/débito
    CARTAO,
    CARTAO_CREDITO,
    CARTAO_DEBITO,
    CARTEIRA_DIGITAL,
    DEBITO_AUTOMATICO,
    CHEQUE,
}
