package br.edu.ifgoiano.financeiro.config;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifgoiano.financeiro.api.Dtos.*;
import br.edu.ifgoiano.financeiro.api.Dtos.AgenciaRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.BaixaRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.BancoRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.CategoriaRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.CategoriaResponse;
import br.edu.ifgoiano.financeiro.api.Dtos.ContaBancariaRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.ContaRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.ContaResponse;
import br.edu.ifgoiano.financeiro.api.Dtos.DespesaRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.GerarContaRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.MotivoRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.PessoaRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.PessoaResponse;
import br.edu.ifgoiano.financeiro.dominio.FormaPagamento;
import br.edu.ifgoiano.financeiro.dominio.NaturezaCategoria;
import br.edu.ifgoiano.financeiro.dominio.TipoConta;
import br.edu.ifgoiano.financeiro.dominio.TipoPessoa;
import br.edu.ifgoiano.financeiro.dominio.TipoTitulo;
import br.edu.ifgoiano.financeiro.repositorio.BancoRepository;
import br.edu.ifgoiano.financeiro.repositorio.CategoriaRepository;
import br.edu.ifgoiano.financeiro.repositorio.ContaRepository;
import br.edu.ifgoiano.financeiro.repositorio.PessoaRepository;
import br.edu.ifgoiano.financeiro.servico.CadastrosService;
import br.edu.ifgoiano.financeiro.servico.ContasService;
import br.edu.ifgoiano.financeiro.servico.DespesasService;

@Component
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DadosDemonstrativos implements ApplicationRunner {
    private final CadastrosService cadastros;
    private final ContasService contas;
    private final DespesasService despesas;
    private final PessoaRepository pessoas;
    private final CategoriaRepository categorias;
    private final BancoRepository bancos;
    private final ContaRepository titulos;

    public DadosDemonstrativos(CadastrosService cadastros, ContasService contas, DespesasService despesas,
            PessoaRepository pessoas, CategoriaRepository categorias, BancoRepository bancos, ContaRepository titulos) {
        this.cadastros = cadastros;
        this.contas = contas;
        this.despesas = despesas;
        this.pessoas = pessoas;
        this.categorias = categorias;
        this.bancos = bancos;
        this.titulos = titulos;
    }

    private BigDecimal d(String v) {
        return new BigDecimal(v);
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (pessoas.count() > 0 || categorias.count() > 0 || bancos.count() > 0 || titulos.count() > 0)
            return;
        var fornecedor = pessoa("Horizonte Suprimentos (fictício)", TipoPessoa.FORNECEDOR);
        var energia = pessoa("Luz do Cerrado (fictício)", TipoPessoa.FORNECEDOR);
        var locador = pessoa("Espaço Central (fictício)", TipoPessoa.FORNECEDOR);
        var cliente = pessoa("Estúdio Ipê (fictício)", TipoPessoa.CLIENTE);
        var cliente2 = pessoa("Ateliê Aurora (fictício)", TipoPessoa.CLIENTE);
        pessoa("Papelaria Ponte (fictício)", TipoPessoa.AMBOS);
        var material = categoria("2.01", "Materiais e suprimentos", NaturezaCategoria.DESPESA);
        var utilidades = categoria("2.02", "Energia e utilidades", NaturezaCategoria.DESPESA);
        var aluguel = categoria("2.03", "Aluguel e estrutura", NaturezaCategoria.DESPESA);
        var servicos = categoria("1.01", "Prestação de serviços", NaturezaCategoria.RECEITA);
        var vendas = categoria("1.02", "Venda de produtos", NaturezaCategoria.RECEITA);
        var banco = cadastros.salvarBanco(null, new BancoRequest("999", "Banco Demonstração (fictício)", true, null));
        var agencia = cadastros.salvarAgencia(null,
                new AgenciaRequest(banco.id(), "0001", "Agência de demonstração", true, null));
        var bancaria = cadastros.salvarContaBancaria(null,
                new ContaBancariaRequest(agencia.id(), "12345-6", "Conta operacional", d("15000"), true, null));
        var hoje = contas.hoje();
        titulo(TipoConta.PAGAR, energia, utilidades, "P-001", "Energia elétrica", -3, "485.70");
        titulo(TipoConta.PAGAR, locador, aluguel, "P-002", "Aluguel do escritório", 5, "2400");
        var parcial = titulo(TipoConta.PAGAR, fornecedor, material, "P-003", "Compra de suprimentos", 10, "1250");
        contas.baixar(parcial.id(), UUID.randomUUID().toString(), new BaixaRequest(hoje, d("400"), d("0"), d("0"),
                FormaPagamento.PIX, bancaria.id(), "Primeira parcela demonstrativa"));
        var paga = titulo(TipoConta.PAGAR, fornecedor, material, "P-004", "Materiais de expediente", -5, "380");
        contas.baixar(paga.id(), UUID.randomUUID().toString(), new BaixaRequest(hoje.minusDays(5), d("380"), d("0"),
                d("0"), FormaPagamento.TRANSFERENCIA, bancaria.id(), null));
        titulo(TipoConta.RECEBER, cliente, servicos, "R-001", "Projeto de identidade visual", -2, "3200");
        titulo(TipoConta.RECEBER, cliente2, vendas, "R-002", "Fornecimento de produtos", 7, "1840");
        var recebida = titulo(TipoConta.RECEBER, cliente, servicos, "R-003", "Consultoria concluída", -4, "2100");
        contas.baixar(recebida.id(), UUID.randomUUID().toString(), new BaixaRequest(hoje.minusDays(4), d("2100"),
                d("0"), d("0"), FormaPagamento.PIX, bancaria.id(), null));
        var cancelada = titulo(TipoConta.RECEBER, cliente2, servicos, "R-004", "Serviço descontinuado", 12, "600");
        contas.cancelar(cancelada.id(), new MotivoRequest("Pedido cancelado pelo cliente — exemplo fictício"));
        despesas.salvar(null,
                new DespesaRequest(energia.id(), utilidades.id(), "Estimativa da próxima fatura de energia",
                        "Planejamento mensal", hoje.plusDays(25), d("520"),
                        "Valor estimado; converter após receber a fatura.", true, null));
        var previsao = despesas.salvar(null,
                new DespesaRequest(fornecedor.id(), material.id(), "Reposição de materiais", "Cotação inicial",
                        hoje.plusDays(15), d("900"), "Estimativa preservada após conversão.", true, null));
        despesas.gerarConta(previsao.id(), new GerarContaRequest("P-005", TipoTitulo.DUPLICATA, hoje, hoje.plusDays(15),
                d("870"), d("20"), hoje.plusDays(10), "Valor final negociado: R$ 870,00."));
    }

    private PessoaResponse pessoa(String nome, TipoPessoa tipo) {
        return cadastros.salvarPessoa(null,
                new PessoaRequest(nome, tipo, null, null, null, null, null, "Goiânia", "GO", null, true, null));
    }

    private CategoriaResponse categoria(String codigo, String nome, NaturezaCategoria natureza) {
        return cadastros.salvarCategoria(null, new CategoriaRequest(codigo, nome, natureza, true, null));
    }

    private ContaResponse titulo(TipoConta tipo, PessoaResponse pessoa, CategoriaResponse categoria, String numero,
            String descricao, int dias, String valor) {
        var hoje = contas.hoje();
        return contas.criar(new ContaRequest(tipo, pessoa.id(), categoria.id(), numero, TipoTitulo.DUPLICATA, descricao,
                hoje.minusDays(30), hoje.plusDays(dias), d(valor), d("0"), null, "Dado fictício para demonstração",
                null));
    }
}
