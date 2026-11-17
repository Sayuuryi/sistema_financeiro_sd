package br.edu.fluxo.usuarios.api;

import br.edu.fluxo.usuarios.api.Dtos.*;
import br.edu.fluxo.usuarios.exceptions.ErroNegocio;
import br.edu.fluxo.usuarios.servico.Imagens;
import br.edu.fluxo.usuarios.servico.UsuariosService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.Duration;
import java.util.Locale;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários e sessões")
public class UsuariosController {

    private final UsuariosService service;

    public UsuariosController(UsuariosService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessaoResponse cadastrar(@Valid @RequestBody CadastroRequest r) {
        return service.cadastrar(r);
    }

    @PostMapping("/login")
    public SessaoResponse entrar(@Valid @RequestBody LoginRequest r) {
        return service.entrar(r);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sair(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a) {
        if (a != null) {
            service.sair(a);
        }
    }

    @GetMapping("/eu")
    public PerfilResponse perfil(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a
    ) {
        return service.perfil(a);
    }

    @PutMapping("/eu")
    public PerfilResponse atualizar(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
        @Valid @RequestBody PerfilRequest r
    ) {
        return service.atualizar(a, r);
    }

    @PutMapping("/eu/foto")
    public PerfilResponse foto(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
        HttpServletRequest req
    ) throws IOException {
        return service.trocarImagem(a, "FOTO", lerArquivo(req), req.getContentType());
    }

    @DeleteMapping("/eu/foto")
    public PerfilResponse removerFoto(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a
    ) {
        return service.removerImagem(a, "FOTO");
    }

    @PutMapping("/eu/capa")
    public PerfilResponse capa(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
        HttpServletRequest req
    ) throws IOException {
        return service.trocarImagem(a, "CAPA", lerArquivo(req), req.getContentType());
    }

    @PutMapping("/eu/capa/fundo")
    public PerfilResponse fundo(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
        @Valid @RequestBody FundoRequest r
    ) {
        return service.escolherFundo(a, r.fundo());
    }

    @DeleteMapping("/eu/capa")
    public PerfilResponse removerCapa(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a
    ) {
        return service.removerImagem(a, "CAPA");
    }

    // Endereço com o hash do conteúdo: público para <img> e guardado em cache sem prazo
    @GetMapping("/imagens/{usuarioId}/{tipo}/{arquivo}")
    public ResponseEntity<byte[]> imagem(
        @PathVariable long usuarioId,
        @PathVariable String tipo,
        @PathVariable String arquivo
    ) {
        String hash = arquivo.contains(".") ? arquivo.substring(0, arquivo.indexOf('.')) : arquivo;
        if (!(tipo.equals("foto") || tipo.equals("capa")) || !hash.matches("[0-9a-f]{64}")) {
            return ResponseEntity.notFound().build();
        }
        return service
            .imagem(usuarioId, tipo.toUpperCase(Locale.ROOT), hash)
            .map(i ->
                ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(i.getMediaType()))
                    .cacheControl(
                        CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable()
                    )
                    .header("X-Content-Type-Options", "nosniff")
                    .body(i.getConteudo())
            )
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Lê no máximo um byte além do limite, para recusar arquivos grandes sem guardá-los inteiros
    private static byte[] lerArquivo(HttpServletRequest req) throws IOException {
        byte[] bytes = req.getInputStream().readNBytes(Imagens.LIMITE_MAXIMO + 1);
        if (bytes.length > Imagens.LIMITE_MAXIMO) {
            throw ErroNegocio.regra(
                "A imagem passa de " + Imagens.LIMITE_MAXIMO / Imagens.KB + " KB."
            );
        }
        return bytes;
    }

    @PutMapping("/eu/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void trocarSenha(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
        @Valid @RequestBody SenhaRequest r
    ) {
        service.trocarSenha(a, r);
    }
}
