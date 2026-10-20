package br.edu.ifgoiano.financeiro;

import br.edu.ifgoiano.financeiro.api.Dtos.*;
import br.edu.ifgoiano.financeiro.dominio.*;
import br.edu.ifgoiano.financeiro.erro.ErroNegocio;
import br.edu.ifgoiano.financeiro.servico.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.*;
import org.springframework.context.annotation.*;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = { "spring.datasource.url=jdbc:h2:mem:financeiro-test;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
        "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa",
        "spring.datasource.password=", "app.demo.enabled=false" })
@AutoConfigureMockMvc
@Import(FinanceiroIntegracaoTest.RelogioTeste.class)
class FinanceiroIntegracaoTest {
    @TestConfiguration
    static class RelogioTeste {
        @Bean
        @Primary
        Clock relogioFixo() {
            return Clock.fixed(Instant.parse("2026-10-15T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        }
    }

    @Autowired
    CadastrosService cadastros;
    @Autowired
    ContasService contas;
    @Autowired
    DespesasService despesas;
    @Autowired
    RelatoriosService relatorios;
    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper json;
    PessoaResponse fornecedor, cliente;
    CategoriaResponse despesa, receita;
    final LocalDate hoje = LocalDate.of(2026, 10, 15);

    static BigDecimal d(String s) {
        return new BigDecimal(s);
    }

    @BeforeEach
    void base() {
        String tag = UUID.randomUUID().toString();
        fornecedor = pessoa("Fornecedor " + tag, TipoPessoa.FORNECEDOR);
        cliente = pessoa("Cliente " + tag, TipoPessoa.CLIENTE);
        despesa = cadastros.salvarCategoria(null,
                new CategoriaRequest(tag.substring(0, 12), "Materiais", NaturezaCategoria.DESPESA, true, null));
        receita = cadastros.salvarCategoria(null,
                new CategoriaRequest(tag.substring(12, 24), "Serviços", NaturezaCategoria.RECEITA, true, null));
    }

    PessoaResponse pessoa(String nome, TipoPessoa tipo) {
        return cadastros.salvarPessoa(null,
                new PessoaRequest(nome, tipo, null, null, null, null, null, null, null, null, true, null));
    }

    ContaRequest request(TipoConta tipo, String numero, String valor, LocalDate vence) {
        return new ContaRequest(tipo, tipo == TipoConta.PAGAR ? fornecedor.id() : cliente.id(),
                tipo == TipoConta.PAGAR ? despesa.id() : receita.id(), numero, TipoTitulo.DUPLICATA, "Título de teste",
                hoje.minusDays(30), vence, d(valor), d("10"), hoje.plusDays(10), null, null);
    }

    ContaResponse titulo(String valor) {
        return contas.criar(request(TipoConta.PAGAR, UUID.randomUUID().toString(), valor, hoje.plusDays(5)));
    }

    BaixaRequest baixa(String principal, String acrescimo, String desconto) {
        return new BaixaRequest(hoje, d(principal), d(acrescimo), d(desconto), FormaPagamento.DINHEIRO, null, null);
    }

    ResultadoBaixa baixar(Long id, BaixaRequest b) {
        return contas.baixar(id, UUID.randomUUID().toString(), b);
    }

    void erro(int status, Runnable action) {
        assertThatThrownBy(action::run).isInstanceOfSatisfying(ErroNegocio.class,
                e -> assertThat(e.getStatus()).isEqualTo(status));
    }

    FiltroContas filtro(SituacaoConta s) {
        return new FiltroContas(TipoConta.PAGAR, fornecedor.id(), null, s, null, null, null, null, null);
    }

    ContaBancariaResponse bancaria() {
        var b = cadastros.salvarBanco(null,
                new BancoRequest(UUID.randomUUID().toString().substring(0, 8), "Banco teste", true, null));
        var a = cadastros.salvarAgencia(null, new AgenciaRequest(b.id(), "001", "Agência teste", true, null));
        return cadastros.salvarContaBancaria(null,
                new ContaBancariaRequest(a.id(), "123-4", "Conta teste", d("1000"), true, null));
    }

    @Test
    void baixaParcialTotalEEstornoPreservamHistorico() {
        var c = titulo("100.00");
        var b = baixar(c.id(), baixa("40", "2", "5")).baixa();
        var parcial = contas.obter(c.id()).conta();
        assertThat(parcial.saldo()).isEqualByComparingTo("60");
        assertThat(parcial.situacao()).isEqualTo(SituacaoConta.PARCIAL);
        assertThat(b.valorMovimentado()).isEqualByComparingTo("37");
        baixar(c.id(), baixa("60", "0", "0"));
        assertThat(contas.obter(c.id()).conta().situacao()).isEqualTo(SituacaoConta.PAGA);
        contas.estornar(c.id(), b.id(), new MotivoRequest("Pagamento registrado incorretamente"));
        var detalhe = contas.obter(c.id());
        assertThat(detalhe.conta().saldo()).isEqualByComparingTo("40");
        assertThat(detalhe.historico()).hasSize(2);
        assertThat(detalhe.historico().getFirst().estornada()).isTrue();
        erro(409, () -> contas.excluir(c.id()));
    }

    @Test
    void receberUsaSituacaoRecebida() {
        var c = contas.criar(request(TipoConta.RECEBER, "REC-" + UUID.randomUUID(), "100", hoje));
        baixar(c.id(), baixa("100", "0", "0"));
        assertThat(contas.obter(c.id()).conta().situacao()).isEqualTo(SituacaoConta.RECEBIDA);
    }

    @Test
    void idempotenciaRetornaMesmaBaixaSemDuplicar() {
        var c = titulo("100");
        String key = UUID.randomUUID().toString();
        var b = baixa("30", "0", "0");
        var a = contas.baixar(c.id(), key, b);
        var rep = contas.baixar(c.id(), key, b);
        assertThat(rep.repeticao()).isTrue();
        assertThat(rep.baixa().id()).isEqualTo(a.baixa().id());
        assertThat(contas.obter(c.id()).historico()).hasSize(1);
        erro(409, () -> contas.baixar(c.id(), key, baixa("31", "0", "0")));
    }

    @Test
    void chaveDeIdempotenciaExigeUuidCompleto() {
        var c = titulo("100");
        erro(400, () -> contas.baixar(c.id(), "1-1-1-1-1", baixa("10", "0", "0")));
    }

    @Test
    void principalNaoPodeExcederSaldo() {
        var c = titulo("100");
        baixar(c.id(), baixa("70", "0", "0"));
        erro(409, () -> baixar(c.id(), baixa("31", "0", "0")));
        assertThat(contas.obter(c.id()).conta().saldo()).isEqualByComparingTo("30");
    }

    @Test
    void concorrenciaNaoPermiteBaixaExcedente() throws Exception {
        var c = titulo("100");
        var inicio = new CountDownLatch(1);
        var prontos = new CountDownLatch(2);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<Integer> tarefa = () -> {
                prontos.countDown();
                inicio.await();
                try {
                    baixar(c.id(), baixa("70", "0", "0"));
                    return 201;
                } catch (ErroNegocio e) {
                    return e.getStatus();
                }
            };
            var a = executor.submit(tarefa);
            var b = executor.submit(tarefa);
            assertThat(prontos.await(5, TimeUnit.SECONDS)).isTrue();
            inicio.countDown();
            assertThat(List.of(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS))).containsExactlyInAnyOrder(201,
                    409);
        }
        assertThat(contas.obter(c.id()).conta().saldo()).isEqualByComparingTo("30");
        assertThat(contas.obter(c.id()).historico()).hasSize(1);
    }

    @Test
    void descontoAcumuladoELimiteDeValidade() {
        var c = titulo("100");
        baixar(c.id(), baixa("30", "0", "6"));
        erro(422, () -> baixar(c.id(), baixa("30", "0", "5")));
        var b = contas.obter(c.id()).historico().getFirst();
        contas.estornar(c.id(), b.id(), new MotivoRequest("Desfazer para corrigir"));
        baixar(c.id(), baixa("30", "0", "10"));
    }

    @Test
    void descontoVencidoEDataFuturaSaoRecusados() {
        var r = request(TipoConta.PAGAR, "D-" + UUID.randomUUID(), "100", hoje);
        var c = contas.criar(new ContaRequest(r.tipo(), r.pessoaId(), r.categoriaId(), r.numeroTitulo(), r.tipoTitulo(),
                r.descricao(), r.emissao(), r.vencimento(), r.valor(), d("10"), hoje.minusDays(1), null, null));
        erro(422, () -> baixar(c.id(), baixa("50", "0", "1")));
        erro(422, () -> baixar(c.id(),
                new BaixaRequest(hoje.plusDays(1), d("10"), null, null, FormaPagamento.DINHEIRO, null, null)));
    }

    @Test
    void validacaoDeVinculosClienteFornecedorECategoria() {
        var r = request(TipoConta.PAGAR, "V-" + UUID.randomUUID(), "100", hoje);
        erro(422, () -> contas.criar(new ContaRequest(r.tipo(), cliente.id(), r.categoriaId(), r.numeroTitulo(),
                r.tipoTitulo(), r.descricao(), r.emissao(), r.vencimento(), r.valor(), null, null, null, null)));
        erro(422, () -> contas.criar(new ContaRequest(r.tipo(), fornecedor.id(), receita.id(), r.numeroTitulo(),
                r.tipoTitulo(), r.descricao(), r.emissao(), r.vencimento(), r.valor(), null, null, null, null)));
    }

    @Test
    void pessoaAmbosAtendeDuasOperacoes() {
        var p = pessoa("Ambos teste", TipoPessoa.AMBOS);
        for (var tipo : TipoConta.values()) {
            var r = request(tipo, "A-" + UUID.randomUUID(), "100", hoje);
            assertThat(contas.criar(new ContaRequest(tipo, p.id(), r.categoriaId(), r.numeroTitulo(), r.tipoTitulo(),
                    r.descricao(), r.emissao(), r.vencimento(), r.valor(), null, null, null, null)).id()).isPositive();
        }
    }

    @Test
    void tituloDuplicadoERecusado() {
        var r = request(TipoConta.PAGAR, "UNICO", "100", hoje);
        contas.criar(r);
        erro(409, () -> contas.criar(r));
    }

    @Test
    void cancelamentoExigeEstornoESaldoCanceladoEhZero() {
        var c = titulo("100");
        var b = baixar(c.id(), baixa("20", "0", "0")).baixa();
        erro(409, () -> contas.cancelar(c.id(), new MotivoRequest("Desistência da operação")));
        contas.estornar(c.id(), b.id(), new MotivoRequest("Correção da operação"));
        var cancelada = contas.cancelar(c.id(), new MotivoRequest("Desistência da operação"));
        assertThat(cancelada.saldo()).isEqualByComparingTo("0");
        assertThat(cancelada.situacao()).isEqualTo(SituacaoConta.CANCELADA);
        erro(409, () -> baixar(c.id(), baixa("1", "0", "0")));
    }

    @Test
    void previsaoPreservaEstimativaEAceitaValorRealDiferente() {
        var d = despesas.salvar(null, new DespesaRequest(fornecedor.id(), despesa.id(), "Energia estimada", null, hoje,
                d("300"), null, true, null));
        var r = new GerarContaRequest("P-" + UUID.randomUUID(), TipoTitulo.DUPLICATA, hoje, hoje.plusDays(5), d("285"),
                null, null, null);
        var c = despesas.gerarConta(d.id(), r);
        assertThat(c.valor()).isEqualByComparingTo("285");
        assertThat(despesas.obter(d.id()).valorPrevisto()).isEqualByComparingTo("300");
        assertThat(despesas.obter(d.id()).contaGeradaId()).isEqualTo(c.id());
        erro(409, () -> despesas.gerarConta(d.id(), r));
        erro(409, () -> despesas.excluir(d.id()));
        erro(409, () -> contas.excluir(c.id()));
    }

    @Test
    void bancoAgenciaContaExigemVinculosEProtegemHistorico() {
        var b = bancaria();
        erro(409, () -> cadastros.excluirBanco(b.agencia().banco().id()));
        erro(409, () -> cadastros.excluirAgencia(b.agencia().id()));
        var c = titulo("100");
        baixar(c.id(), new BaixaRequest(hoje, d("100"), null, null, FormaPagamento.PIX, b.id(), null));
        erro(409, () -> cadastros.excluirContaBancaria(b.id()));
        assertThat(cadastros.listarContasBancarias().stream().filter(x -> x.id().equals(b.id())).findFirst()
                .orElseThrow().saldoAtual()).isEqualByComparingTo("900");
    }

    @Test
    void pixSemContaEhRecusado() {
        var c = titulo("100");
        erro(422, () -> baixar(c.id(), new BaixaRequest(hoje, d("10"), null, null, FormaPagamento.PIX, null, null)));
    }

    @Test
    void versaoObsoletaNaoSobrescreveCadastro() {
        var r = new CategoriaRequest(despesa.codigo(), "Novo nome", NaturezaCategoria.DESPESA, true, despesa.versao());
        var updated = cadastros.salvarCategoria(despesa.id(), r);
        assertThat(updated.versao()).isGreaterThan(despesa.versao());
        erro(409, () -> cadastros.salvarCategoria(despesa.id(), r));
    }

    @Test
    void vencimentoPassadoPrevaleceSobreBaixaParcial() {
        var c = contas.criar(request(TipoConta.PAGAR, "VENC-" + UUID.randomUUID(), "100", hoje.minusDays(1)));
        baixar(c.id(), baixa("10", "0", "0"));
        assertThat(contas.listar(filtro(SituacaoConta.VENCIDA), 0, 20).itens()).extracting(ContaResponse::id)
                .contains(c.id());
        assertThat(contas.listar(filtro(SituacaoConta.PARCIAL), 0, 20).itens()).isEmpty();
    }

    @Test
    void saldoNaoUsaPontoFlutuante() {
        var c = titulo("10.30");
        baixar(c.id(), baixa("0.10", "0", "0"));
        baixar(c.id(), baixa("0.20", "0", "0"));
        assertThat(contas.obter(c.id()).conta().saldo()).isEqualByComparingTo("10.00");
    }

    @Test
    void versaoETituloComHistoricoImpedemEdicao() {
        var c = titulo("100");
        baixar(c.id(), baixa("10", "0", "0"));
        var r = request(TipoConta.PAGAR, c.numeroTitulo(), "120", hoje);
        erro(409,
                () -> contas.editar(c.id(),
                        new ContaRequest(r.tipo(), r.pessoaId(), r.categoriaId(), r.numeroTitulo(), r.tipoTitulo(),
                                r.descricao(), r.emissao(), r.vencimento(), r.valor(), null, null, null, c.versao())));
    }

    @Test
    void csvNeutralizaFormulaEmDescricao() {
        var r = request(TipoConta.PAGAR, "CSV-" + UUID.randomUUID(), "100", hoje);
        contas.criar(new ContaRequest(r.tipo(), r.pessoaId(), r.categoriaId(), r.numeroTitulo(), r.tipoTitulo(),
                "=SUM(A1:A5)", r.emissao(), r.vencimento(), r.valor(), null, null, null, null));
        assertThat(relatorios.contasCsv(filtro(null))).contains("\"'=SUM(A1:A5)\"");
    }

    @Test
    void apiCrudFiltrosErrosEDocumentacao() throws Exception {
        var c = titulo("100");
        mvc.perform(get("/api/contas").param("pessoaId", fornecedor.id().toString()).param("situacao", "ABERTA"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(1));
        mvc.perform(get("/api/contas/{id}", c.id())).andExpect(status().isOk())
                .andExpect(jsonPath("$.conta.saldo").value(100));
        mvc.perform(get("/api/contas/99999999")).andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.codigo").exists());
        mvc.perform(
                post("/api/pessoas").contentType("application/json").content("{\"nome\":\"\",\"tipo\":\"CLIENTE\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.violacoes.nome").exists());
        mvc.perform(get("/api/contas").param("vencimentoDe", "2026-12-10").param("vencimentoAte", "2026-01-01"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/contas/{id}/baixas']").exists());
    }

    @Test
    void apiRecusaFormatoDesconhecidoEDecimaisExcessivos() throws Exception {
        mvc.perform(post("/api/pessoas").contentType("application/json")
                .content("{\"nome\":\"Teste\",\"tipo\":\"CLIENTE\",\"campoInventado\":1}"))
                .andExpect(status().isBadRequest());
        var r = request(TipoConta.PAGAR, "INVALID-" + UUID.randomUUID(), "100.001", hoje);
        mvc.perform(post("/api/contas").contentType("application/json").content(json.writeValueAsString(r)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void apiBaixaExigeChaveERepeticaoRetorna200() throws Exception {
        var c = titulo("100");
        String body = json.writeValueAsString(baixa("20", "0", "0")), key = UUID.randomUUID().toString();
        mvc.perform(post("/api/contas/{id}/baixas", c.id()).contentType("application/json").content(body))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/contas/{id}/baixas", c.id()).header("Idempotency-Key", key)
                .contentType("application/json").content(body)).andExpect(status().isCreated());
        mvc.perform(post("/api/contas/{id}/baixas", c.id()).header("Idempotency-Key", key)
                .contentType("application/json").content(body)).andExpect(status().isOk());
    }

    @Test
    void resumoUsaDataDaBaixaEExcluiEstornos() {
        var emissao = LocalDate.of(2000, 1, 1);
        var c = contas.criar(new ContaRequest(TipoConta.RECEBER, cliente.id(), receita.id(), "RES-" + UUID.randomUUID(),
                TipoTitulo.OUTROS, "Resumo isolado", emissao, LocalDate.of(2000, 2, 15), d("100"), d("5"),
                LocalDate.of(2000, 3, 1), null, null));
        var b = baixar(c.id(), new BaixaRequest(LocalDate.of(2000, 3, 1), d("50"), d("2"), d("5"),
                FormaPagamento.DINHEIRO, null, null)).baixa();
        var r = relatorios.resumo(LocalDate.of(2000, 3, 1), LocalDate.of(2000, 3, 31));
        assertThat(r.recebido()).isEqualByComparingTo("47");
        assertThat(r.aReceber()).isEqualByComparingTo("0");
        contas.estornar(c.id(), b.id(), new MotivoRequest("Correção de lançamento"));
        assertThat(relatorios.resumo(r.inicio(), r.fim()).recebido()).isEqualByComparingTo("0");
    }

    @Test
    void exclusaoSemVinculoFuncionaEInativoImpedeNovoTitulo() {
        var p = pessoa("Pessoa removível", TipoPessoa.CLIENTE);
        cadastros.excluirPessoa(p.id());
        erro(404, () -> cadastros.obterPessoa(p.id()));
        cadastros.salvarCategoria(despesa.id(),
                new CategoriaRequest(despesa.codigo(), despesa.nome(), despesa.natureza(), false, despesa.versao()));
        erro(422, () -> titulo("100"));
    }

    @Test
    void contratoOpenApiDocumentaFiltrosECodigosReais() throws Exception {
        var resposta = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString();
        java.nio.file.Files.writeString(java.nio.file.Path.of("target/openapi.json"), resposta,
                java.nio.charset.StandardCharsets.UTF_8);
        var paths = json.readTree(resposta).path("paths");
        var nomes = new ArrayList<String>();
        paths.path("/api/contas").path("get").path("parameters").forEach(p -> nomes.add(p.path("name").asText()));
        assertThat(nomes).contains("tipo", "situacao", "vencimentoDe", "emissaoAte", "pessoaId", "categoriaId", "busca",
                "pagina", "tamanho").doesNotContain("filtro");
        assertThat(paths.path("/api/contas").path("post").path("responses").has("201")).isTrue();
        assertThat(paths.path("/api/contas/{id}").path("delete").path("responses").has("204")).isTrue();
        var baixaResponses = paths.path("/api/contas/{id}/baixas").path("post").path("responses");
        assertThat(baixaResponses.has("201") && baixaResponses.has("200")).isTrue();
        assertThat(baixaResponses.has("400") && baixaResponses.has("409") && baixaResponses.has("422")).isTrue();
        for (String action : List.of("/api/contas/{id}/cancelamento", "/api/contas/{id}/baixas/{baixaId}/estorno")) {
            var respostas = paths.path(action).path("post").path("responses");
            assertThat(respostas.has("200")).isTrue();
            assertThat(respostas.has("201")).isFalse();
        }
    }
}
