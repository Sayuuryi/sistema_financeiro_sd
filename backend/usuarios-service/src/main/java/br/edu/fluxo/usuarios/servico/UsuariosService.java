package br.edu.fluxo.usuarios.servico;

import br.edu.fluxo.usuarios.api.Dtos.*;
import br.edu.fluxo.usuarios.dominio.*;
import br.edu.fluxo.usuarios.exceptions.ErroNegocio;
import br.edu.fluxo.usuarios.repositorio.*;
import br.edu.fluxo.usuarios.seguranca.Token;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuariosService {

    private static final String CREDENCIAIS = "E-mail ou senha incorretos.";
    private static final String SEM_SESSAO = "Sessão ausente ou expirada. Entre novamente.";

    private final UsuarioRepository usuarios;
    private final SessaoRepository sessoes;
    private final PasswordEncoder senhas;
    private final Clock clock;
    private final Duration duracao;
    private final String segredo;
    // Hash fixo para comparar mesmo quando o e-mail não existe
    private final String hashFalso;

    public UsuariosService(
        UsuarioRepository usuarios,
        SessaoRepository sessoes,
        PasswordEncoder senhas,
        Clock clock,
        @Value("${sessao.duracao-horas}") long horas,
        @Value("${seguranca.token-segredo}") String segredo
    ) {
        this.segredo = Token.exigirSegredo(segredo);
        this.usuarios = usuarios;
        this.sessoes = sessoes;
        this.senhas = senhas;
        this.clock = clock;
        this.duracao = Duration.ofHours(horas);
        this.hashFalso = senhas.encode(UUID.randomUUID().toString());
    }

    @Transactional
    public SessaoResponse cadastrar(CadastroRequest r) {
        String email = normalizar(r.email());
        if (usuarios.existsByEmail(email)) {
            throw ErroNegocio.conflito("Já existe uma conta com este e-mail.");
        }
        var u = new Usuario();
        u.setNome(r.nome().strip());
        u.setEmail(email);
        u.setSenhaHash(senhas.encode(senhaValida(r.senha())));
        u.setCriadoEm(agora());
        u.setUltimoAcessoEm(agora());
        usuarios.saveAndFlush(u);
        return abrirSessao(u);
    }

    @Transactional
    public SessaoResponse entrar(LoginRequest r) {
        var u = usuarios.findByEmail(normalizar(r.email())).orElse(null);
        boolean confere =
            bytes(r.senha()) <= 72 &&
            senhas.matches(r.senha(), u == null ? hashFalso : u.getSenhaHash());
        if (u == null || !confere) {
            throw ErroNegocio.naoAutorizado(CREDENCIAIS);
        }
        sessoes.apagarExpiradas(agora());
        u.setUltimoAcessoEm(agora());
        return abrirSessao(u);
    }

    @Transactional
    public void sair(String autorizacao) {
        sessoes.findByTokenHash(hash(token(autorizacao))).ifPresent(sessoes::delete);
    }

    @Transactional(readOnly = true)
    public long usuarioDaSessao(String autorizacao) {
        return sessao(autorizacao).getUsuario().getId();
    }

    @Transactional(readOnly = true)
    public PerfilResponse perfil(String autorizacao) {
        return PerfilResponse.de(sessao(autorizacao).getUsuario());
    }

    @Transactional
    public PerfilResponse atualizar(String autorizacao, PerfilRequest r) {
        var u = sessao(autorizacao).getUsuario();
        if (r.versao() != u.getVersao()) {
            throw ErroNegocio.conflito(
                "O perfil foi alterado em outra sessão. Atualize a página antes de salvar."
            );
        }
        String email = normalizar(r.email());
        if (usuarios.existsByEmailAndIdNot(email, u.getId())) {
            throw ErroNegocio.conflito("Já existe uma conta com este e-mail.");
        }
        u.setNome(r.nome().strip());
        u.setEmail(email);
        u.setTelefone(opcional(r.telefone()));
        u.setCargo(opcional(r.cargo()));
        u.setEmpresa(opcional(r.empresa()));
        usuarios.saveAndFlush(u);
        return PerfilResponse.de(u);
    }

    @Transactional
    public PerfilResponse trocarFoto(String autorizacao, String imagem) {
        var u = sessao(autorizacao).getUsuario();
        u.setFoto(imagem == null ? null : Imagens.validar(imagem, Imagens.LIMITE_FOTO));
        usuarios.saveAndFlush(u);
        return PerfilResponse.de(u);
    }

    @Transactional
    public PerfilResponse trocarCapa(String autorizacao, String capa) {
        var u = sessao(autorizacao).getUsuario();
        if (capa == null || Imagens.ehFundoPronto(capa)) {
            u.setCapa(capa);
        } else {
            u.setCapa(Imagens.validar(capa, Imagens.LIMITE_CAPA));
        }
        usuarios.saveAndFlush(u);
        return PerfilResponse.de(u);
    }

    @Transactional
    public void trocarSenha(String autorizacao, SenhaRequest r) {
        var s = sessao(autorizacao);
        var u = s.getUsuario();
        if (bytes(r.senhaAtual()) > 72 || !senhas.matches(r.senhaAtual(), u.getSenhaHash())) {
            throw ErroNegocio.regra("A senha atual não confere.");
        }
        u.setSenhaHash(senhas.encode(senhaValida(r.novaSenha())));
        // Troca de senha encerra as outras sessões abertas
        sessoes.apagarOutras(u.getId(), s.getId());
    }

    private Sessao sessao(String autorizacao) {
        var s = sessoes
            .findByTokenHash(hash(token(autorizacao)))
            .orElseThrow(() -> ErroNegocio.naoAutorizado(SEM_SESSAO));
        if (s.getExpiraEm().isBefore(agora())) {
            throw ErroNegocio.naoAutorizado(SEM_SESSAO);
        }
        return s;
    }

    private SessaoResponse abrirSessao(Usuario u) {
        var expira = agora().plus(duracao);
        String token = Token.emitir(u.getId(), expira.toInstant(), segredo);
        var s = new Sessao();
        s.setUsuario(u);
        s.setTokenHash(hash(token));
        s.setCriadaEm(agora());
        s.setExpiraEm(expira);
        sessoes.save(s);
        return new SessaoResponse(token, s.getExpiraEm(), PerfilResponse.de(u));
    }

    private String token(String autorizacao) {
        if (autorizacao == null || !autorizacao.startsWith("Bearer ")) {
            throw ErroNegocio.naoAutorizado(SEM_SESSAO);
        }
        String t = autorizacao.substring(7).strip();
        if (t.isEmpty()) {
            throw ErroNegocio.naoAutorizado(SEM_SESSAO);
        }
        return t;
    }

    private static String hash(String token) {
        try {
            return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))
            );
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // O BCrypt só considera os primeiros 72 bytes
    private static String senhaValida(String senha) {
        if (bytes(senha) > 72) {
            throw ErroNegocio.regra(
                "A senha passa do limite de 72 bytes. Use uma senha mais curta."
            );
        }
        return senha;
    }

    private static int bytes(String senha) {
        return senha.getBytes(StandardCharsets.UTF_8).length;
    }

    private static String normalizar(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    private static String opcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.strip();
    }

    private OffsetDateTime agora() {
        return OffsetDateTime.now(clock);
    }
}
