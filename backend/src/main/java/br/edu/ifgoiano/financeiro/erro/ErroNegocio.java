package br.edu.ifgoiano.financeiro.erro;

public class ErroNegocio extends RuntimeException {
    private final int status;
    private final String codigo;

    public ErroNegocio(int status, String codigo, String mensagem) {
        super(mensagem);
        this.status = status;
        this.codigo = codigo;
    }

    public int getStatus() {
        return status;
    }

    public String getCodigo() {
        return codigo;
    }

    public static ErroNegocio regra(String mensagem) {
        return new ErroNegocio(422, "REGRA_NEGOCIO", mensagem);
    }

    public static ErroNegocio conflito(String mensagem) {
        return new ErroNegocio(409, "CONFLITO", mensagem);
    }

    public static ErroNegocio ausente(String recurso) {
        return new ErroNegocio(404, "NAO_ENCONTRADO", recurso + " não encontrado(a).");
    }
}
