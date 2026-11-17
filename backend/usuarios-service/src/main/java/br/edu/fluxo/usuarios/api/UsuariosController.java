package br.edu.fluxo.usuarios.api;

import br.edu.fluxo.usuarios.api.Dtos.*;
import br.edu.fluxo.usuarios.servico.UsuariosService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
        @RequestBody ImagemRequest r
    ) {
        return service.trocarFoto(a, r.imagem());
    }

    @PutMapping("/eu/capa")
    public PerfilResponse capa(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a,
        @RequestBody ImagemRequest r
    ) {
        return service.trocarCapa(a, r.imagem());
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
