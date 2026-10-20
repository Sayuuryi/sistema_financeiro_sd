package br.edu.ifgoiano.financeiro.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;

@Configuration
public class AplicacaoConfig {
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info().title("Sistema financeiro monolítico")
                .version("1.2.0").description(
                        "API REST do Projeto Integrador. Pessoas, cadastros bancários, previsões, contas, baixas e relatórios. Valores em BRL; datas em ISO 8601."));
    }

    @Bean
    public OpenApiCustomizer documentarErros() {
        return api -> {
            if (api.getComponents() == null)
                api.setComponents(new Components());
            api.getComponents().addSchemas("Problema", new ObjectSchema()
                    .addProperty("type", new StringSchema()).addProperty("title", new StringSchema())
                    .addProperty("status", new IntegerSchema()).addProperty("detail", new StringSchema())
                    .addProperty("instance", new StringSchema()).addProperty("codigo", new StringSchema())
                    .addProperty("violacoes", new MapSchema().additionalProperties(new StringSchema())));
            var descricoes = java.util.Map.of("400", "Entrada ou formato inválido", "404", "Registro não encontrado",
                    "409", "Conflito, duplicidade ou estado incompatível", "422", "Regra de negócio violada", "500",
                    "Falha interna sem exposição de detalhes");
            api.getPaths().values().forEach(path -> path.readOperations().forEach(op -> descricoes.forEach((codigo,
                    descricao) -> op.getResponses().addApiResponse(codigo, new ApiResponse().description(descricao)
                            .content(new Content().addMediaType("application/problem+json",
                                    new MediaType().schema(new Schema<>().$ref("#/components/schemas/Problema"))))))));
        };
    }
}
