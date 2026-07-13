package com.foodfast.autenticacion_servicio;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Ev3ArquitecturaTest {

    @Test
    void debeTenerEstructuraBaseEv3() {
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/autenticacion_servicio/controller")), "Debe existir capa controller");
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/autenticacion_servicio/service")), "Debe existir capa service");
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/autenticacion_servicio/repository")), "Debe existir capa repository");
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/autenticacion_servicio/security")), "Debe existir capa security/JWT");
        assertTrue(Files.exists(Path.of("src/test/resources/application-test.properties")), "Debe existir perfil H2 de pruebas");
        assertTrue(Files.exists(Path.of("Dockerfile")), "Debe existir Dockerfile");
        assertTrue(Files.exists(Path.of("docker-compose.yml")), "Debe existir docker-compose.yml");
        assertTrue(Files.exists(Path.of("docker-compose.auth.yml")), "Debe existir compose especifico del microservicio");
        assertTrue(Files.exists(Path.of("src/main/resources/application.yml")), "Debe existir configuracion YAML principal");
        assertTrue(Files.exists(Path.of("src/main/resources/application-docker.yml")), "Debe existir perfil docker");

    }
}
