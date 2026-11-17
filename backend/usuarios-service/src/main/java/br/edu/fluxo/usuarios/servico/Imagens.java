package br.edu.fluxo.usuarios.servico;

import br.edu.fluxo.usuarios.exceptions.ErroNegocio;
import java.util.Set;

// Foto e capa chegam como arquivo; estáticas já vêm reduzidas pelo navegador
public final class Imagens {

    public static final int KB = 1024;
    public static final int LIMITE_MAXIMO = 4 * 1024 * KB;

    public enum Formato {
        PNG("image/png", "png"),
        JPEG("image/jpeg", "jpg"),
        WEBP("image/webp", "webp"),
        GIF("image/gif", "gif");

        public final String mediaType;
        public final String extensao;

        Formato(String mediaType, String extensao) {
            this.mediaType = mediaType;
            this.extensao = extensao;
        }

        public static Formato deMediaType(String mediaType) {
            for (var f : values()) {
                if (f.mediaType.equals(mediaType)) {
                    return f;
                }
            }
            return PNG;
        }
    }

    private static final Set<String> FUNDOS = Set.of(
        "fundo:teal",
        "fundo:oceano",
        "fundo:aurora",
        "fundo:areia",
        "fundo:grafite"
    );

    private Imagens() {}

    public static boolean ehFundoPronto(String valor) {
        return FUNDOS.contains(valor);
    }

    // GIF vai sem redução para manter a animação, por isso tem limite maior
    public static int limite(boolean capa, Formato formato) {
        if (formato == Formato.GIF) {
            return capa ? LIMITE_MAXIMO : 1024 * KB;
        }
        return capa ? 1024 * KB : 400 * KB;
    }

    public static Formato validar(byte[] bytes, String contentType, boolean capa) {
        if (bytes == null || bytes.length == 0) {
            throw ErroNegocio.regra("Envie uma imagem.");
        }
        Formato formato = detectar(bytes);
        String declarado = contentType == null ? "" : contentType.split(";")[0].strip();
        if (!formato.mediaType.equalsIgnoreCase(declarado)) {
            throw ErroNegocio.regra("O conteúdo do arquivo não corresponde ao tipo informado.");
        }
        int limite = limite(capa, formato);
        if (bytes.length > limite) {
            throw ErroNegocio.regra("A imagem passa de " + limite / KB + " KB.");
        }
        return formato;
    }

    // Confere os bytes iniciais de cada formato, sem confiar só no tipo declarado
    private static Formato detectar(byte[] b) {
        if (b.length > 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return Formato.PNG;
        }
        if (b.length > 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8) {
            return Formato.JPEG;
        }
        if (
            b.length > 12 &&
            b[0] == 'R' &&
            b[1] == 'I' &&
            b[2] == 'F' &&
            b[3] == 'F' &&
            b[8] == 'W' &&
            b[9] == 'E' &&
            b[10] == 'B' &&
            b[11] == 'P'
        ) {
            return Formato.WEBP;
        }
        if (
            b.length > 6 &&
            b[0] == 'G' &&
            b[1] == 'I' &&
            b[2] == 'F' &&
            b[3] == '8' &&
            (b[4] == '7' || b[4] == '9') &&
            b[5] == 'a'
        ) {
            return Formato.GIF;
        }
        throw ErroNegocio.regra("Envie uma imagem PNG, JPEG, WebP ou GIF.");
    }
}
