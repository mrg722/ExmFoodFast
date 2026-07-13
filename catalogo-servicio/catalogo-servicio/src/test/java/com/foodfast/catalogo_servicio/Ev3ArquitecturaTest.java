package com.foodfast.catalogo_servicio;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class Ev3ArquitecturaTest {

    @Test
    void debeTenerEstructuraBaseEv3() {
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/catalogo_servicio/controller")));
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/catalogo_servicio/service")));
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/catalogo_servicio/repository")));
        assertTrue(Files.exists(Path.of("src/main/java/com/foodfast/catalogo_servicio/model")));
        assertTrue(Files.exists(Path.of("src/main/resources/application.yml")));
        assertTrue(Files.exists(Path.of("src/main/resources/application-docker.yml")));
        assertTrue(Files.exists(Path.of("Dockerfile")));
        assertTrue(Files.exists(Path.of("docker-compose.yml")));

    }

    @Test
    void debeTenerPruebasRealesParaCobertura() {
        assertTrue(Files.exists(Path.of("src/test/java/com/foodfast/catalogo_servicio/service/CategoriaServiceTest.java")));
        assertTrue(Files.exists(Path.of("src/test/java/com/foodfast/catalogo_servicio/service/ProductoServiceTest.java")));
        assertTrue(Files.exists(Path.of("src/test/java/com/foodfast/catalogo_servicio/controller/CategoriaControllerTest.java")));
        assertTrue(Files.exists(Path.of("src/test/java/com/foodfast/catalogo_servicio/controller/ProductoControllerTest.java")));
        assertTrue(Files.exists(Path.of("src/test/java/com/foodfast/catalogo_servicio/repository/CatalogoRepositoryTest.java")));
    }
}
