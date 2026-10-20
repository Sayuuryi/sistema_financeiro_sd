package br.edu.ifgoiano.financeiro;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.io.IOException;

/**
 * Reexecuta os mesmos cenários sobre um processo PostgreSQL 17 real e isolado.
 * Os binários são exclusivos de teste. A aplicação usa o PostgreSQL do Compose.
 */
class FinanceiroPostgresIntegracaoTest extends FinanceiroIntegracaoTest {
    private static final EmbeddedPostgres POSTGRES = iniciar();

    private static EmbeddedPostgres iniciar() {
        try {
            var pg = EmbeddedPostgres.builder().setPort(0).setServerConfig("listen_addresses", "127.0.0.1").start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    pg.close();
                } catch (IOException ignored) {
                }
            }));
            return pg;
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível iniciar PostgreSQL isolado para os testes.", e);
        }
    }

    @DynamicPropertySource
    static void postgres(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl("postgres", "postgres"));
        properties.add("spring.datasource.username", () -> "postgres");
        properties.add("spring.datasource.password", () -> "postgres");
        properties.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }
}
