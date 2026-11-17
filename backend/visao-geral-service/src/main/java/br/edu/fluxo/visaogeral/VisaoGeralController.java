package br.edu.fluxo.visaogeral;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/visao-geral")
public class VisaoGeralController {

    private final LeitorRemoto leitor;
    private final ResumoService resumo;
    private final ObjectMapper json;

    public VisaoGeralController(LeitorRemoto leitor, ResumoService resumo, ObjectMapper json) {
        this.leitor = leitor;
        this.resumo = resumo;
        this.json = json;
    }

    @GetMapping
    public ObjectNode consultar(
        @RequestParam(required = false) LocalDate inicio,
        @RequestParam(required = false) LocalDate fim
    ) {
        var result = resumo.consultar(inicio, fim);
        var fontes = leitor.paralelo(
            Map.of(
                "pagar",
                new LeitorRemoto.Destino("pagar", "/api/contas?tamanho=100"),
                "receber",
                new LeitorRemoto.Destino("receber", "/api/contas?tamanho=100"),
                "bancos",
                new LeitorRemoto.Destino("cadastros", "/api/contas-bancarias?incluirSaldo=false"),
                "saldoPagar",
                new LeitorRemoto.Destino("pagar", "/interno/saldos-bancarios"),
                "saldoReceber",
                new LeitorRemoto.Destino("receber", "/interno/saldos-bancarios")
            )
        );
        var contas = json.createArrayNode();
        for (var name : List.of("pagar", "receber")) {
            var fonte = fontes.get(name);
            if (fonte.disponivel()) {
                fonte.dados().path("itens").forEach(contas::add);
            }
        }
        result.set("contas", contas);
        var bancos = json.createArrayNode();
        var movimentos = new HashMap<Long, BigDecimal>();
        boolean saldoCompleto =
            fontes.get("saldoPagar").disponivel() && fontes.get("saldoReceber").disponivel();
        if (saldoCompleto) {
            for (var name : List.of("saldoPagar", "saldoReceber")) {
                for (var m : fontes.get(name).dados()) {
                    movimentos.merge(
                        m.path("contaBancariaId").asLong(),
                        m.path("movimentoLiquido").decimalValue(),
                        BigDecimal::add
                    );
                }
            }
        }
        if (fontes.get("bancos").disponivel()) {
            for (var banco : fontes.get("bancos").dados()) {
                ObjectNode copy = banco.deepCopy();
                if (saldoCompleto) {
                    copy.put(
                        "saldoAtual",
                        banco
                            .path("saldoInicial")
                            .decimalValue()
                            .add(
                                movimentos.getOrDefault(banco.path("id").asLong(), BigDecimal.ZERO)
                            )
                    );
                } else {
                    copy.putNull("saldoAtual");
                }
                bancos.add(copy);
            }
        }
        result.set("contasBancarias", bancos);
        var avisos = new LinkedHashSet<String>();
        result.path("avisos").forEach(a -> avisos.add(a.asText()));
        leitor.avisos(fontes.values()).forEach(a -> avisos.add(a.asText()));
        var lista = json.createArrayNode();
        avisos.forEach(lista::add);
        result.set("avisos", lista);
        result.put("parcial", !lista.isEmpty());
        return result;
    }
}
