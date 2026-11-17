import { useCallback, useState } from "react";
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import {
  Ban,
  ChevronLeft,
  ChevronRight,
  Download,
  Filter,
  HandCoins,
  Pencil,
  Plus,
  Trash2,
  Undo2,
  X,
} from "lucide-react";
import { useApp } from "../contexto/AppContext.jsx";
import { accountPath, api, download, params } from "../core/api.js";
import { ROTAS_REFERENCIA, estadoDe, useCadastros } from "../core/consultas.js";
import { FORMAS_PAGAMENTO, date, enums, labels, money } from "../core/formatacao.js";
import {
  AVISO_CADASTROS_OFFLINE,
  ActionButton,
  Badge,
  Loading,
  Offline,
  PageHead,
} from "../componentes/Layout.jsx";
import { AccountRows } from "../componentes/TabelaContas.jsx";
import {
  Field,
  FormEditor,
  SelectField,
  TextArea,
  numberIds,
} from "../componentes/Formularios.jsx";
import { useConfirmacoes } from "../componentes/AcoesConfirmacao.jsx";

const TITULOS = { PAGAR: "Contas a pagar", RECEBER: "Contas a receber" };

export function Contas({ tipo }) {
  const { openModal, toast } = useApp();
  const [filters, setFilters] = useState({});
  const [page, setPage] = useState(0);
  const [versaoFiltros, setVersaoFiltros] = useState(0);
  const abrirConta = useAbrirConta();

  const referencias = useCadastros(ROTAS_REFERENCIA, { timeout: 1800 });
  const lista = useQuery({
    queryKey: ["contas", tipo, filters, page],
    queryFn: () =>
      api(accountPath(tipo) + "?" + params({ tipo, ...filters, pagina: page, tamanho: 10 })),
    placeholderData: keepPreviousData,
  });
  const estado = estadoDe(lista);

  if (estado.carregando || referencias.carregando) return <Loading />;
  if (estado.erro) {
    return (
      <Offline title={TITULOS[tipo]} message={estado.erro.message} onRetry={estado.recarregar} />
    );
  }

  const data = lista.data;
  const f = filters;
  const { pessoas, categorias } = referencias.dados;
  const status = [
    "ABERTA",
    "PARCIAL",
    "VENCIDA",
    tipo === "PAGAR" ? "PAGA" : "RECEBIDA",
    "CANCELADA",
  ];

  function filtrar(evento) {
    evento.preventDefault();
    setFilters(Object.fromEntries(new FormData(evento.target)));
    setPage(0);
  }

  function limpar() {
    setFilters({});
    setPage(0);
    setVersaoFiltros((v) => v + 1);
  }

  function novaConta() {
    if (!referencias.disponiveis) {
      toast("Cadastros indisponível no momento. Você pode continuar consultando os registros.");
      return;
    }
    openModal(
      "Nova conta " + (tipo === "RECEBER" ? "a receber" : "a pagar"),
      <ContaForm tipo={tipo} />,
    );
  }

  function exportar() {
    download(accountPath(tipo) + "/exportar.csv?" + params({ tipo, ...filters })).catch((error) =>
      toast(error.message),
    );
  }

  return (
    <>
      {referencias.disponiveis ? null : (
        <div className="notice warning" role="status">
          {AVISO_CADASTROS_OFFLINE}
        </div>
      )}
      <PageHead
        eyebrow={tipo === "PAGAR" ? "SAÍDAS" : "ENTRADAS"}
        title={TITULOS[tipo]}
        subtitle={
          tipo === "PAGAR"
            ? "Títulos de fornecedores e registro de pagamentos."
            : "Títulos de clientes e registro de recebimentos."
        }
        image={
          tipo === "PAGAR" ? "/assets/images/contas-pagar.svg" : "/assets/images/contas-receber.svg"
        }
        actions={
          <>
            <ActionButton onClick={exportar}>
              <Download size={14} aria-hidden="true" /> Exportar CSV
            </ActionButton>
            <ActionButton onClick={novaConta} style="primary">
              <Plus size={15} aria-hidden="true" /> Nova conta
            </ActionButton>
          </>
        }
      />
      <section className="panel">
        <form id="account-filters" className="filters" onSubmit={filtrar} key={versaoFiltros}>
          <input
            type="search"
            name="busca"
            placeholder="Buscar descrição, título ou pessoa"
            aria-label="Buscar contas"
            defaultValue={f.busca || ""}
          />
          <select name="situacao" aria-label="Situação" defaultValue={f.situacao || ""}>
            <option value="">Todas as situações</option>
            {status.map((s) => (
              <option value={s} key={s}>
                {labels[s]}
              </option>
            ))}
          </select>
          <select name="pessoaId" aria-label="Pessoa" defaultValue={f.pessoaId || ""}>
            <option value="">Todas as pessoas</option>
            {pessoas
              .filter(
                (p) =>
                  p.tipo === "AMBOS" || p.tipo === (tipo === "PAGAR" ? "FORNECEDOR" : "CLIENTE"),
              )
              .map((p) => (
                <option value={p.id} key={p.id}>
                  {p.nome}
                </option>
              ))}
          </select>
          <select name="categoriaId" aria-label="Categoria" defaultValue={f.categoriaId || ""}>
            <option value="">Todas as categorias</option>
            {categorias
              .filter((c) => c.natureza === (tipo === "PAGAR" ? "DESPESA" : "RECEITA"))
              .map((c) => (
                <option value={c.id} key={c.id}>
                  {c.nome}
                </option>
              ))}
          </select>
          <label>
            Vencimento de
            <input type="date" name="vencimentoDe" defaultValue={f.vencimentoDe || ""} />
          </label>
          <label>
            até
            <input type="date" name="vencimentoAte" defaultValue={f.vencimentoAte || ""} />
          </label>
          <label>
            Emissão de
            <input type="date" name="emissaoDe" defaultValue={f.emissaoDe || ""} />
          </label>
          <label>
            até
            <input type="date" name="emissaoAte" defaultValue={f.emissaoAte || ""} />
          </label>
          <button className="button small" type="submit">
            <Filter size={13} aria-hidden="true" /> Filtrar
          </button>
          <button className="button small" type="button" onClick={limpar}>
            <X size={13} aria-hidden="true" /> Limpar
          </button>
        </form>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Título / pessoa</th>
                <th>Categoria</th>
                <th>Vencimento</th>
                <th className="right">Saldo a quitar</th>
                <th>Situação</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <AccountRows rows={data.itens} onDetail={abrirConta} />
            </tbody>
          </table>
        </div>
        <div className="table-count">
          <span>
            {data.total} título(s) · página {data.totalPaginas ? data.pagina + 1 : 0} de{" "}
            {data.totalPaginas}
          </span>
          <div className="actions">
            <button
              className="button small"
              disabled={page === 0}
              onClick={() => setPage((p) => p - 1)}>
              <ChevronLeft size={13} aria-hidden="true" /> Anterior
            </button>
            <button
              className="button small"
              disabled={page + 1 >= data.totalPaginas}
              onClick={() => setPage((p) => p + 1)}>
              Próxima <ChevronRight size={13} aria-hidden="true" />
            </button>
          </div>
        </div>
      </section>
    </>
  );
}

// conversion: usado ao gerar conta de uma previsão
export function AccountFields({
  row = {},
  conversion = false,
  tipo,
  pessoas = [],
  categorias = [],
}) {
  const { today } = useApp();
  const type = row.tipo || tipo || "PAGAR";
  return (
    <>
      {conversion ? null : (
        <>
          <SelectField
            name="pessoaId"
            label={type === "PAGAR" ? "Fornecedor *" : "Cliente *"}
            options={pessoas
              .filter(
                (p) =>
                  (p.ativo || p.id === row.pessoa?.id) &&
                  (p.tipo === "AMBOS" || p.tipo === (type === "PAGAR" ? "FORNECEDOR" : "CLIENTE")),
              )
              .map((p) => [p.id, p.nome])}
            value={row.pessoa?.id}
          />
          <SelectField
            name="categoriaId"
            label="Categoria *"
            options={categorias
              .filter(
                (c) =>
                  (c.ativo || c.id === row.categoria?.id) &&
                  c.natureza === (type === "PAGAR" ? "DESPESA" : "RECEITA"),
              )
              .map((c) => [c.id, c.codigo + " · " + c.nome])}
            value={row.categoria?.id}
          />
          <Field
            name="descricao"
            label="Descrição *"
            value={row.descricao}
            required
            maxLength={180}
            wide
          />
        </>
      )}
      <Field
        name="numeroTitulo"
        label="Número do título *"
        value={row.numeroTitulo}
        required
        maxLength={60}
      />
      <SelectField
        name="tipoTitulo"
        label="Documento *"
        options={enums(["DUPLICATA", "NOTA_PROMISSORIA", "OUTROS"])}
        value={row.tipoTitulo || "DUPLICATA"}
      />
      <Field
        name="emissao"
        label="Data de emissão *"
        type="date"
        value={row.emissao || today}
        required
      />
      <Field
        name="vencimento"
        label="Vencimento *"
        type="date"
        value={row.vencimento || row.dataPrevista || today}
        required
      />
      <Field
        name="valor"
        label="Valor definitivo (R$) *"
        type="number"
        value={row.valor ?? row.valorPrevisto ?? ""}
        required
        min="0.01"
        step="0.01"
      />
      <Field
        name="descontoMaximo"
        label="Desconto máximo acumulado (R$)"
        type="number"
        value={row.descontoMaximo ?? "0"}
        min="0"
        step="0.01"
      />
      <Field
        name="validadeDesconto"
        label="Validade do desconto"
        type="date"
        value={row.validadeDesconto}
      />
      <TextArea name="observacoes" label="Observações" value={row.observacoes} />
    </>
  );
}

export function ContaForm({ row = {}, tipo }) {
  const referencias = useCadastros(["pessoas", "categorias"], { timeout: 1800 });
  if (referencias.carregando) return <Loading>Carregando cadastros…</Loading>;
  const type = row.tipo || tipo || "PAGAR";

  async function salvar(o) {
    numberIds(o);
    o.tipo = type;
    o.validadeDesconto = o.validadeDesconto || null;
    o.descontoMaximo = o.descontoMaximo || "0";
    if (row.id) {
      o.versao = row.versao;
    }
    await api(accountPath(o.tipo) + (row.id ? "/" + row.id : ""), {
      method: row.id ? "PUT" : "POST",
      body: o,
    });
  }

  return (
    <>
      {referencias.disponiveis ? null : (
        <div className="notice warning" role="status">
          {AVISO_CADASTROS_OFFLINE}
        </div>
      )}
      <FormEditor onSubmit={salvar}>
        <AccountFields
          row={row}
          tipo={type}
          pessoas={referencias.dados.pessoas}
          categorias={referencias.dados.categorias}
        />
      </FormEditor>
    </>
  );
}

export function useAbrirConta() {
  const { openModal, toast } = useApp();
  return useCallback(
    async (id, tipo = "PAGAR") => {
      try {
        const { conta, historico } = await api(accountPath(tipo) + "/" + id);
        openModal(
          "Título " + conta.numeroTitulo,
          <ContaDetalhe conta={conta} historico={historico} />,
        );
      } catch (e) {
        toast(e.message);
      }
    },
    [openModal, toast],
  );
}

function ContaDetalhe({ conta: c, historico: h }) {
  const { openModal } = useApp();
  const { reasonForm, confirmDelete } = useConfirmacoes();
  const route = accountPath(c.tipo);
  const canSettle = Number(c.saldo) > 0 && c.situacao !== "CANCELADA";

  return (
    <>
      <span className="eyebrow">{c.tipo === "PAGAR" ? "CONTA A PAGAR" : "CONTA A RECEBER"}</span>
      <h2 style={{ margin: "9px 0" }}>{c.descricao}</h2>
      <p className="sub">
        {c.pessoa.nome} · {c.categoria.nome} · <Badge s={c.situacao} />
      </p>
      <div className="detail-metrics">
        <div>
          Valor original
          <strong>{money(c.valor)}</strong>
        </div>
        <div>
          Principal baixado
          <strong>{money(c.totalBaixado)}</strong>
        </div>
        <div>
          Saldo a quitar
          <strong>{money(c.saldo)}</strong>
        </div>
      </div>
      <p className="sub">
        Emissão: {date(c.emissao)} · Vencimento: {date(c.vencimento)}
        <br />
        Desconto permitido: {money(c.descontoMaximo)} até {date(c.validadeDesconto)}
        <br />
        Descontos utilizados: {money(c.totalDesconto)} · Acréscimos: {money(c.totalAcrescimo)}
        <br />
        {c.despesaOrigemId ? (
          <>
            Origem: previsão #{c.despesaOrigemId}
            <br />
          </>
        ) : null}
        {c.observacoes || ""}
        {c.motivoCancelamento ? (
          <>
            <br />
            Cancelamento: {c.motivoCancelamento}
          </>
        ) : null}
      </p>
      <div className="actions" style={{ margin: "20px 0" }}>
        {canSettle ? (
          <button
            className="button primary"
            id="settle"
            onClick={() =>
              openModal(
                c.tipo === "PAGAR" ? "Registrar pagamento" : "Registrar recebimento",
                <BaixaForm conta={c} />,
              )
            }>
            <HandCoins size={14} aria-hidden="true" />
            {c.tipo === "PAGAR" ? "Registrar pagamento" : "Registrar recebimento"}
          </button>
        ) : null}
        {!c.possuiHistorico && c.situacao !== "CANCELADA" ? (
          <button
            className="button"
            id="edit-title"
            onClick={() => openModal("Editar título", <ContaForm row={c} />)}>
            <Pencil size={14} aria-hidden="true" /> Editar título
          </button>
        ) : null}
        {c.situacao !== "CANCELADA" ? (
          <button
            className="button danger"
            id="cancel-title"
            onClick={() =>
              reasonForm(
                "Cancelar título",
                route + "/" + c.id + "/cancelamento",
                "Estorne todas as baixas ativas antes de cancelar. O título e seu histórico serão preservados.",
              )
            }>
            <Ban size={14} aria-hidden="true" /> Cancelar título
          </button>
        ) : null}
        {!c.possuiHistorico && !c.despesaOrigemId ? (
          <button
            className="button danger"
            id="delete-title"
            onClick={() => confirmDelete(route + "/" + c.id)}>
            <Trash2 size={14} aria-hidden="true" /> Excluir
          </button>
        ) : null}
      </div>
      <h3>Histórico de pagamentos / recebimentos</h3>
      {h.length ? (
        h.map((b) => (
          <div className={"history-item" + (b.estornada ? " reversed" : "")} key={b.id}>
            <div className="history-title">
              <strong>
                {date(b.data)} · {money(b.valorMovimentado)}
              </strong>
              {b.estornada ? (
                <Badge s="ESTORNADA" />
              ) : (
                <button
                  className="button small danger"
                  onClick={() =>
                    reasonForm(
                      "Estornar baixa",
                      route + "/" + c.id + "/baixas/" + b.id + "/estorno",
                      "O movimento deixará de compor o saldo e o título será recalculado. A baixa permanecerá no histórico.",
                    )
                  }>
                  <Undo2 size={13} aria-hidden="true" /> Estornar
                </button>
              )}
            </div>
            <p>
              Principal {money(b.principal)} + acréscimo {money(b.acrescimo)} − desconto{" "}
              {money(b.desconto)}
              <br />
              {labels[b.forma]} · {b.contaBancaria?.nome || "Sem conta bancária"}
              <br />
              {b.observacoes || ""}
              {b.estornada ? (
                <>
                  <br />
                  Estornada: {b.motivoEstorno}
                </>
              ) : null}
            </p>
          </div>
        ))
      ) : (
        <p className="sub" style={{ marginTop: 14 }}>
          Nenhuma baixa registrada.
        </p>
      )}
    </>
  );
}

function BaixaForm({ conta: c }) {
  const { today } = useApp();
  // Mesma chave em nova tentativa evita baixa duplicada
  const [key] = useState(() => crypto.randomUUID());
  const referencias = useCadastros(["contas-bancarias"], { timeout: 1800 });
  if (referencias.carregando) return <Loading>Carregando contas bancárias…</Loading>;

  async function salvar(o) {
    numberIds(o);
    o.acrescimo = o.acrescimo || "0";
    o.desconto = o.desconto || "0";
    await api(accountPath(c.tipo) + "/" + c.id + "/baixas", {
      method: "POST",
      headers: { "Idempotency-Key": key },
      body: o,
    });
  }

  return (
    <>
      <div className="notice">
        Saldo do título: <strong>{money(c.saldo)}</strong>. Pix, Pix Automático, transferência,
        débito automático e cheque exigem conta bancária.
      </div>
      <FormEditor submitLabel="Confirmar baixa" onSubmit={salvar}>
        <Field
          name="data"
          label="Data da baixa *"
          type="date"
          value={today}
          required
          min={c.emissao}
          max={today}
        />
        <SelectField name="forma" label="Forma *" options={enums(FORMAS_PAGAMENTO)} value="PIX" />
        <Field
          name="principal"
          label="Principal a quitar (R$) *"
          type="number"
          value={c.saldo}
          required
          min="0.01"
          max={c.saldo}
          step="0.01"
        />
        <SelectField
          name="contaBancariaId"
          label="Conta bancária"
          options={referencias.dados["contas-bancarias"]
            .filter((b) => b.ativo && b.agencia.ativo && b.agencia.banco.ativo)
            .map((b) => [b.id, b.descricao + " · " + b.numero])}
          value=""
          required={false}
        />
        <Field
          name="acrescimo"
          label="Acréscimo (R$)"
          type="number"
          value="0"
          min="0"
          step="0.01"
        />
        <Field name="desconto" label="Desconto (R$)" type="number" value="0" min="0" step="0.01" />
        <TextArea name="observacoes" label="Observações" />
      </FormEditor>
    </>
  );
}
