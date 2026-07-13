package com.foodfast.notificacion_servicio;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class Ev3ArquitecturaTest {

    @Test
    void debeTenerEstructuraCsrYamlDockerSwaggerYTests() {
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/notificacion_servicio/controller/NotificacionController.java")));
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/notificacion_servicio/service/NotificacionService.java")));
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/notificacion_servicio/repository/NotificacionRepository.java")));
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/notificacion_servicio/model/Notificacion.java")));
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/notificacion_servicio/client/PedidoClient.java")));
        assertTrue(Files.exists(Path.of("src/main/resources/application.yml")));
        assertTrue(Files.exists(Path.of("src/main/resources/application-docker.yml")));
        assertTrue(Files.exists(Path.of("Dockerfile")));
        assertTrue(Files.exists(Path.of("docker-compose.notificacion.yml")));
        assertTrue(Files.exists(Path.of("src/test/java/com/foodfast/notificacion_servicio/service/NotificacionServiceTest.java")));
        assertTrue(Files.exists(Path.of("src/test/java/com/foodfast/notificacion_servicio/controller/NotificacionControllerTest.java")));
    }
}
