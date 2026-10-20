package br.edu.ifgoiano.financeiro.api;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifgoiano.financeiro.api.Dtos.*;
import br.edu.ifgoiano.financeiro.api.Dtos.ContaResponse;
import br.edu.ifgoiano.financeiro.api.Dtos.DespesaRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.DespesaResponse;
import br.edu.ifgoiano.financeiro.api.Dtos.GerarContaRequest;
import br.edu.ifgoiano.financeiro.servico.DespesasService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/despesas-previstas")
@Tag(name = "Despesas previstas")
public class DespesasController {
    private final DespesasService service;

    public DespesasController(DespesasService service) {
        this.service = service;
    }

    @ApiResponse(responseCode = "200", description = "Operação realizada")
    @GetMapping
    public List<DespesaResponse> listar() {
        return service.listar();
    }

    @ApiResponse(responseCode = "200", description = "Operação realizada")
    @GetMapping("/{id}")
    public DespesaResponse obter(@PathVariable Long id) {
        return service.obter(id);
    }

    @ApiResponse(responseCode = "201", description = "Registro criado")
    @PostMapping
    public ResponseEntity<DespesaResponse> criar(@Valid @RequestBody DespesaRequest r) {
        var d = service.salvar(null, r);
        return ResponseEntity.created(URI.create("/api/despesas-previstas/" + d.id())).body(d);
    }

    @ApiResponse(responseCode = "200", description = "Operação realizada")
    @PutMapping("/{id}")
    public DespesaResponse editar(@PathVariable Long id, @Valid @RequestBody DespesaRequest r) {
        return service.salvar(id, r);
    }

    @ApiResponse(responseCode = "204", description = "Registro excluído")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @ApiResponse(responseCode = "201", description = "Registro criado")
    @PostMapping("/{id}/gerar-conta")
    public ResponseEntity<ContaResponse> gerar(@PathVariable Long id, @Valid @RequestBody GerarContaRequest r) {
        var c = service.gerarConta(id, r);
        return ResponseEntity.created(URI.create("/api/contas/" + c.id())).body(c);
    }
}
