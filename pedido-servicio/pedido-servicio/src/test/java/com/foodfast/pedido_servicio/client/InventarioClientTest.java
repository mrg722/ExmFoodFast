package com.foodfast.pedido_servicio.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.foodfast.pedido_servicio.dto.InventarioResponse;
import com.foodfast.pedido_servicio.dto.InventarioStockResponse;
import com.foodfast.pedido_servicio.exception.ServicioExternoException;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class InventarioClientTest {

    private MockWebServer mockWebServer;
    private InventarioClient inventarioClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = mockWebServer.url("/").toString();
        inventarioClient = new InventarioClient(WebClient.builder(), baseUrl, 2L);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void consultarStockDebeLeerRespuestaApiResponse() throws Exception {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "success": true,
                          "message": "Stock encontrado",
                          "data": {
                            "productoId": 72,
                            "stockDisponible": 15,
                            "hayStock": true
                          }
                        }
                        """));

        // When
        InventarioStockResponse response = inventarioClient.consultarStock(72L);

        // Then
        assertThat(response.getProductoId()).isEqualTo(72L);
        assertThat(response.getStockDisponible()).isEqualTo(15);
        assertThat(response.getHayStock()).isTrue();
        assertThat(mockWebServer.takeRequest().getPath()).isEqualTo("/api/inventarios/stock/72");
    }

    @Test
    void descontarStockDebeEnviarPostConProductoYCantidad() throws Exception {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "success": true,
                          "message": "Stock descontado",
                          "data": {
                            "id": 5,
                            "productoId": 72,
                            "cantidadDisponible": 13,
                            "stockReal": 13,
                            "estadoStock": "DISPONIBLE"
                          }
                        }
                        """));

        // When
        InventarioResponse response = inventarioClient.descontarStock(72L, 2);

        // Then
        assertThat(response.getProductoId()).isEqualTo(72L);
        assertThat(response.getCantidadDisponible()).isEqualTo(13);
        assertThat(mockWebServer.takeRequest().getMethod()).isEqualTo("POST");
    }

    @Test
    void consultarStockDebeReenviarAuthorizationHeaderCuandoSeProporcione() throws Exception {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "success": true,
                          "message": "Stock encontrado",
                          "data": {
                            "productoId": 72,
                            "stockDisponible": 15,
                            "hayStock": true
                          }
                        }
                        """));

        // When
        inventarioClient.consultarStock(72L, "Bearer token-demo");

        // Then
        assertThat(mockWebServer.takeRequest().getHeader("Authorization")).isEqualTo("Bearer token-demo");
    }

    @Test
    void consultarStockConErrorHttpDebeLanzarServicioExternoException() {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"message\":\"Error inventario\"}"));

        // When / Then
        assertThatThrownBy(() -> inventarioClient.consultarStock(72L))
                .isInstanceOf(ServicioExternoException.class)
                .hasMessageContaining("Inventario respondió con error HTTP");
    }

    @Test
    void descontarStockSinDataDebeLanzarServicioExternoException() {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"success\":true,\"message\":\"sin data\",\"data\":null}"));

        // When / Then
        assertThatThrownBy(() -> inventarioClient.descontarStock(72L, 2))
                .isInstanceOf(ServicioExternoException.class)
                .hasMessageContaining("Inventario no devolvió datos");
    }
}
