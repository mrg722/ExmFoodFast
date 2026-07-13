package com.foodfast.notificacion_servicio.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/notificaciones/1");
    }

    @Test
    void debeManejarNotFound() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(new RecursoNoEncontradoException("No existe"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("No existe");
        assertThat(response.getBody().getPath()).isEqualTo("/api/notificaciones/1");
    }

    @Test
    void debeManejarReglaNegocio() {
        ResponseEntity<ErrorResponse> response = handler.handleBusiness(new ReglaNegocioException("Regla inválida"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Regla inválida");
    }

    @Test
    void debeManejarServicioExterno() {
        ResponseEntity<ErrorResponse> response = handler.handleExternal(new ServicioExternoException("Pedido caído"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(503);
        assertThat(response.getBody().getMessage()).isEqualTo("Pedido caído");
    }

    @Test
    void debeManejarErrorGeneral() {
        ResponseEntity<ErrorResponse> response = handler.handleGeneral(new RuntimeException("boom"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("Error interno del servidor");
    }
}
