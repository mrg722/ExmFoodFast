package com.foodfast.pedido_servicio;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class Ev3ArquitecturaTest {

    @Test
    void debeTenerEstructuraCsrYamlSwaggerTestsDocker() {
        // Given: la rúbrica EV3 pide CSR, YAML, documentación, pruebas y despliegue.

        // When / Then: se verifica que existan los archivos y carpetas clave del microservicio.
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/pedido_servicio/controller")), "Debe existir capa controller");
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/pedido_servicio/service")), "Debe existir capa service");
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/pedido_servicio/repository")), "Debe existir capa repository");
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/pedido_servicio/model")), "Debe existir capa model");
        assertTrue(Files.exists(Path.of("src/main/resources/application.yml")), "Debe existir YAML principal");
        assertTrue(Files.exists(Path.of("src/main/resources/application-docker.yml")), "Debe existir YAML para perfil docker");
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/pedido_servicio/config/OpenApiConfig.java")), "Debe existir configuración Swagger/OpenAPI");
        assertTrue(Files.exists(Path.of("Dockerfile")), "Debe existir Dockerfile");
        
    }
}
