package br.edu.fluxo.usuarios.api;

import br.edu.fluxo.usuarios.servico.UsuariosService;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

// Os outros serviços perguntam aqui se a sessão do token ainda está aberta
@RestController
@RequestMapping("/interno/sessoes")
public class SessoesInternasController {

    private final UsuariosService service;

    public SessoesInternasController(UsuariosService service) {
        this.service = service;
    }

    @GetMapping("/atual")
    public Map<String, Long> atual(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String a
    ) {
        return Map.of("usuarioId", service.usuarioDaSessao(a));
    }
}
