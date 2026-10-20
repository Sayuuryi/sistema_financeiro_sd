package br.edu.ifgoiano.financeiro.api;

import java.net.URI;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifgoiano.financeiro.api.Dtos.BaixaRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.BaixaResponse;
import br.edu.ifgoiano.financeiro.api.Dtos.ContaDetalhe;
import br.edu.ifgoiano.financeiro.api.Dtos.ContaRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.ContaResponse;
import br.edu.ifgoiano.financeiro.api.Dtos.FiltroContas;
import br.edu.ifgoiano.financeiro.api.Dtos.MotivoRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.Pagina;
import br.edu.ifgoiano.financeiro.servico.ContasService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contas")
@Tag(name = "Títulos e baixas")
public class ContasController {
    private final ContasService service;

    public ContasController(ContasService service) {
        this.service = service;
    }

    @ApiResponse(responseCode = "200", description = "Operação realizada")
    @GetMapping
    public Pagina<ContaResponse> listar(@ParameterObject @ModelAttribute FiltroContas filtro,
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanho) {
        return service.listar(filtro, pagina, tamanho);
    }

    @ApiResponse(responseCode = "200", description = "Operação realizada")
    @GetMapping("/{id}")
    public ContaDetalhe obter(@PathVariable Long id) {
        return service.obter(id);
    }

    @ApiResponse(responseCode = "201", description = "Registro criado")
    @PostMapping
    public ResponseEntity<ContaResponse> criar(@Valid @RequestBody ContaRequest r) {
        var c = service.criar(r);
        return ResponseEntity.created(URI.create("/api/contas/" + c.id())).body(c);
    }

    @ApiResponse(responseCode = "200", description = "Operação realizada")
    @PutMapping("/{id}")
    public ContaResponse editar(@PathVariable Long id, @Valid @RequestBody ContaRequest r) {
        return service.editar(id, r);
    }

    @ApiResponse(responseCode = "204", description = "Registro excluído")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @ApiResponse(responseCode = "200", description = "Título cancelado ou já cancelado")
    @PostMapping("/{id}/cancelamento")
    public ContaResponse cancelar(@PathVariable Long id, @Valid @RequestBody MotivoRequest r) {
        return service.cancelar(id, r);
    }

    @ApiResponse(responseCode = "201", description = "Registro criado")
    @ApiResponse(responseCode = "200", description = "Repetição idempotente: baixa original")
    @PostMapping("/{id}/baixas")
    public ResponseEntity<BaixaResponse> baixar(@PathVariable Long id, @RequestHeader("Idempotency-Key") String key,
            @Valid @RequestBody BaixaRequest r) {
        var result = service.baixar(id, key, r);
        return ResponseEntity.status(result.repeticao() ? 200 : 201).header("Location", "/api/contas/" + id)
                .body(result.baixa());
    }

    @ApiResponse(responseCode = "200", description = "Baixa estornada ou já estornada")
    @PostMapping("/{id}/baixas/{baixaId}/estorno")
    public BaixaResponse estornar(@PathVariable Long id, @PathVariable Long baixaId,
            @Valid @RequestBody MotivoRequest r) {
        return service.estornar(id, baixaId, r);
    }
}
