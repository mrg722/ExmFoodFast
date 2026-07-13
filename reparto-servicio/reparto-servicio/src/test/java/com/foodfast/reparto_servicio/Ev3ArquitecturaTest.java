package com.foodfast.reparto_servicio;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class Ev3ArquitecturaTest {

    @Test
    void debeTenerEstructuraCsrYPaquetesPrincipales() {
        Path base = Path.of("src/main/java/com/foodfast/reparto_servicio");

        assertThat(Files.exists(base.resolve("controller"))).isTrue();
        assertThat(Files.exists(base.resolve("service"))).isTrue();
        assertThat(Files.exists(base.resolve("repository"))).isTrue();
        assertThat(Files.exists(base.resolve("model"))).isTrue();
        assertThat(Files.exists(base.resolve("dto"))).isTrue();
        assertThat(Files.exists(base.resolve("client"))).isTrue();
    }

    @Test
    void debeTenerArchivosEv3Obligatorios() {
        assertThat(Files.exists(Path.of("src/main/resources/application.yml"))).isTrue();
        assertThat(Files.exists(Path.of("src/main/resources/application-docker.yml"))).isTrue();
        assertThat(Files.exists(Path.of("Dockerfile"))).isTrue();
        assertThat(Files.exists(Path.of("docker-compose.reparto.yml"))).isTrue();
    }

    @Test
    void debeTenerTestsRealesParaCobertura() {
        assertThat(Files.exists(Path.of("src/test/java/com/foodfast/reparto_servicio/service/EntregaServiceTest.java"))).isTrue();
        assertThat(Files.exists(Path.of("src/test/java/com/foodfast/reparto_servicio/service/RepartidorServiceTest.java"))).isTrue();
        assertThat(Files.exists(Path.of("src/test/java/com/foodfast/reparto_servicio/controller/EntregaControllerTest.java"))).isTrue();
        assertThat(Files.exists(Path.of("src/test/java/com/foodfast/reparto_servicio/controller/RepartidorControllerTest.java"))).isTrue();
        assertThat(Files.exists(Path.of("src/test/java/com/foodfast/reparto_servicio/repository/RepartoRepositoryTest.java"))).isTrue();
    }
}
