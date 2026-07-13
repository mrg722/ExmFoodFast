package com.foodfast.pedido_servicio.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.foodfast.pedido_servicio.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/pedidos/1");
    }

    @Test
    void manejarReglaNegocioDebeResponderBadRequest() {
        ResponseEntity<ErrorResponse> response = handler.manejarReglaNegocio(
                new ReglaNegocioException("Regla incumplida"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Regla incumplida");
        assertThat(response.getBody().getPath()).isEqualTo("/api/pedidos/1");
    }

    @Test
    void manejarNoEncontradoDebeResponderNotFound() {
        ResponseEntity<ErrorResponse> response = handler.manejarNoEncontrado(
                new RecursoNoEncontradoException("No encontrado"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("No encontrado");
    }

    @Test
    void manejarServicioExternoDebeResponderServiceUnavailable() {
        ResponseEntity<ErrorResponse> response = handler.manejarServicioExterno(
                new ServicioExternoException("Inventario caído"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(503);
        assertThat(response.getBody().getMessage()).isEqualTo("Inventario caído");
    }

    @Test
    void manejarErrorGeneralDebeResponderInternalServerError() {
        ResponseEntity<ErrorResponse> response = handler.manejarErrorGeneral(
                new RuntimeException("boom"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("Error interno del servidor");
    }
}
