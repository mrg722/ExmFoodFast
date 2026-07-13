package com.foodfast.pago_servicio;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class Ev3ArquitecturaTest {

    private final Path root = Path.of("");

    @Test
    void debeTenerEstructuraCsrSwaggerYamlDockerYTests() {
        assertThat(Files.exists(root.resolve("src/main/java/com/foodfast/pago_servicio/controller/PagoController.java"))).isTrue();
        assertThat(Files.exists(root.resolve("src/main/java/com/foodfast/pago_servicio/service/PagoService.java"))).isTrue();
        assertThat(Files.exists(root.resolve("src/main/java/com/foodfast/pago_servicio/repository/PagoRepository.java"))).isTrue();
        assertThat(Files.exists(root.resolve("src/main/java/com/foodfast/pago_servicio/model/Pago.java"))).isTrue();
        assertThat(Files.exists(root.resolve("src/main/java/com/foodfast/pago_servicio/client/PedidoClient.java"))).isTrue();
        assertThat(Files.exists(root.resolve("src/main/java/com/foodfast/pago_servicio/config/OpenApiConfig.java"))).isTrue();
        assertThat(Files.exists(root.resolve("src/main/resources/application.yml"))).isTrue();
        assertThat(Files.exists(root.resolve("src/main/resources/application-docker.yml"))).isTrue();
        assertThat(Files.exists(root.resolve("Dockerfile"))).isTrue();
        assertThat(Files.exists(root.resolve("docker-compose.pago.yml"))).isTrue();
        assertThat(Files.exists(root.resolve("src/test/java/com/foodfast/pago_servicio/service/PagoServiceTest.java"))).isTrue();
        assertThat(Files.exists(root.resolve("src/test/java/com/foodfast/pago_servicio/controller/PagoControllerTest.java"))).isTrue();
    }
}
