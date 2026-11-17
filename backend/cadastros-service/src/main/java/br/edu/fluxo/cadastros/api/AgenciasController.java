package br.edu.fluxo.cadastros.api;

import br.edu.fluxo.cadastros.api.Dtos.*;
import br.edu.fluxo.cadastros.dominio.*;
import br.edu.fluxo.cadastros.servico.CadastrosService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agencias")
@Tag(name = "Cadastros")
public class AgenciasController {

    private final CadastrosService service;

    public AgenciasController(CadastrosService service) {
        this.service = service;
    }

    @ApiResponse(responseCode = "200", description = "Operação realizada")
    @GetMapping
    public List<AgenciaResponse> listar() {
        return service.listarAgencias();
    }

    @ApiResponse(responseCode = "201", description = "Registro criado")
    @PostMapping
    public ResponseEntity<AgenciaResponse> criar(@Valid @RequestBody AgenciaRequest r) {
        var c = service.salvarAgencia(null, r);
        return ResponseEntity.created(URI.create("/api/agencias/" + c.id())).body(c);
    }

    @ApiResponse(responseCode = "200", description = "Operação realizada")
    @PutMapping("/{id}")
    public AgenciaResponse editar(@PathVariable Long id, @Valid @RequestBody AgenciaRequest r) {
        return service.salvarAgencia(id, r);
    }

    @ApiResponse(responseCode = "204", description = "Registro excluído")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluirAgencia(id);
        return ResponseEntity.noContent().build();
    }
}
