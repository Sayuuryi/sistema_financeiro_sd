package br.edu.fluxo.receber.api;

import br.edu.fluxo.receber.api.Dtos.*;
import br.edu.fluxo.receber.dominio.TipoConta;
import br.edu.fluxo.receber.repositorio.BaixaRepository;
import br.edu.fluxo.receber.servico.*;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/interno")
public class OperacoesInternasController {

    private final ContasService contas;
    private final BaixaRepository baixas;

    public OperacoesInternasController(ContasService contas, BaixaRepository baixas) {
        this.contas = contas;
        this.baixas = baixas;
    }

    @PostMapping("/previsoes/{origem}/conta")
    public ResponseEntity<ContaResponse> converter(
        @PathVariable Long origem,
        @Valid @RequestBody ContaRequest request
    ) {
        var r = contas.criarDePrevisao(origem, request);
        return ResponseEntity.status(r.repeticao() ? 200 : 201).body(r.conta());
    }

    @GetMapping("/saldos-bancarios")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<SaldoBancario> saldos() {
        var result = new TreeMap<Long, BigDecimal>();
        for (var b : baixas.findByEstornadaFalse()) {
            if (b.getContaBancariaId() != null && b.getConta().getTipo() == contas.tipo()) {
                var valor =
                    b.getConta().getTipo() == TipoConta.RECEBER
                        ? b.valorMovimentado()
                        : b.valorMovimentado().negate();
                result.merge(b.getContaBancariaId(), valor, BigDecimal::add);
            }
        }
        return result
            .entrySet()
            .stream()
            .map(e -> new SaldoBancario(e.getKey(), e.getValue()))
            .toList();
    }
}
