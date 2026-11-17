package br.edu.fluxo.receber.seguranca;

// Usuário dono da requisição em andamento
public final class UsuarioAtual {

    private record Dados(long id, String token) {}

    private static final ThreadLocal<Dados> ATUAL = new ThreadLocal<>();

    private UsuarioAtual() {}

    public static void definir(long id, String token) {
        ATUAL.set(new Dados(id, token));
    }

    public static Long id() {
        var d = ATUAL.get();
        return d == null ? null : d.id();
    }

    public static String token() {
        var d = ATUAL.get();
        return d == null ? null : d.token();
    }

    public static void limpar() {
        ATUAL.remove();
    }
}
