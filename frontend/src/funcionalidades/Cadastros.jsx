import {
  Archive,
  Building,
  Landmark,
  Pencil,
  Plus,
  Tags,
  Trash2,
  Users,
  Wallet,
} from "lucide-react";
import { useApp } from "../contexto/AppContext.jsx";
import { api } from "../core/api.js";
import { useCadastros } from "../core/consultas.js";
import { enums, labels, money } from "../core/formatacao.js";
import { ActionButton, Badge, Loading, Offline, PageHead } from "../componentes/Layout.jsx";
import {
  ActiveField,
  Field,
  FormEditor,
  SelectField,
  numberIds,
} from "../componentes/Formularios.jsx";
import { useConfirmacoes } from "../componentes/AcoesConfirmacao.jsx";

export const masterLabels = {
  pessoas: "Pessoas",
  categorias: "Categorias",
  bancos: "Bancos",
  agencias: "Agências",
  "contas-bancarias": "Contas bancárias",
};

const ARQUIVAVEIS = ["pessoas", "categorias", "contas-bancarias"];

// Nome no singular e se o título usa "Nova" ou "Novo"
const SINGULAR = {
  pessoas: ["pessoa", true],
  categorias: ["categoria", true],
  bancos: ["banco", false],
  agencias: ["agência", true],
  "contas-bancarias": ["conta bancária", true],
};

export function tituloCadastro(tipo, editando) {
  const [nome, feminino] = SINGULAR[tipo];
  return (editando ? "Editar" : feminino ? "Nova" : "Novo") + " " + nome;
}

const ICONES = {
  pessoas: Users,
  categorias: Tags,
  bancos: Landmark,
  agencias: Building,
  "contas-bancarias": Wallet,
};

function descrever(type, r) {
  if (type === "pessoas") {
    return {
      main: r.nome,
      info: labels[r.tipo],
      detail: r.email || r.telefone || r.documento || "Sem contato informado",
    };
  }
  if (type === "categorias") {
    return { main: r.codigo + " · " + r.nome, info: labels[r.natureza], detail: "" };
  }
  if (type === "bancos") {
    return { main: r.codigo + " · " + r.nome, info: "Banco", detail: "" };
  }
  if (type === "agencias") {
    return { main: r.numero + " · " + r.nome, info: r.banco.nome, detail: "" };
  }
  return {
    main: r.descricao,
    info: money(r.saldoInicial) + " · saldo inicial",
    detail: r.agencia.banco.nome + " · Ag. " + r.agencia.numero + " · C/C " + r.numero,
  };
}

export function Cadastros() {
  const { master: type, setMaster, openModal } = useApp();
  const { confirmDelete } = useConfirmacoes();
  const cadastros = useCadastros();

  if (cadastros.carregando) return <Loading />;
  if (cadastros.erro) {
    return (
      <Offline title="Cadastros" message={cadastros.erro.message} onRetry={cadastros.recarregar} />
    );
  }

  const rows = cadastros.dados[type];
  const Icone = ICONES[type];
  const abrirForm = (row) =>
    openModal(
      tituloCadastro(type, Boolean(row?.id)),
      <CadastroForm type={type} row={row} dados={cadastros.dados} />,
    );

  return (
    <>
      <PageHead
        eyebrow="REGISTROS"
        title="Cadastros"
        subtitle="Pessoas, categorias, bancos, agências e contas bancárias."
        image="/assets/images/cadastros.svg"
        actions={
          <ActionButton onClick={() => abrirForm()} style="primary">
            <Plus size={15} aria-hidden="true" /> Novo cadastro
          </ActionButton>
        }
      />
      <div className="tabs">
        {Object.entries(masterLabels).map(([key, label]) => {
          const Icone = ICONES[key];
          return (
            <button
              className={"tab" + (key === type ? " active" : "")}
              key={key}
              onClick={() => setMaster(key)}>
              <Icone size={14} aria-hidden="true" /> {label}
            </button>
          );
        })}
      </div>
      <section className="panel">
        <div className="panel-head">
          <div className="panel-title">
            <span className="panel-title-icon">
              <Icone size={16} aria-hidden="true" />
            </span>
            <h2>{masterLabels[type]}</h2>
          </div>
          <span className="sub">{rows.length} registro(s)</span>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Identificação</th>
                <th>Informações</th>
                <th>Situação</th>
                <th className="right">Ações</th>
              </tr>
            </thead>
            <tbody>
              {rows.length ? (
                rows.map((r) => {
                  const { main, info, detail } = descrever(type, r);
                  return (
                    <tr key={r.id}>
                      <td>
                        <strong>{main}</strong>
                        <small>{detail}</small>
                      </td>
                      <td>{info}</td>
                      <td>
                        <Badge s={r.ativo ? "ATIVO" : "INATIVA"} />
                      </td>
                      <td className="right">
                        <div className="actions" style={{ justifyContent: "flex-end" }}>
                          <button className="button small" onClick={() => abrirForm(r)}>
                            <Pencil size={13} aria-hidden="true" /> Editar
                          </button>
                          <button
                            className="button small danger"
                            onClick={() => confirmDelete("/api/" + type + "/" + r.id)}>
                            {ARQUIVAVEIS.includes(type) ? (
                              <Archive size={13} aria-hidden="true" />
                            ) : (
                              <Trash2 size={13} aria-hidden="true" />
                            )}
                            {ARQUIVAVEIS.includes(type) ? "Arquivar" : "Excluir"}
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })
              ) : (
                <tr>
                  <td colSpan={4} className="empty">
                    Nenhum cadastro encontrado.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>
      <p className="sub page-note">
        Arquivar tira o registro das próximas operações e mantém o histórico.
      </p>
    </>
  );
}

function CadastroForm({ type, row = {}, dados }) {
  // Campos que não mudam depois de criados
  const estaveis = !row.id
    ? {}
    : type === "pessoas"
      ? { tipo: row.tipo }
      : type === "categorias"
        ? { natureza: row.natureza }
        : type === "contas-bancarias"
          ? { agenciaId: row.agencia.id, numero: row.numero, saldoInicial: row.saldoInicial }
          : {};
  const fixo = (name) => name in estaveis;

  async function salvar(o) {
    Object.assign(o, estaveis);
    numberIds(o);
    o.ativo = o.ativo === "true";
    if ("saldoInicial" in o) {
      o.saldoInicial = o.saldoInicial || "0";
    }
    for (const k in o) {
      if (o[k] === "") {
        o[k] = null;
      }
    }
    if (row.id) {
      o.versao = row.versao;
    }
    await api("/api/" + type + (row.id ? "/" + row.id : ""), {
      method: row.id ? "PUT" : "POST",
      body: o,
    });
  }

  return (
    <FormEditor onSubmit={salvar}>
      {type === "pessoas" ? (
        <>
          <Field name="nome" label="Nome *" value={row.nome} required maxLength={120} wide />
          <SelectField
            name="tipo"
            label="Tipo de pessoa *"
            options={enums(["CLIENTE", "FORNECEDOR", "AMBOS"])}
            value={row.tipo}
            disabled={fixo("tipo")}
          />
          <Field
            name="documento"
            label="CPF / CNPJ (somente dígitos)"
            value={row.documento}
            pattern="[0-9]{11}|[0-9]{14}"
            maxLength={14}
          />
          <Field name="email" label="E-mail" type="email" value={row.email} maxLength={150} />
          <Field name="telefone" label="Telefone" value={row.telefone} maxLength={30} />
          <Field name="contato" label="Pessoa de contato" value={row.contato} maxLength={100} />
          <Field name="endereco" label="Endereço" value={row.endereco} maxLength={200} wide />
          <Field name="cidade" label="Cidade" value={row.cidade} maxLength={80} />
          <Field name="estado" label="UF" value={row.estado} pattern="[A-Za-z]{2}" maxLength={2} />
          <Field
            name="cep"
            label="CEP"
            value={row.cep}
            pattern="[0-9]{5}-?[0-9]{3}"
            maxLength={9}
          />
        </>
      ) : null}
      {type === "categorias" ? (
        <>
          <Field name="codigo" label="Código *" value={row.codigo} required maxLength={20} />
          <SelectField
            name="natureza"
            label="Natureza *"
            options={enums(["DESPESA", "RECEITA"])}
            value={row.natureza}
            disabled={fixo("natureza")}
          />
          <Field name="nome" label="Nome *" value={row.nome} required maxLength={100} wide />
        </>
      ) : null}
      {type === "bancos" ? (
        <>
          <Field name="codigo" label="Código *" value={row.codigo} required maxLength={10} />
          <Field name="nome" label="Nome *" value={row.nome} required maxLength={120} />
        </>
      ) : null}
      {type === "agencias" ? (
        <>
          <SelectField
            name="bancoId"
            label="Banco *"
            options={dados.bancos
              .filter((b) => b.ativo || b.id === row.banco?.id)
              .map((b) => [b.id, b.codigo + " · " + b.nome])}
            value={row.banco?.id}
          />
          <Field name="numero" label="Número *" value={row.numero} required maxLength={20} />
          <Field name="nome" label="Nome *" value={row.nome} required maxLength={120} wide />
        </>
      ) : null}
      {type === "contas-bancarias" ? (
        <>
          <SelectField
            name="agenciaId"
            label="Agência *"
            options={dados.agencias
              .filter((a) => a.ativo || a.id === row.agencia?.id)
              .map((a) => [a.id, a.banco.nome + " · " + a.numero])}
            value={row.agencia?.id}
            disabled={fixo("agenciaId")}
          />
          <Field
            name="numero"
            label="Conta / dígito *"
            value={row.numero}
            required
            maxLength={30}
            disabled={fixo("numero")}
          />
          <Field
            name="descricao"
            label="Descrição *"
            value={row.descricao}
            required
            maxLength={120}
          />
          <Field
            name="saldoInicial"
            label="Saldo inicial (R$)"
            type="number"
            value={row.saldoInicial ?? "0"}
            step="0.01"
            required
            disabled={fixo("saldoInicial")}
          />
        </>
      ) : null}
      <ActiveField value={row.ativo ?? true} />
      {Object.keys(estaveis).length ? (
        <p className="notice wide">
          Os campos de identificação destacados permanecem fixos. Para outra classificação ou conta
          bancária, crie um novo cadastro.
        </p>
      ) : null}
    </FormEditor>
  );
}
