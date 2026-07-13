package com.foodfast.cliente_servicio.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_debeRetornar404() {
        var response = handler.handleNotFound(new RecursoNoEncontradoException("No existe"), request());

        assertEquals(404, response.getStatusCode().value());
        assertEquals("No existe", response.getBody().getMessage());
        assertEquals("/api/clientes/99", response.getBody().getPath());
    }

    @Test
    void handleBusiness_debeRetornar400() {
        var response = handler.handleBusiness(new ReglaNegocioException("Regla invalida"), request());

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Regla invalida", response.getBody().getMessage());
    }

    @Test
    void handleDataIntegrity_debeRetornar400() {
        var response = handler.handleDataIntegrity(new DataIntegrityViolationException("duplicado"), request());

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleServicioExterno_debeRetornar502() {
        var response = handler.handleServicioExterno(new ServicioExternoException("pedido caido"), request());

        assertEquals(502, response.getStatusCode().value());
        assertEquals("pedido caido", response.getBody().getMessage());
    }

    @Test
    void handleGeneral_debeRetornar500() {
        var response = handler.handleGeneral(new RuntimeException("error"), request());

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Error interno del servidor", response.getBody().getMessage());
    }

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/clientes/99");
        return request;
    }
}
