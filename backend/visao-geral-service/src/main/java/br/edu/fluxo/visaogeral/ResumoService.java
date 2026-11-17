package br.edu.fluxo.visaogeral;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class ResumoService {

    private final LeitorRemoto leitor;
    private final ObjectMapper json;

    public ResumoService(LeitorRemoto leitor, ObjectMapper json) {
        this.leitor = leitor;
        this.json = json;
    }

    public ObjectNode consultar(LocalDate inicio, LocalDate fim) {
        var periodo = LeitorRemoto.periodo(inicio, fim);
        var fontes = leitor.paralelo(
            Map.of(
                "pagar",
                new LeitorRemoto.Destino("pagar", "/interno/consultas/resumo?" + periodo),
                "receber",
                new LeitorRemoto.Destino("receber", "/interno/consultas/resumo?" + periodo),
                "planejamento",
                new LeitorRemoto.Destino(
                    "planejamento",
                    "/interno/despesas-previstas/total?" + periodo
                )
            )
        );
        var pagar = fontes.get("pagar");
        var receber = fontes.get("receber");
        var previsto = fontes.get("planejamento");
        var r = json.createObjectNode();
        r.put("inicio", periodo.substring(7, 17));
        r.put("fim", periodo.substring(periodo.indexOf("&fim=") + 5));
        campo(r, "aPagar", pagar, "aPagar");
        campo(r, "pago", pagar, "pago");
        campo(r, "aReceber", receber, "aReceber");
        campo(r, "recebido", receber, "recebido");
        if (pagar.disponivel() && receber.disponivel()) {
            r.put(
                "saldoMovimentado",
                receber
                    .dados()
                    .path("recebido")
                    .decimalValue()
                    .subtract(pagar.dados().path("pago").decimalValue())
            );
            r.put(
                "vencidas",
                pagar.dados().path("vencidas").asLong() + receber.dados().path("vencidas").asLong()
            );
        } else {
            r.putNull("saldoMovimentado");
            r.putNull("vencidas");
        }
        r.set("despesasPrevistas", previsto.disponivel() ? previsto.dados() : NullNode.instance);
        r.set("avisos", leitor.avisos(fontes.values()));
        r.put("parcial", !r.path("avisos").isEmpty());
        return r;
    }

    private void campo(ObjectNode r, String nome, LeitorRemoto.Resultado fonte, String chave) {
        r.set(nome, fonte.disponivel() ? fonte.dados().path(chave) : NullNode.instance);
    }
}
