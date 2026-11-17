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
@RequestMapping("/api/pessoas")
@Tag(name = "Cadastros")
public class PessoasController {

    private final CadastrosService service;

    public PessoasController(CadastrosService service) {
        this.service = service;
    }

    @ApiResponse(responseCode = "200", description = "Operação realizada")
    @GetMapping
    public List<PessoaResponse> listar(
        @RequestParam(required = false) String busca,
        @RequestParam(required = false) TipoPessoa tipo,
        @RequestParam(required = false) Boolean ativos
    ) {
        return service.listarPessoas(busca, tipo, ativos);
    }

    @ApiResponse(responseCode = "201", description = "Registro criado")
    @PostMapping
    public ResponseEntity<PessoaResponse> criar(@Valid @RequestBody PessoaRequest r) {
        var c = service.salvarPessoa(null, r);
        return ResponseEntity.created(URI.create("/api/pessoas/" + c.id())).body(c);
    }

    @ApiResponse(responseCode = "200", description = "Operação realizada")
    @PutMapping("/{id}")
    public PessoaResponse editar(@PathVariable Long id, @Valid @RequestBody PessoaRequest r) {
        return service.salvarPessoa(id, r);
    }

    @ApiResponse(responseCode = "204", description = "Registro excluído")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluirPessoa(id);
        return ResponseEntity.noContent().build();
    }
}
