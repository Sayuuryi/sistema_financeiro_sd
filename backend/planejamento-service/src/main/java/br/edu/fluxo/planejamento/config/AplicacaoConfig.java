package br.edu.fluxo.planejamento.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.time.Clock;
import java.time.ZoneId;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AplicacaoConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(
            new Info()
                .title("Fluxo - planejamento")
                .version("2.0.0")
                .description(
                    "Previsões de despesas e conversão em obrigações. Valores em BRL; datas ISO 8601."
                )
        );
    }

    @Bean
    public OpenApiCustomizer documentarErros() {
        return api -> {
            if (api.getComponents() == null) {
                api.setComponents(new Components());
            }
            api.getComponents().addSchemas(
                "Problema",
                new ObjectSchema()
                    .addProperty("type", new StringSchema())
                    .addProperty("title", new StringSchema())
                    .addProperty("status", new IntegerSchema())
                    .addProperty("detail", new StringSchema())
                    .addProperty("instance", new StringSchema())
                    .addProperty("codigo", new StringSchema())
                    .addProperty(
                        "violacoes",
                        new MapSchema().additionalProperties(new StringSchema())
                    )
            );
            var descricoes = java.util.Map.of(
                "400",
                "Entrada ou formato inválido",
                "404",
                "Registro não encontrado",
                "409",
                "Conflito, duplicidade ou estado incompatível",
                "422",
                "Regra de negócio violada",
                "500",
                "Falha interna sem exposição de detalhes",
                "502",
                "Resposta remota inválida",
                "503",
                "Dependência temporariamente indisponível"
            );
            api.getPaths()
                .values()
                .forEach(path ->
                    path
                        .readOperations()
                        .forEach(op ->
                            descricoes.forEach((codigo, descricao) ->
                                op
                                    .getResponses()
                                    .addApiResponse(
                                        codigo,
                                        new ApiResponse()
                                            .description(descricao)
                                            .content(
                                                new Content().addMediaType(
                                                    "application/problem+json",
                                                    new MediaType().schema(
                                                        new Schema<>().$ref(
                                                            "#/components/schemas/Problema"
                                                        )
                                                    )
                                                )
                                            )
                                    )
                            )
                        )
                );
        };
    }
}
