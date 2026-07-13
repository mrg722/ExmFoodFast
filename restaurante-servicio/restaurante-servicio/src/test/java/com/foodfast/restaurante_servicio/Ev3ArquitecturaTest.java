package com.foodfast.restaurante_servicio;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class Ev3ArquitecturaTest {

    private final Path root = Path.of(System.getProperty("user.dir"));

    @Test
    void debeMantenerPatronCsr() {
        assertThat(Files.exists(root.resolve("src/main/java/com/foodfast/restaurante_servicio/controller"))).isTrue();
        assertThat(Files.exists(root.resolve("src/main/java/com/foodfast/restaurante_servicio/service"))).isTrue();
        assertThat(Files.exists(root.resolve("src/main/java/com/foodfast/restaurante_servicio/repository"))).isTrue();
        assertThat(Files.exists(root.resolve("src/main/java/com/foodfast/restaurante_servicio/model"))).isTrue();
    }

    @Test
    void debeTenerYamlDockerSwaggerYFlyway() {
        assertThat(Files.exists(root.resolve("src/main/resources/application.yml"))).isTrue();
        assertThat(Files.exists(root.resolve("src/main/resources/application-docker.yml"))).isTrue();
        assertThat(Files.exists(root.resolve("Dockerfile"))).isTrue();
        assertThat(Files.exists(root.resolve("docker-compose.restaurante.yml"))).isTrue();
        assertThat(Files.exists(root.resolve("src/main/java/com/foodfast/restaurante_servicio/config/OpenApiConfig.java"))).isTrue();
        assertThat(Files.exists(root.resolve("src/main/resources/db/migration/V1__crear_tablas_restaurante.sql"))).isTrue();
    }

    @Test
    void debeTenerClienteRestParaInteroperabilidad() {
        assertThat(Files.exists(root.resolve("src/main/java/com/foodfast/restaurante_servicio/client/NotificacionClient.java"))).isTrue();
        assertThat(Files.exists(root.resolve("src/main/java/com/foodfast/restaurante_servicio/config/RestClientConfig.java"))).isTrue();
    }

    @Test
    void debeTenerPruebasUnitariasReales() {
        assertThat(Files.exists(root.resolve("src/test/java/com/foodfast/restaurante_servicio/service/RestauranteServiceTest.java"))).isTrue();
        assertThat(Files.exists(root.resolve("src/test/java/com/foodfast/restaurante_servicio/service/HorarioRestauranteServiceTest.java"))).isTrue();
        assertThat(Files.exists(root.resolve("src/test/java/com/foodfast/restaurante_servicio/controller/RestauranteControllerTest.java"))).isTrue();
        assertThat(Files.exists(root.resolve("src/test/java/com/foodfast/restaurante_servicio/repository/RestauranteRepositoryTest.java"))).isTrue();
        assertThat(Files.exists(root.resolve("src/test/java/com/foodfast/restaurante_servicio/client/NotificacionClientTest.java"))).isTrue();
    }
}
