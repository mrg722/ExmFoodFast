package com.foodfast.reparto_servicio.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.foodfast.reparto_servicio.dto.PedidoResponse;
import com.foodfast.reparto_servicio.exception.ServicioExternoException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class PedidoClientTest {

    private MockWebServer mockWebServer;
    private PedidoClient pedidoClient;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = mockWebServer.url("/").toString();
        pedidoClient = new PedidoClient(WebClient.builder().baseUrl(baseUrl).build(), 2L);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void obtenerPedidoDebeRetornarDataCuandoServicioRespondeOk() throws Exception {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "success": true,
                          "message": "Pedido encontrado",
                          "data": {
                            "id": 10,
                            "clienteId": 1,
                            "productoId": 5,
                            "cantidad": 2,
                            "estado": "CONFIRMADO"
                          }
                        }
                        """));

        // When
        PedidoResponse response = pedidoClient.obtenerPedido(10L);

        // Then
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getEstado()).isEqualTo("CONFIRMADO");
        assertThat(mockWebServer.takeRequest().getPath()).isEqualTo("/api/pedidos/10");
    }

    @Test
    void obtenerPedidoDebeLanzarErrorSiServicioResponde404() {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"message\":\"No existe\"}"));

        // When / Then
        assertThatThrownBy(() -> pedidoClient.obtenerPedido(99L))
                .isInstanceOf(ServicioExternoException.class)
                .hasMessageContaining("HTTP 404");
    }

    @Test
    void obtenerPedidoDebeLanzarErrorSiNoVieneData() {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"success\":true,\"message\":\"sin data\",\"data\":null}"));

        // When / Then
        assertThatThrownBy(() -> pedidoClient.obtenerPedido(10L))
                .isInstanceOf(ServicioExternoException.class)
                .hasMessageContaining("no devolvió datos");
    }
}
