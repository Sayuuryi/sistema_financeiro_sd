package br.edu.fluxo.pagar.seguranca;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

// Token de sessão (JWT HS256) emitido pelo serviço de usuários
public final class Token {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final String CABECALHO = B64.encodeToString(
        "{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(UTF_8)
    );

    private Token() {}

    public static String emitir(long usuarioId, Instant expira, String segredo) {
        String corpo = B64.encodeToString(
            (
                "{\"sub\":\"" +
                usuarioId +
                "\",\"exp\":" +
                expira.getEpochSecond() +
                ",\"jti\":\"" +
                identificador() +
                "\"}"
            ).getBytes(UTF_8)
        );
        String conteudo = CABECALHO + "." + corpo;
        return conteudo + "." + B64.encodeToString(assinar(conteudo, segredo));
    }

    // Torna único cada token, mesmo com dois logins no mesmo segundo
    private static String identificador() {
        var bytes = new byte[12];
        ALEATORIO.nextBytes(bytes);
        return B64.encodeToString(bytes);
    }

    public static Optional<Long> validar(String token, String segredo, Instant agora) {
        if (token == null) {
            return Optional.empty();
        }
        String[] partes = token.split("\\.", -1);
        if (partes.length != 3 || !partes[0].equals(CABECALHO)) {
            return Optional.empty();
        }
        try {
            byte[] assinatura = Base64.getUrlDecoder().decode(partes[2]);
            if (!MessageDigest.isEqual(assinatura, assinar(partes[0] + "." + partes[1], segredo))) {
                return Optional.empty();
            }
            var corpo = JSON.readTree(Base64.getUrlDecoder().decode(partes[1]));
            if (corpo.path("exp").asLong(0) <= agora.getEpochSecond()) {
                return Optional.empty();
            }
            long id = Long.parseLong(corpo.path("sub").asText());
            return id > 0 ? Optional.of(id) : Optional.empty();
        } catch (IllegalArgumentException | IOException e) {
            return Optional.empty();
        }
    }

    public static String exigirSegredo(String segredo) {
        if (segredo == null || segredo.getBytes(UTF_8).length < 32) {
            throw new IllegalStateException("TOKEN_SEGREDO precisa ter pelo menos 32 bytes.");
        }
        return segredo;
    }

    private static byte[] assinar(String conteudo, String segredo) {
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(segredo.getBytes(UTF_8), "HmacSHA256"));
            return mac.doFinal(conteudo.getBytes(UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
