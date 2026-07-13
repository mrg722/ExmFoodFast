package com.foodfast.inventario_servicio;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class Ev3ArquitecturaTest {

    @Test
    void debeTenerEstructuraCsrYamlDockerSwaggerYTests() {
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/inventario_servicio/controller/InventarioController.java")), "Debe existir controller");
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/inventario_servicio/service/InventarioService.java")), "Debe existir service");
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/inventario_servicio/repository/InventarioRepository.java")), "Debe existir repository");
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/inventario_servicio/model/Inventario.java")), "Debe existir model");
        assertTrue(Files.exists(Path.of("src/main/resources/application.yml")), "Debe existir YAML principal");
        assertTrue(Files.exists(Path.of("src/main/resources/application-docker.yml")), "Debe existir YAML docker");
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/inventario_servicio/config/OpenApiConfig.java")), "Debe existir configuración Swagger/OpenAPI");
        assertTrue(Files.exists(Path.of("Dockerfile")), "Debe existir Dockerfile");
        assertTrue(Files.exists(Path.of("docker-compose.inventario.yml")), "Debe existir docker compose del servicio");
    }
}
