package br.edu.fluxo.pagar.servico;

import br.edu.fluxo.pagar.dominio.Entidade;
import br.edu.fluxo.pagar.exceptions.ErroNegocio;

public final class RegistroUtils {

    private RegistroUtils() {}

    public static String texto(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    public static void versao(Entidade entity, Long expected) {
        if (expected == null) {
            throw new ErroNegocio(400, "VERSAO_OBRIGATORIA", "Informe a versão atual para editar.");
        }
        if (entity.getVersao() != expected) {
            throw ErroNegocio.conflito("O registro mudou. Atualize a consulta antes de editar.");
        }
    }
}
