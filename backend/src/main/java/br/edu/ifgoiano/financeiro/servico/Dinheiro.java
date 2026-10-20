package br.edu.ifgoiano.financeiro.servico;

import br.edu.ifgoiano.financeiro.erro.ErroNegocio;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Dinheiro {
    public static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal LIMITE = new BigDecimal("999999999999999.99");

    private Dinheiro() {
    }

    public static BigDecimal valor(BigDecimal input) {
        if (input == null)
            return ZERO;
        try {
            var value = input.setScale(2, RoundingMode.UNNECESSARY);
            if (value.abs().compareTo(LIMITE) > 0)
                throw ErroNegocio.regra("Valor monetário excede o limite permitido.");
            return value;
        } catch (ArithmeticException e) {
            throw ErroNegocio.regra("Valores monetários devem ter no máximo duas casas decimais.");
        }
    }

    public static BigDecimal positivo(BigDecimal input, String campo) {
        var value = valor(input);
        if (value.signum() <= 0)
            throw ErroNegocio.regra(campo + " deve ser maior que zero.");
        return value;
    }

    public static BigDecimal naoNegativo(BigDecimal input, String campo) {
        var value = valor(input);
        if (value.signum() < 0)
            throw ErroNegocio.regra(campo + " não pode ser negativo.");
        return value;
    }
}
