package br.edu.fluxo.planejamento.cliente;

import br.edu.fluxo.planejamento.api.Dtos.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "contas-pagar-service", url = "${servicos.contas.url:}")
public interface ContasApi {
    @PostMapping("/interno/previsoes/{origem}/conta")
    ContaResponse converter(@PathVariable("origem") Long origem, @RequestBody ContaRequest request);
}
