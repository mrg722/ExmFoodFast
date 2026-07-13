package com.foodfast.resena_servicio.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.foodfast.resena_servicio.dto.ProductoResponse;
import com.foodfast.resena_servicio.exception.ServicioExternoException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class CatalogoClientTest {

    private MockWebServer mockWebServer;
    private CatalogoClient catalogoClient;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        catalogoClient = new CatalogoClient(WebClient.builder(), mockWebServer.url("/").toString(), 2L);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void debeBuscarProductoCorrectamente() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "success": true,
                          "message": "Producto encontrado",
                          "data": {
                            "id": 10,
                            "nombre": "Hamburguesa",
                            "descripcion": "Producto de prueba",
                            "activo": true
                          }
                        }
                        """));

        ProductoResponse response = catalogoClient.buscarProducto(10L);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getActivo()).isTrue();
        assertThat(mockWebServer.takeRequest().getPath()).isEqualTo("/api/productos/10");
    }

    @Test
    void debeLanzarServicioExternoSiCatalogoRespondeError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"message\":\"No existe\"}"));

        assertThatThrownBy(() -> catalogoClient.buscarProducto(99L))
                .isInstanceOf(ServicioExternoException.class)
                .hasMessageContaining("HTTP 404");
    }

    @Test
    void debeLanzarServicioExternoSiNoHayData() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"success\":true,\"message\":\"ok\",\"data\":null}"));

        assertThatThrownBy(() -> catalogoClient.buscarProducto(10L))
                .isInstanceOf(ServicioExternoException.class)
                .hasMessageContaining("no devolvió datos");
    }
}
