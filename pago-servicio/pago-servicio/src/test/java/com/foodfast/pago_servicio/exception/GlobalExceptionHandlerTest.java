package com.foodfast.pago_servicio.exception;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final HttpServletRequest request = new MockHttpServletRequest("GET", "/api/pagos/1");

    @Test
    void manejarNoEncontradoDebeRetornar404() {
        ResponseEntity<ErrorResponse> response = handler.manejarNoEncontrado(new RecursoNoEncontradoException("no existe"), request);
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("no existe");
    }

    @Test
    void manejarReglaNegocioDebeRetornar400() {
        ResponseEntity<ErrorResponse> response = handler.manejarReglaNegocio(new ReglaNegocioException("regla"), request);
        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void manejarServicioExternoDebeRetornar503() {
        ResponseEntity<ErrorResponse> response = handler.manejarServicioExterno(new ServicioExternoException("externo"), request);
        assertThat(response.getStatusCode().value()).isEqualTo(503);
    }

    @Test
    void manejarErrorGeneralDebeRetornar500() {
        ResponseEntity<ErrorResponse> response = handler.manejarErrorGeneral(new RuntimeException("x"), request);
        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().getPath()).isEqualTo("/api/pagos/1");
    }
}
