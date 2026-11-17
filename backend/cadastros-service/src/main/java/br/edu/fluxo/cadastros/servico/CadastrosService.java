package br.edu.fluxo.cadastros.servico;

import br.edu.fluxo.cadastros.api.Dtos.*;
import br.edu.fluxo.cadastros.dominio.*;
import br.edu.fluxo.cadastros.exceptions.ErroNegocio;
import br.edu.fluxo.cadastros.repositorio.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CadastrosService {

    private final PessoaRepository pessoas;
    private final CategoriaRepository categorias;
    private final BancoRepository bancos;
    private final AgenciaRepository agencias;
    private final ContaBancariaRepository contasBancarias;

    public CadastrosService(
        PessoaRepository pessoas,
        CategoriaRepository categorias,
        BancoRepository bancos,
        AgenciaRepository agencias,
        ContaBancariaRepository contasBancarias
    ) {
        this.pessoas = pessoas;
        this.categorias = categorias;
        this.bancos = bancos;
        this.agencias = agencias;
        this.contasBancarias = contasBancarias;
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
        if (expected == null) {
            throw new ErroNegocio(
                400,
                "VERSAO_OBRIGATORIA",
                "Informe a versão atual do registro para editar."
            );
        }
        if (entity.getVersao() != expected) {
            throw ErroNegocio.conflito(
                "O registro foi alterado por outra operação. Atualize a tela."
            );
        }
    }

    public Pessoa pessoa(Long id) {
        return pessoas
            .findById(id)
            .filter(p -> !p.isExcluido())
            .orElseThrow(() -> ErroNegocio.ausente("Pessoa"));
    }

    public Categoria categoria(Long id) {
        return categorias
            .findById(id)
            .filter(c -> !c.isExcluido())
            .orElseThrow(() -> ErroNegocio.ausente("Categoria"));
    }

    public Banco banco(Long id) {
        return bancos.findById(id).orElseThrow(() -> ErroNegocio.ausente("Banco"));
    }

    public Agencia agencia(Long id) {
        return agencias.findById(id).orElseThrow(() -> ErroNegocio.ausente("Agência"));
    }

    public ContaBancaria contaBancaria(Long id) {
        return contasBancarias
            .findById(id)
            .filter(c -> !c.isExcluido())
            .orElseThrow(() -> ErroNegocio.ausente("Conta bancária"));
    }

    public List<PessoaResponse> listarPessoas(String busca, TipoPessoa tipo, Boolean ativos) {
        var termo = Objects.requireNonNullElse(texto(busca), "").toLowerCase(Locale.ROOT);
        return pessoas
            .findAll()
            .stream()
            .filter(p -> !p.isExcluido())
            .filter(p -> tipo == null || p.getTipo() == tipo || p.getTipo() == TipoPessoa.AMBOS)
            .filter(p -> ativos == null || p.isAtivo() == ativos)
            .filter(
                p ->
                    p.getNome().toLowerCase(Locale.ROOT).contains(termo) ||
                    Objects.requireNonNullElse(p.getDocumento(), "").contains(termo)
            )
            .sorted(Comparator.comparing(Pessoa::getNome, String.CASE_INSENSITIVE_ORDER))
            .map(this::resposta)
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
            if (p.getTipo() != r.tipo()) {
                throw ErroNegocio.conflito(
                    "O papel da pessoa é estável. Cadastre outra pessoa para uma classificação diferente."
                );
            }
        }
        String documento = texto(r.documento());
        if (
            documento != null && pessoas.existsByDocumentoAndIdNot(documento, id == null ? -1L : id)
        ) {
            throw ErroNegocio.conflito("Documento já cadastrado.");
        }
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
        p.setExcluido(true);
        p.setAtivo(false);
        pessoas.saveAndFlush(p);
    }

    public List<CategoriaResponse> listarCategorias() {
        return categorias
            .findAll()
            .stream()
            .filter(c -> !c.isExcluido())
            .sorted(Comparator.comparing(Categoria::getCodigo))
            .map(this::resposta)
            .toList();
    }

    @Transactional
    public CategoriaResponse salvarCategoria(Long id, CategoriaRequest r) {
        var c = id == null ? new Categoria() : categoria(id);
        if (id != null) {
            versao(c, r.versao());
            if (c.getNatureza() != r.natureza()) {
                throw ErroNegocio.conflito(
                    "A natureza da categoria é estável. Cadastre outra categoria para uma natureza diferente."
                );
            }
        }
        if (
            categorias.existsByCodigoIgnoreCaseAndIdNot(codigo(r.codigo()), id == null ? -1L : id)
        ) {
            throw ErroNegocio.conflito("Código de categoria já cadastrado.");
        }
        c.setCodigo(codigo(r.codigo()));
        c.setNome(r.nome().strip());
        c.setNatureza(r.natureza());
        c.setAtivo(ativo(r.ativo()));
        return resposta(categorias.saveAndFlush(c));
    }

    @Transactional
    public void excluirCategoria(Long id) {
        var c = categoria(id);
        c.setExcluido(true);
        c.setAtivo(false);
        categorias.saveAndFlush(c);
    }

    public List<BancoResponse> listarBancos() {
        return bancos
            .findAll()
            .stream()
            .sorted(Comparator.comparing(Banco::getCodigo))
            .map(this::resposta)
            .toList();
    }

    @Transactional
    public BancoResponse salvarBanco(Long id, BancoRequest r) {
        var b = id == null ? new Banco() : banco(id);
        if (id != null) {
            versao(b, r.versao());
        }
        if (bancos.existsByCodigoIgnoreCaseAndIdNot(codigo(r.codigo()), id == null ? -1L : id)) {
            throw ErroNegocio.conflito("Código de banco já cadastrado.");
        }
        b.setCodigo(codigo(r.codigo()));
        b.setNome(r.nome().strip());
        b.setAtivo(ativo(r.ativo()));
        return resposta(bancos.saveAndFlush(b));
    }

    @Transactional
    public void excluirBanco(Long id) {
        var b = banco(id);
        if (agencias.existsByBancoId(id)) {
            throw ErroNegocio.conflito("Banco possui agências. Preserve o vínculo ou inative-o.");
        }
        bancos.delete(b);
        bancos.flush();
    }

    public List<AgenciaResponse> listarAgencias() {
        return agencias
            .findAll()
            .stream()
            .sorted(Comparator.comparing(Agencia::getNumero))
            .map(this::resposta)
            .toList();
    }

    @Transactional
    public AgenciaResponse salvarAgencia(Long id, AgenciaRequest r) {
        var a = id == null ? new Agencia() : agencia(id);
        var b = banco(r.bancoId());
        if (!b.isAtivo()) {
            throw ErroNegocio.regra("Selecione um banco ativo.");
        }
        if (id != null) {
            versao(a, r.versao());
            if (!a.getBanco().getId().equals(b.getId()) && contasBancarias.existsByAgenciaId(id)) {
                throw ErroNegocio.conflito(
                    "Não é possível mudar o banco de uma agência com contas vinculadas."
                );
            }
        }
        if (
            agencias.existsByBancoIdAndNumeroIgnoreCaseAndIdNot(
                b.getId(),
                codigo(r.numero()),
                id == null ? -1L : id
            )
        ) {
            throw ErroNegocio.conflito("Agência já cadastrada neste banco.");
        }
        a.setBanco(b);
        a.setNumero(codigo(r.numero()));
        a.setNome(r.nome().strip());
        a.setAtivo(ativo(r.ativo()));
        return resposta(agencias.saveAndFlush(a));
    }

    @Transactional
    public void excluirAgencia(Long id) {
        var a = agencia(id);
        if (contasBancarias.existsByAgenciaId(id)) {
            throw ErroNegocio.conflito("Agência possui contas correntes. Inative o cadastro.");
        }
        agencias.delete(a);
        agencias.flush();
    }

    public List<ContaBancariaResponse> listarContasBancarias(boolean incluirSaldo) {
        return contasBancarias
            .findAll()
            .stream()
            .filter(c -> !c.isExcluido())
            .sorted(Comparator.comparing(ContaBancaria::getDescricao))
            .map(c -> resposta(c, null))
            .toList();
    }

    @Transactional
    public ContaBancariaResponse salvarContaBancaria(Long id, ContaBancariaRequest r) {
        var c = id == null ? new ContaBancaria() : contaBancaria(id);
        var a = agencia(r.agenciaId());
        if (!a.isAtivo() || !a.getBanco().isAtivo()) {
            throw ErroNegocio.regra("Selecione uma agência e um banco ativos.");
        }
        var saldo = Dinheiro.valor(r.saldoInicial());
        if (id != null) {
            versao(c, r.versao());
            if (
                !c.getAgencia().getId().equals(a.getId()) ||
                !c.getNumero().equals(codigo(r.numero())) ||
                c.getSaldoInicial().compareTo(saldo) != 0
            ) {
                throw ErroNegocio.conflito(
                    "Agência, número e saldo inicial são estáveis. Abra outro cadastro para uma nova identidade bancária."
                );
            }
        }
        if (
            contasBancarias.existsByAgenciaIdAndNumeroIgnoreCaseAndIdNot(
                a.getId(),
                codigo(r.numero()),
                id == null ? -1L : id
            )
        ) {
            throw ErroNegocio.conflito("Conta corrente já cadastrada nesta agência.");
        }
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
        c.setExcluido(true);
        c.setAtivo(false);
        contasBancarias.saveAndFlush(c);
    }

    public PessoaResponse resposta(Pessoa p) {
        return new PessoaResponse(
            p.getId(),
            p.getVersao(),
            p.getNome(),
            p.getTipo(),
            p.getDocumento(),
            p.getEmail(),
            p.getTelefone(),
            p.getContato(),
            p.getEndereco(),
            p.getCidade(),
            p.getEstado(),
            p.getCep(),
            p.isAtivo()
        );
    }

    public CategoriaResponse resposta(Categoria c) {
        return new CategoriaResponse(
            c.getId(),
            c.getVersao(),
            c.getCodigo(),
            c.getNome(),
            c.getNatureza(),
            c.isAtivo()
        );
    }

    public BancoResponse resposta(Banco b) {
        return new BancoResponse(b.getId(), b.getVersao(), b.getCodigo(), b.getNome(), b.isAtivo());
    }

    public AgenciaResponse resposta(Agencia a) {
        return new AgenciaResponse(
            a.getId(),
            a.getVersao(),
            resposta(a.getBanco()),
            a.getNumero(),
            a.getNome(),
            a.isAtivo()
        );
    }

    public ContaBancariaResponse resposta(ContaBancaria c) {
        return resposta(c, null);
    }

    public ContaBancariaResponse respostaInterna(Long id) {
        return resposta(contaBancaria(id), Dinheiro.ZERO);
    }

    private ContaBancariaResponse resposta(ContaBancaria c, BigDecimal movimento) {
        BigDecimal saldo = movimento == null ? null : c.getSaldoInicial().add(movimento);
        return new ContaBancariaResponse(
            c.getId(),
            c.getVersao(),
            resposta(c.getAgencia()),
            c.getNumero(),
            c.getDescricao(),
            c.getSaldoInicial(),
            saldo,
            c.isAtivo()
        );
    }
}
