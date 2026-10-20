package br.edu.ifgoiano.financeiro.api;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifgoiano.financeiro.api.Dtos.*;
import br.edu.ifgoiano.financeiro.api.Dtos.FiltroContas;
import br.edu.ifgoiano.financeiro.api.Dtos.MovimentoResponse;
import br.edu.ifgoiano.financeiro.api.Dtos.ResumoFinanceiro;
import br.edu.ifgoiano.financeiro.servico.ContasService;
import br.edu.ifgoiano.financeiro.servico.RelatoriosService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Consultas financeiras")
public class RelatoriosController {
    private final RelatoriosService service;
    private final ContasService contas;

    public RelatoriosController(RelatoriosService service, ContasService contas) {
        this.service = service;
        this.contas = contas;
    }

    @ApiResponse(responseCode = "200", description = "Operação realizada")
    @GetMapping("/sistema")
    public Map<String, Object> sistema() {
        return Map.of("dataAtual", contas.hoje(), "versao", "1.2.0", "ambiente",
                "Protótipo acadêmico local — dados demonstrativos fictícios");
    }

    @ApiResponse(responseCode = "200", description = "Operação realizada")
    @GetMapping("/relatorios/resumo")
    public ResumoFinanceiro resumo(@RequestParam(required = false) LocalDate inicio,
            @RequestParam(required = false) LocalDate fim) {
        return service.resumo(inicio, fim);
    }

    @ApiResponse(responseCode = "200", description = "Operação realizada")
    @GetMapping("/relatorios/movimentos")
    public List<MovimentoResponse> movimentos(@RequestParam(required = false) LocalDate inicio,
            @RequestParam(required = false) LocalDate fim) {
        return service.movimentos(inicio, fim);
    }

    @ApiResponse(responseCode = "200", description = "Operação realizada")
    @GetMapping("/relatorios/contas.csv")
    public ResponseEntity<String> contasCsv(@ParameterObject @ModelAttribute FiltroContas filtro) {
        return csv("contas.csv", service.contasCsv(filtro));
    }

    @ApiResponse(responseCode = "200", description = "Operação realizada")
    @GetMapping("/relatorios/movimentos.csv")
    public ResponseEntity<String> movimentosCsv(@RequestParam(required = false) LocalDate inicio,
            @RequestParam(required = false) LocalDate fim) {
        return csv("movimentos.csv", service.movimentosCsv(inicio, fim));
    }

    private ResponseEntity<String> csv(String nome, String content) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nome + "\"")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8")).body(content);
    }
}
