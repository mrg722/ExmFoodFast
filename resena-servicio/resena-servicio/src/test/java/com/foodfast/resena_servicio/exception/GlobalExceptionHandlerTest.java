package com.foodfast.resena_servicio.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/resenas/test");
    }

    @Test
    void debeManejarNotFoundBusinessExternalYGeneral() {
        ResponseEntity<ErrorResponse> notFound = handler.handleNotFound(new RecursoNoEncontradoException("No encontrado"), request);
        ResponseEntity<ErrorResponse> business = handler.handleBusiness(new ReglaNegocioException("Regla"), request);
        ResponseEntity<ErrorResponse> external = handler.handleExternal(new ServicioExternoException("Servicio caido"), request);
        ResponseEntity<ErrorResponse> general = handler.handleGeneral(new RuntimeException("Boom"), request);

        assertThat(notFound.getStatusCode().value()).isEqualTo(404);
        assertThat(business.getStatusCode().value()).isEqualTo(400);
        assertThat(external.getStatusCode().value()).isEqualTo(503);
        assertThat(general.getStatusCode().value()).isEqualTo(500);
        assertThat(notFound.getBody().getPath()).isEqualTo("/api/resenas/test");
    }
}
