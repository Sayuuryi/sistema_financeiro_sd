package br.edu.ifgoiano.financeiro.servico;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifgoiano.financeiro.api.Dtos.*;
import br.edu.ifgoiano.financeiro.api.Dtos.AgenciaRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.AgenciaResponse;
import br.edu.ifgoiano.financeiro.api.Dtos.BancoRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.BancoResponse;
import br.edu.ifgoiano.financeiro.api.Dtos.CategoriaRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.CategoriaResponse;
import br.edu.ifgoiano.financeiro.api.Dtos.ContaBancariaRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.ContaBancariaResponse;
import br.edu.ifgoiano.financeiro.api.Dtos.PessoaRequest;
import br.edu.ifgoiano.financeiro.api.Dtos.PessoaResponse;
import br.edu.ifgoiano.financeiro.dominio.Agencia;
import br.edu.ifgoiano.financeiro.dominio.Banco;
import br.edu.ifgoiano.financeiro.dominio.Categoria;
import br.edu.ifgoiano.financeiro.dominio.ContaBancaria;
import br.edu.ifgoiano.financeiro.dominio.Entidade;
import br.edu.ifgoiano.financeiro.dominio.Pessoa;
import br.edu.ifgoiano.financeiro.dominio.TipoConta;
import br.edu.ifgoiano.financeiro.dominio.TipoPessoa;
import br.edu.ifgoiano.financeiro.erro.ErroNegocio;
import br.edu.ifgoiano.financeiro.repositorio.AgenciaRepository;
import br.edu.ifgoiano.financeiro.repositorio.BaixaRepository;
import br.edu.ifgoiano.financeiro.repositorio.BancoRepository;
import br.edu.ifgoiano.financeiro.repositorio.CategoriaRepository;
import br.edu.ifgoiano.financeiro.repositorio.ContaBancariaRepository;
import br.edu.ifgoiano.financeiro.repositorio.ContaRepository;
import br.edu.ifgoiano.financeiro.repositorio.DespesaRepository;
import br.edu.ifgoiano.financeiro.repositorio.PessoaRepository;

@Service
@Transactional(readOnly = true)
public class CadastrosService {
    private final PessoaRepository pessoas;
    private final CategoriaRepository categorias;
    private final BancoRepository bancos;
    private final AgenciaRepository agencias;
    private final ContaBancariaRepository contasBancarias;
    private final ContaRepository contas;
    private final DespesaRepository despesas;
    private final BaixaRepository baixas;

    public CadastrosService(PessoaRepository pessoas, CategoriaRepository categorias, BancoRepository bancos,
            AgenciaRepository agencias, ContaBancariaRepository contasBancarias, ContaRepository contas,
            DespesaRepository despesas, BaixaRepository baixas) {
        this.pessoas = pessoas;
        this.categorias = categorias;
        this.bancos = bancos;
        this.agencias = agencias;
        this.contasBancarias = contasBancarias;
        this.contas = contas;
        this.despesas = despesas;
        this.baixas = baixas;
    }

    public static String texto(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static String codigo(String value) {
        return value.strip().toUpperCase(Locale.ROOT);
    }

    private static boolean ativo(Boolean value) {
        return value == null || value;
    }

    public static void versao(Entidade entity, Long expected) {
        if (expected == null)
            throw new ErroNegocio(400, "VERSAO_OBRIGATORIA", "Informe a versão atual do registro para editar.");
        if (entity.getVersao() != expected)
            throw ErroNegocio.conflito("O registro foi alterado por outra operação. Atualize a tela.");
    }

    public Pessoa pessoa(Long id) {
        return pessoas.findById(id).orElseThrow(() -> ErroNegocio.ausente("Pessoa"));
    }

    public Categoria categoria(Long id) {
        return categorias.findById(id).orElseThrow(() -> ErroNegocio.ausente("Categoria"));
    }

    public Banco banco(Long id) {
        return bancos.findById(id).orElseThrow(() -> ErroNegocio.ausente("Banco"));
    }

    public Agencia agencia(Long id) {
        return agencias.findById(id).orElseThrow(() -> ErroNegocio.ausente("Agência"));
    }

    public ContaBancaria contaBancaria(Long id) {
        return contasBancarias.findById(id).orElseThrow(() -> ErroNegocio.ausente("Conta bancária"));
    }

    public List<PessoaResponse> listarPessoas(String busca, TipoPessoa tipo, Boolean ativos) {
        var termo = Objects.requireNonNullElse(texto(busca), "").toLowerCase(Locale.ROOT);
        return pessoas.findAll().stream()
                .filter(p -> tipo == null || p.getTipo() == tipo || p.getTipo() == TipoPessoa.AMBOS)
                .filter(p -> ativos == null || p.isAtivo() == ativos)
                .filter(p -> p.getNome().toLowerCase(Locale.ROOT).contains(termo)
                        || Objects.requireNonNullElse(p.getDocumento(), "").contains(termo))
                .sorted(Comparator.comparing(Pessoa::getNome, String.CASE_INSENSITIVE_ORDER)).map(this::resposta)
                .toList();
    }

    public PessoaResponse obterPessoa(Long id) {
        return resposta(pessoa(id));
    }

    @Transactional
    public PessoaResponse salvarPessoa(Long id, PessoaRequest r) {
        Pessoa p = id == null ? new Pessoa() : pessoa(id);
        if (id != null) {
            versao(p, r.versao());
            boolean usaPagar = contas.existsByPessoaIdAndTipo(id, TipoConta.PAGAR) || despesas.existsByPessoaId(id);
            boolean usaReceber = contas.existsByPessoaIdAndTipo(id, TipoConta.RECEBER);
            if ((usaPagar && r.tipo() == TipoPessoa.CLIENTE) || (usaReceber && r.tipo() == TipoPessoa.FORNECEDOR))
                throw ErroNegocio.conflito(
                        "O tipo da pessoa é utilizado por contas ou previsões existentes. Use AMBOS para preservar os vínculos.");
        }
        String documento = texto(r.documento());
        if (documento != null && pessoas.existsByDocumentoAndIdNot(documento, id == null ? -1L : id))
            throw ErroNegocio.conflito("Documento já cadastrado.");
        p.setNome(r.nome().strip());
        p.setTipo(r.tipo());
        p.setDocumento(documento);
        p.setEmail(texto(r.email()) == null ? null : texto(r.email()).toLowerCase(Locale.ROOT));
        p.setTelefone(texto(r.telefone()));
        p.setContato(texto(r.contato()));
        p.setEndereco(texto(r.endereco()));
        p.setCidade(texto(r.cidade()));
        p.setEstado(texto(r.estado()) == null ? null : texto(r.estado()).toUpperCase(Locale.ROOT));
        p.setCep(texto(r.cep()));
        p.setAtivo(ativo(r.ativo()));
        return resposta(pessoas.saveAndFlush(p));
    }

    @Transactional
    public void excluirPessoa(Long id) {
        var p = pessoa(id);
        if (contas.existsByPessoaId(id) || despesas.existsByPessoaId(id))
            throw ErroNegocio
                    .conflito("Pessoa vinculada a contas ou previsões. Inative o cadastro para preservar o histórico.");
        pessoas.delete(p);
        pessoas.flush();
    }

    public List<CategoriaResponse> listarCategorias() {
        return categorias.findAll().stream().sorted(Comparator.comparing(Categoria::getCodigo)).map(this::resposta)
                .toList();
    }

    @Transactional
    public CategoriaResponse salvarCategoria(Long id, CategoriaRequest r) {
        var c = id == null ? new Categoria() : categoria(id);
        if (id != null) {
            versao(c, r.versao());
            if (c.getNatureza() != r.natureza() && (contas.existsByCategoriaId(id) || despesas.existsByCategoriaId(id)))
                throw ErroNegocio.conflito("A natureza da categoria é utilizada por registros existentes.");
        }
        if (categorias.existsByCodigoIgnoreCaseAndIdNot(codigo(r.codigo()), id == null ? -1L : id))
            throw ErroNegocio.conflito("Código de categoria já cadastrado.");
        c.setCodigo(codigo(r.codigo()));
        c.setNome(r.nome().strip());
        c.setNatureza(r.natureza());
        c.setAtivo(ativo(r.ativo()));
        return resposta(categorias.saveAndFlush(c));
    }

    @Transactional
    public void excluirCategoria(Long id) {
        var c = categoria(id);
        if (contas.existsByCategoriaId(id) || despesas.existsByCategoriaId(id))
            throw ErroNegocio.conflito("Categoria vinculada a contas ou previsões. Inative o cadastro.");
        categorias.delete(c);
        categorias.flush();
    }

    public List<BancoResponse> listarBancos() {
        return bancos.findAll().stream().sorted(Comparator.comparing(Banco::getCodigo)).map(this::resposta).toList();
    }

    @Transactional
    public BancoResponse salvarBanco(Long id, BancoRequest r) {
        var b = id == null ? new Banco() : banco(id);
        if (id != null)
            versao(b, r.versao());
        if (bancos.existsByCodigoIgnoreCaseAndIdNot(codigo(r.codigo()), id == null ? -1L : id))
            throw ErroNegocio.conflito("Código de banco já cadastrado.");
        b.setCodigo(codigo(r.codigo()));
        b.setNome(r.nome().strip());
        b.setAtivo(ativo(r.ativo()));
        return resposta(bancos.saveAndFlush(b));
    }

    @Transactional
    public void excluirBanco(Long id) {
        var b = banco(id);
        if (agencias.existsByBancoId(id))
            throw ErroNegocio.conflito("Banco possui agências. Preserve o vínculo ou inative-o.");
        bancos.delete(b);
        bancos.flush();
    }

    public List<AgenciaResponse> listarAgencias() {
        return agencias.findAll().stream().sorted(Comparator.comparing(Agencia::getNumero)).map(this::resposta)
                .toList();
    }

    @Transactional
    public AgenciaResponse salvarAgencia(Long id, AgenciaRequest r) {
        var a = id == null ? new Agencia() : agencia(id);
        var b = banco(r.bancoId());
        if (!b.isAtivo())
            throw ErroNegocio.regra("Selecione um banco ativo.");
        if (id != null) {
            versao(a, r.versao());
            if (!a.getBanco().getId().equals(b.getId()) && contasBancarias.existsByAgenciaId(id))
                throw ErroNegocio.conflito("Não é possível mudar o banco de uma agência com contas vinculadas.");
        }
        if (agencias.existsByBancoIdAndNumeroIgnoreCaseAndIdNot(b.getId(), codigo(r.numero()), id == null ? -1L : id))
            throw ErroNegocio.conflito("Agência já cadastrada neste banco.");
        a.setBanco(b);
        a.setNumero(codigo(r.numero()));
        a.setNome(r.nome().strip());
        a.setAtivo(ativo(r.ativo()));
        return resposta(agencias.saveAndFlush(a));
    }

    @Transactional
    public void excluirAgencia(Long id) {
        var a = agencia(id);
        if (contasBancarias.existsByAgenciaId(id))
            throw ErroNegocio.conflito("Agência possui contas correntes. Inative o cadastro.");
        agencias.delete(a);
        agencias.flush();
    }

    public List<ContaBancariaResponse> listarContasBancarias() {
        return contasBancarias.findAll().stream().sorted(Comparator.comparing(ContaBancaria::getDescricao))
                .map(this::resposta).toList();
    }

    @Transactional
    public ContaBancariaResponse salvarContaBancaria(Long id, ContaBancariaRequest r) {
        var c = id == null ? new ContaBancaria() : contaBancaria(id);
        var a = agencia(r.agenciaId());
        if (!a.isAtivo() || !a.getBanco().isAtivo())
            throw ErroNegocio.regra("Selecione uma agência e um banco ativos.");
        var saldo = Dinheiro.valor(r.saldoInicial());
        if (id != null) {
            versao(c, r.versao());
            if (baixas.existsByContaBancariaId(id) && (!c.getAgencia().getId().equals(a.getId())
                    || !c.getNumero().equals(codigo(r.numero())) || c.getSaldoInicial().compareTo(saldo) != 0))
                throw ErroNegocio
                        .conflito("Conta com movimentações: agência, número e saldo inicial devem ser preservados.");
        }
        if (contasBancarias.existsByAgenciaIdAndNumeroIgnoreCaseAndIdNot(a.getId(), codigo(r.numero()),
                id == null ? -1L : id))
            throw ErroNegocio.conflito("Conta corrente já cadastrada nesta agência.");
        c.setAgencia(a);
        c.setNumero(codigo(r.numero()));
        c.setDescricao(r.descricao().strip());
        c.setSaldoInicial(saldo);
        c.setAtivo(ativo(r.ativo()));
        return resposta(contasBancarias.saveAndFlush(c));
    }

    @Transactional
    public void excluirContaBancaria(Long id) {
        var c = contaBancaria(id);
        if (baixas.existsByContaBancariaId(id))
            throw ErroNegocio.conflito("Conta bancária possui histórico de movimentações. Inative o cadastro.");
        contasBancarias.delete(c);
        contasBancarias.flush();
    }

    public PessoaResponse resposta(Pessoa p) {
        return new PessoaResponse(p.getId(), p.getVersao(), p.getNome(), p.getTipo(), p.getDocumento(), p.getEmail(),
                p.getTelefone(), p.getContato(), p.getEndereco(), p.getCidade(), p.getEstado(), p.getCep(),
                p.isAtivo());
    }

    public CategoriaResponse resposta(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getVersao(), c.getCodigo(), c.getNome(), c.getNatureza(),
                c.isAtivo());
    }

    public BancoResponse resposta(Banco b) {
        return new BancoResponse(b.getId(), b.getVersao(), b.getCodigo(), b.getNome(), b.isAtivo());
    }

    public AgenciaResponse resposta(Agencia a) {
        return new AgenciaResponse(a.getId(), a.getVersao(), resposta(a.getBanco()), a.getNumero(), a.getNome(),
                a.isAtivo());
    }

    public ContaBancariaResponse resposta(ContaBancaria c) {
        BigDecimal saldo = c.getSaldoInicial();
        for (var b : baixas.findByContaBancariaIdAndEstornadaFalse(c.getId()))
            saldo = b.getConta().getTipo() == TipoConta.RECEBER ? saldo.add(b.valorMovimentado())
                    : saldo.subtract(b.valorMovimentado());
        return new ContaBancariaResponse(c.getId(), c.getVersao(), resposta(c.getAgencia()), c.getNumero(),
                c.getDescricao(), c.getSaldoInicial(), saldo, c.isAtivo());
    }
}
