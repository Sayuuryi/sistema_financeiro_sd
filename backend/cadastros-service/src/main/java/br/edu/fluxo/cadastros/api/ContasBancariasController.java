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
@RequestMapping("/api/contas-bancarias")
@Tag(name = "Cadastros")
public class ContasBancariasController {

    private final CadastrosService service;

    public ContasBancariasController(CadastrosService service) {
        this.service = service;
    }

    @ApiResponse(responseCode = "200", description = "Operação realizada")
    @GetMapping
    public List<ContaBancariaResponse> listar(
        @RequestParam(defaultValue = "true") boolean incluirSaldo
    ) {
        return service.listarContasBancarias(incluirSaldo);
    }

    @ApiResponse(responseCode = "201", description = "Registro criado")
    @PostMapping
    public ResponseEntity<ContaBancariaResponse> criar(@Valid @RequestBody ContaBancariaRequest r) {
        var c = service.salvarContaBancaria(null, r);
        return ResponseEntity.created(URI.create("/api/contas-bancarias/" + c.id())).body(c);
    }

    @ApiResponse(responseCode = "200", description = "Operação realizada")
    @PutMapping("/{id}")
    public ContaBancariaResponse editar(
        @PathVariable Long id,
        @Valid @RequestBody ContaBancariaRequest r
    ) {
        return service.salvarContaBancaria(id, r);
    }

    @ApiResponse(responseCode = "204", description = "Registro excluído")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluirContaBancaria(id);
        return ResponseEntity.noContent().build();
    }
}
