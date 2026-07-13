package com.foodfast.cliente_servicio;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class Ev3ArquitecturaTest {

    @Test
    void debeTenerEstructuraCsrYamlDockerSwaggerYTests() {
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/cliente_servicio/controller/ClienteController.java")));
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/cliente_servicio/service/ClienteService.java")));
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/cliente_servicio/repository/ClienteRepository.java")));
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/cliente_servicio/model/Cliente.java")));
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/cliente_servicio/client/PedidoClient.java")));
        assertTrue(Files.exists(Path.of("src/main/resources/application.yml")));
        assertTrue(Files.exists(Path.of("src/main/resources/application-docker.yml")));
        assertTrue(Files.exists(Path.of("Dockerfile")));
        assertTrue(Files.exists(Path.of("docker-compose.cliente.yml")));
        assertTrue(Files.exists(Path.of("src/test/java/com/foodfast/cliente_servicio/service/ClienteServiceTest.java")));
        assertTrue(Files.exists(Path.of("src/test/java/com/foodfast/cliente_servicio/controller/ClienteControllerTest.java")));
    }
}
