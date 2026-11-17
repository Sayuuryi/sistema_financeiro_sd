package br.edu.fluxo.usuarios.servico;

import br.edu.fluxo.usuarios.exceptions.ErroNegocio;
import java.util.Base64;
import java.util.Set;
import java.util.regex.Pattern;

// Foto e capa chegam como data URL já reduzidas pelo navegador
public final class Imagens {

    public static final int LIMITE_FOTO = 400 * 1024;
    public static final int LIMITE_CAPA = 1024 * 1024;

    private static final Set<String> FUNDOS = Set.of(
        "fundo:teal",
        "fundo:oceano",
        "fundo:aurora",
        "fundo:areia",
        "fundo:grafite"
    );
    private static final Pattern DATA_URL = Pattern.compile(
        "^data:image/(png|jpeg|webp);base64,([A-Za-z0-9+/]+={0,2})$"
    );

    private Imagens() {}

    public static boolean ehFundoPronto(String valor) {
        return FUNDOS.contains(valor);
    }

    public static String validar(String dataUrl, int limite) {
        var m = DATA_URL.matcher(dataUrl);
        if (!m.matches()) {
            throw ErroNegocio.regra("Envie uma imagem PNG, JPEG ou WebP.");
        }
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(m.group(2));
        } catch (IllegalArgumentException e) {
            throw ErroNegocio.regra("A imagem enviada está corrompida.");
        }
        if (bytes.length > limite) {
            throw ErroNegocio.regra("A imagem passa de " + limite / 1024 + " KB.");
        }
        if (!assinaturaConfere(m.group(1), bytes)) {
            throw ErroNegocio.regra("O conteúdo do arquivo não corresponde a uma imagem válida.");
        }
        return dataUrl;
    }

    // Confere os bytes iniciais de cada formato, sem confiar só no tipo declarado
    private static boolean assinaturaConfere(String tipo, byte[] b) {
        return switch (tipo) {
            case "png" -> b.length > 8 &&
                (b[0] & 0xFF) == 0x89 &&
                b[1] == 'P' &&
                b[2] == 'N' &&
                b[3] == 'G';
            case "jpeg" -> b.length > 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8;
            case "webp" -> b.length > 12 &&
                b[0] == 'R' &&
                b[1] == 'I' &&
                b[2] == 'F' &&
                b[3] == 'F' &&
                b[8] == 'W' &&
                b[9] == 'E' &&
                b[10] == 'B' &&
                b[11] == 'P';
            default -> false;
        };
    }
}
