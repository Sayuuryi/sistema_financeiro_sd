package br.edu.fluxo.pagar.cliente;

import br.edu.fluxo.pagar.api.Dtos.*;
import br.edu.fluxo.pagar.exceptions.RespostaRemotaInvalidaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CadastrosClient {

    private final ClienteHttp http;

    public CadastrosClient(
        @Value("${servicos.cadastros.url}") String url,
        @Value("${integracao.timeout-ms}") int timeout
    ) {
        http = new ClienteHttp(url, "cadastros", timeout);
    }

    public PessoaResponse pessoa(Long id) {
        var p = http.get("/interno/pessoas/" + id, PessoaResponse.class);
        if (p.id() == null || p.nome() == null || p.tipo() == null) {
            throw new RespostaRemotaInvalidaException("cadastros");
        }
        return p;
    }

    public CategoriaResponse categoria(Long id) {
        var c = http.get("/interno/categorias/" + id, CategoriaResponse.class);
        if (c.id() == null || c.nome() == null || c.natureza() == null || c.codigo() == null) {
            throw new RespostaRemotaInvalidaException("cadastros");
        }
        return c;
    }

    public ContaBancariaResponse contaBancaria(Long id) {
        var b = http.get("/interno/contas-bancarias/" + id, ContaBancariaResponse.class);
        if (
            b.id() == null ||
            b.agencia() == null ||
            b.agencia().banco() == null ||
            b.descricao() == null ||
            b.numero() == null
        ) {
            throw new RespostaRemotaInvalidaException("cadastros");
        }
        return b;
    }
}
