package br.edu.fluxo.relatorios;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import java.time.LocalDate;
import java.util.*;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/relatorios")
public class RelatoriosController {

    private final LeitorRemoto leitor;
    private final ResumoService resumo;
    private final ObjectMapper json;

    public RelatoriosController(LeitorRemoto leitor, ResumoService resumo, ObjectMapper json) {
        this.leitor = leitor;
        this.resumo = resumo;
        this.json = json;
    }

    @GetMapping("/resumo")
    public ObjectNode resumo(
        @RequestParam(required = false) LocalDate inicio,
        @RequestParam(required = false) LocalDate fim
    ) {
        return resumo.consultar(inicio, fim);
    }

    @GetMapping("/movimentos")
    public ObjectNode movimentos(
        @RequestParam(required = false) LocalDate inicio,
        @RequestParam(required = false) LocalDate fim
    ) {
        var periodo = LeitorRemoto.periodo(inicio, fim);
        var fontes = leitor.paralelo(
            Map.of(
                "pagar",
                new LeitorRemoto.Destino(
                    "pagar",
                    "/interno/consultas/relatorios/movimentos?" + periodo
                ),
                "receber",
                new LeitorRemoto.Destino(
                    "receber",
                    "/interno/consultas/relatorios/movimentos?" + periodo
                )
            )
        );
        return lista(fontes.values());
    }

    private ObjectNode lista(Collection<LeitorRemoto.Resultado> fontes) {
        var result = json.createObjectNode();
        var itens = json.createArrayNode();
        var ordenados = new ArrayList<JsonNode>();
        fontes
            .stream()
            .filter(LeitorRemoto.Resultado::disponivel)
            .forEach(f -> f.dados().forEach(ordenados::add));
        if (!ordenados.isEmpty()) {
            var campo = ordenados.get(0).has("data") ? "data" : "vencimento";
            Comparator<JsonNode> ordem = Comparator.comparing(item -> item.path(campo).asText());
            if (campo.equals("data")) {
                ordem = ordem.reversed();
            }
            ordenados.sort(ordem.thenComparingLong(item -> item.path("id").asLong()));
        }
        ordenados.forEach(itens::add);
        result.set("itens", itens);
        result.set("avisos", leitor.avisos(fontes));
        result.put("parcial", !result.path("avisos").isEmpty());
        return result;
    }

    // Exportações são pesadas: poucas ao mesmo tempo, as demais recebem 503 na hora
    @GetMapping("/contas.csv")
    @Bulkhead(name = "exportacao", type = Bulkhead.Type.SEMAPHORE)
    public ResponseEntity<String> contasCsv(@RequestParam MultiValueMap<String, String> filtros) {
        String tipo = filtros.getFirst("tipo");
        if (tipo != null && !Set.of("PAGAR", "RECEBER").contains(tipo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de conta inválido.");
        }
        String query = UriComponentsBuilder.newInstance()
            .queryParams(filtros)
            .build()
            .encode()
            .toUriString();
        var destinos = new LinkedHashMap<String, LeitorRemoto.Destino>();
        if (tipo == null || tipo.equals("PAGAR")) {
            destinos.put(
                "pagar",
                new LeitorRemoto.Destino("pagar", "/interno/consultas/contas" + query)
            );
        }
        if (tipo == null || tipo.equals("RECEBER")) {
            destinos.put(
                "receber",
                new LeitorRemoto.Destino("receber", "/interno/consultas/contas" + query)
            );
        }
        var fontes = leitor.paralelo(destinos);
        leitor.exigirCompleto(fontes.values());
        var itens = lista(fontes.values()).path("itens");
        return csv(
            "contas.csv",
            itens,
            new String[] {
                "ID",
                "Tipo",
                "Número",
                "Pessoa",
                "Categoria",
                "Descrição",
                "Emissão",
                "Vencimento",
                "Valor original",
                "Principal baixado",
                "Saldo",
                "Situação",
            },
            new String[] {
                "id",
                "tipo",
                "numeroTitulo",
                "pessoa.nome",
                "categoria.nome",
                "descricao",
                "emissao",
                "vencimento",
                "valor",
                "totalBaixado",
                "saldo",
                "situacao",
            }
        );
    }

    @GetMapping("/movimentos.csv")
    @Bulkhead(name = "exportacao", type = Bulkhead.Type.SEMAPHORE)
    public ResponseEntity<String> movimentosCsv(
        @RequestParam(required = false) LocalDate inicio,
        @RequestParam(required = false) LocalDate fim
    ) {
        var result = movimentos(inicio, fim);
        if (result.path("parcial").asBoolean()) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Não foi possível exportar agora: uma das áreas está indisponível. Tente novamente em instantes."
            );
        }
        return csv(
            "movimentos.csv",
            result.path("itens"),
            new String[] {
                "Baixa",
                "Conta",
                "Tipo",
                "Título",
                "Pessoa",
                "Data",
                "Principal",
                "Acréscimo",
                "Desconto",
                "Movimentado",
                "Forma",
                "Conta bancária",
            },
            new String[] {
                "id",
                "contaId",
                "tipo",
                "numeroTitulo",
                "pessoa",
                "data",
                "principal",
                "acrescimo",
                "desconto",
                "valorMovimentado",
                "forma",
                "contaBancaria",
            }
        );
    }

    private ResponseEntity<String> csv(
        String nome,
        JsonNode itens,
        String[] titulos,
        String[] campos
    ) {
        var text = new StringBuilder("\uFEFF");
        linha(text, Arrays.asList(titulos));
        for (var item : itens) {
            var cells = new ArrayList<String>();
            for (var campo : campos) {
                JsonNode value = item;
                for (var part : campo.split("\\.")) {
                    value = value.path(part);
                }
                cells.add(
                    value.isNull() || value.isMissingNode()
                        ? ""
                        : value.isNumber()
                          ? value.decimalValue().toPlainString().replace('.', ',')
                          : value.asText()
                );
            }
            linha(text, cells);
        }
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"" + nome + "\"")
            .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
            .body(text.toString());
    }

    private void linha(StringBuilder text, List<String> values) {
        var escaped = new ArrayList<String>();
        for (var value : values) {
            if (
                !value.stripLeading().isEmpty() &&
                "=+-@".indexOf(value.stripLeading().charAt(0)) >= 0
            ) {
                value = "'" + value;
            }
            escaped.add("\"" + value.replace("\"", "\"\"") + "\"");
        }
        text.append(String.join(";", escaped)).append("\r\n");
    }
}
