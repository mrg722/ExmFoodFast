package com.foodfast.resena_servicio;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class Ev3ArquitecturaTest {

    private final Path root = Path.of("");

    @Test
    void debeTenerEstructuraCsrYamlDockerYTestsEv3() {
        assertThat(Files.exists(root.resolve("pom.xml"))).isTrue();
        assertThat(Files.exists(root.resolve("Dockerfile"))).isTrue();
        assertThat(Files.exists(root.resolve("docker-compose.resena.yml"))).isTrue();
        assertThat(Files.exists(root.resolve("src/main/resources/application.yml"))).isTrue();
        assertThat(Files.exists(root.resolve("src/main/resources/application-docker.yml"))).isTrue();

        assertThat(Files.exists(root.resolve("src/main/java/com/foodfast/resena_servicio/controller/ResenaController.java"))).isTrue();
        assertThat(Files.exists(root.resolve("src/main/java/com/foodfast/resena_servicio/service/ResenaService.java"))).isTrue();
        assertThat(Files.exists(root.resolve("src/main/java/com/foodfast/resena_servicio/repository/ResenaRepository.java"))).isTrue();
        assertThat(Files.exists(root.resolve("src/main/java/com/foodfast/resena_servicio/model/Resena.java"))).isTrue();
        assertThat(Files.exists(root.resolve("src/main/java/com/foodfast/resena_servicio/client/CatalogoClient.java"))).isTrue();
    }
}
