package com.foodfast.catalogo_servicio.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.foodfast.catalogo_servicio.dto.InventarioStockResponse;
import com.foodfast.catalogo_servicio.exception.ServicioExternoException;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class InventarioClientTest {

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void consultarStock_debeRetornarRespuestaCuandoIntegracionActiva() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"exito\":true,\"mensaje\":\"ok\",\"datos\":{\"productoId\":1,\"cantidadDisponible\":10,\"cantidadReservada\":2,\"stockReal\":8,\"hayStock\":true}}"));

        InventarioClient client = new InventarioClient(WebClient.builder(), mockWebServer.url("/").toString(), true, 2);

        InventarioStockResponse resultado = client.consultarStock(1L);

        assertEquals(1L, resultado.getProductoId());
        assertEquals(8, resultado.getStockReal());
        assertTrue(resultado.getHayStock());
        assertTrue(mockWebServer.takeRequest().getPath().contains("/api/inventarios/stock/1"));
    }

    @Test
    void consultarStock_debeRetornarVacioCuandoIntegracionDesactivada() {
        InventarioClient client = new InventarioClient(WebClient.builder(), mockWebServer.url("/").toString(), false, 2);

        InventarioStockResponse resultado = client.consultarStock(5L);

        assertEquals(5L, resultado.getProductoId());
        assertNull(resultado.getStockReal());
        assertFalse(client.isIntegrationEnabled());
    }

    @Test
    void consultarStock_debeLanzarServicioExternoCuandoInventarioRespondeError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"mensaje\":\"error\"}"));

        InventarioClient client = new InventarioClient(WebClient.builder(), mockWebServer.url("/").toString(), true, 2);

        assertThrows(ServicioExternoException.class, () -> client.consultarStock(1L));
    }

    @Test
    void consultarStock_debeLanzarServicioExternoCuandoRespuestaNoTraeDatos() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"exito\":true,\"mensaje\":\"ok\",\"datos\":null}"));

        InventarioClient client = new InventarioClient(WebClient.builder(), mockWebServer.url("/").toString(), true, 2);

        assertThrows(ServicioExternoException.class, () -> client.consultarStock(1L));
    }
}
