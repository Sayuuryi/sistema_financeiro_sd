package br.edu.fluxo.cadastros.api;

import br.edu.fluxo.cadastros.api.Dtos.*;
import br.edu.fluxo.cadastros.servico.CadastrosService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/interno")
public class ReferenciasController {

    private final CadastrosService service;

    public ReferenciasController(CadastrosService service) {
        this.service = service;
    }

    @GetMapping("/pessoas/{id}")
    public PessoaResponse pessoa(@PathVariable Long id) {
        return service.obterPessoa(id);
    }

    @GetMapping("/categorias/{id}")
    public CategoriaResponse categoria(@PathVariable Long id) {
        return service.resposta(service.categoria(id));
    }

    @GetMapping("/contas-bancarias/{id}")
    public ContaBancariaResponse bancaria(@PathVariable Long id) {
        return service.respostaInterna(id);
    }
}
