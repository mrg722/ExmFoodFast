package com.foodfast.cliente_servicio.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.foodfast.cliente_servicio.exception.ServicioExternoException;
import java.io.IOException;
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
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();
        pedidoClient = new PedidoClient(webClient, 2L);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void listarPedidosPorCliente_debeRetornarDatosCuandoPedidoServicioRespondeOk() throws Exception {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "success": true,
                          "message": "ok",
                          "data": [
                            {"id": 10, "clienteId": 1, "estado": "CONFIRMADO", "total": 15990}
                          ]
                        }
                        """));

        // When
        var pedidos = pedidoClient.listarPedidosPorCliente(1L, "Bearer token");

        // Then
        assertEquals(1, pedidos.size());
        assertEquals(10L, pedidos.get(0).getId());
        assertEquals("Bearer token", mockWebServer.takeRequest().getHeader("Authorization"));
    }

    @Test
    void listarPedidosPorCliente_debeRetornarListaVaciaCuandoDataEsNull() {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"success\":true,\"message\":\"ok\",\"data\":null}"));

        // When
        var pedidos = pedidoClient.listarPedidosPorCliente(1L, null);

        // Then
        assertTrue(pedidos.isEmpty());
    }

    @Test
    void listarPedidosPorCliente_debeLanzarServicioExternoCuandoStatusEsError() {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json")
                .setBody("error interno"));

        // When / Then
        assertThrows(ServicioExternoException.class,
                () -> pedidoClient.listarPedidosPorCliente(1L, "Bearer token"));
    }
}
