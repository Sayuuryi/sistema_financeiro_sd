package br.edu.fluxo.pagar.api;

import br.edu.fluxo.pagar.api.Dtos.*;
import br.edu.fluxo.pagar.servico.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/interno/consultas")
@Tag(name = "Consultas financeiras")
public class ConsultasInternasController {

    private final ConsultasFinanceirasService service;
    private final ContasService contas;

    public ConsultasInternasController(ConsultasFinanceirasService service, ContasService contas) {
        this.service = service;
        this.contas = contas;
    }

    @ApiResponse(responseCode = "200", description = "Operação realizada")
    @GetMapping("/sistema")
    public Map<String, Object> sistema() {
        return Map.of("dataAtual", contas.hoje(), "versao", "2.0.0", "ambiente", "Local");
    }

    @ApiResponse(responseCode = "200", description = "Operação realizada")
    @GetMapping("/resumo")
    public ResumoFinanceiro resumo(
        @RequestParam(required = false) LocalDate inicio,
        @RequestParam(required = false) LocalDate fim
    ) {
        return service.resumo(inicio, fim);
    }

    @ApiResponse(responseCode = "200", description = "Operação realizada")
    @GetMapping("/relatorios/movimentos")
    public List<MovimentoResponse> movimentos(
        @RequestParam(required = false) LocalDate inicio,
        @RequestParam(required = false) LocalDate fim
    ) {
        return service.movimentos(inicio, fim);
    }

    @ApiResponse(responseCode = "200", description = "Operação realizada")
    @GetMapping("/relatorios/contas.csv")
    public ResponseEntity<String> contasCsv(@ParameterObject @ModelAttribute FiltroContas filtro) {
        return csv("contas.csv", service.contasCsv(filtro));
    }

    @ApiResponse(responseCode = "200", description = "Operação realizada")
    @GetMapping("/relatorios/movimentos.csv")
    public ResponseEntity<String> movimentosCsv(
        @RequestParam(required = false) LocalDate inicio,
        @RequestParam(required = false) LocalDate fim
    ) {
        return csv("movimentos.csv", service.movimentosCsv(inicio, fim));
    }

    @GetMapping("/contas")
    public List<ContaResponse> contas(@ParameterObject @ModelAttribute FiltroContas filtro) {
        return contas.exportar(filtro);
    }

    private ResponseEntity<String> csv(String nome, String content) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nome + "\"")
            .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
            .body(content);
    }
}
