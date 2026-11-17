package br.edu.fluxo.planejamento.cliente;

import br.edu.fluxo.planejamento.api.Dtos.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cadastros-service", url = "${servicos.cadastros.url:}")
public interface CadastrosApi {
    @GetMapping("/interno/pessoas/{id}")
    PessoaResponse pessoa(@PathVariable("id") Long id);

    @GetMapping("/interno/categorias/{id}")
    CategoriaResponse categoria(@PathVariable("id") Long id);
}
