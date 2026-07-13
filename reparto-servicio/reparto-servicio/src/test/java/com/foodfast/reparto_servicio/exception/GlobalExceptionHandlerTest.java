package com.foodfast.reparto_servicio.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void manejarNoEncontradoDebeResponder404() {
        HttpServletRequest request = request("/api/repartidores/99");

        ResponseEntity<ErrorResponse> response = handler.manejarNoEncontrado(
                new RecursoNoEncontradoException("No encontrado"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("No encontrado");
    }

    @Test
    void manejarReglaNegocioDebeResponder400() {
        ResponseEntity<ErrorResponse> response = handler.manejarReglaNegocio(
                new ReglaNegocioException("Regla invalida"), request("/api/entregas"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Regla invalida");
    }

    @Test
    void manejarServicioExternoDebeResponder503() {
        ResponseEntity<ErrorResponse> response = handler.manejarServicioExterno(
                new ServicioExternoException("pedido caido"), request("/api/entregas"));

        assertThat(response.getStatusCode().value()).isEqualTo(503);
        assertThat(response.getBody().getMessage()).isEqualTo("pedido caido");
    }

    @Test
    void manejarIntegridadDebeResponder400() {
        ResponseEntity<ErrorResponse> response = handler.manejarIntegridadDatos(
                new DataIntegrityViolationException("duplicado"), request("/api/entregas"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).contains("restriccion");
    }

    @Test
    void manejarGeneralDebeResponder500() {
        ResponseEntity<ErrorResponse> response = handler.manejarErrorGeneral(
                new RuntimeException("boom"), request("/api/entregas"));

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("Error interno del servidor");
    }

    private HttpServletRequest request(String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(uri);
        return request;
    }
}
