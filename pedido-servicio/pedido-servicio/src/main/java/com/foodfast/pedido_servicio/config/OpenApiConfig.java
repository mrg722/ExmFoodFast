package com.foodfast.pedido_servicio.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("FoodFast - Pedido Servicio API")
                        .version("1.0.0")
                        .description("Documentación técnica del microservicio de pedidos. Expone endpoints CRUD, cancelación de pedidos y comunicación REST con inventario-servicio para validar y descontar stock.")
                        .contact(new Contact().name("Equipo FoodFast")))
                .servers(List.of(
                        new Server().url("http://localhost:8083").description("Pedido servicio local"),
                        new Server().url("http://localhost:8080").description("Acceso por API Gateway")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
