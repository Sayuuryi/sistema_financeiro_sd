package br.edu.fluxo.planejamento.api;

import br.edu.fluxo.planejamento.servico.DespesasService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.*;

@RestController
public class TotaisController {

    private final DespesasService service;

    public TotaisController(DespesasService service) {
        this.service = service;
    }

    @GetMapping("/interno/despesas-previstas/total")
    public BigDecimal total(@RequestParam LocalDate inicio, @RequestParam LocalDate fim) {
        return service.total(inicio, fim);
    }
}
