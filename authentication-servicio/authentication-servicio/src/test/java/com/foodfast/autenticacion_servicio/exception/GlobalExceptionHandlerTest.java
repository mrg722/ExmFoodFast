package com.foodfast.autenticacion_servicio.exception;

import com.foodfast.autenticacion_servicio.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/auth/test");
    }

    @Test
    void debeResponder404ParaRecursoNoEncontrado() {
        ResponseEntity<ErrorResponse> response = handler.manejarNoEncontrado(
                new RecursoNoEncontradoException("no existe"), request);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("no existe", response.getBody().getMessage());
        assertEquals("/api/auth/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void debeResponder400ParaReglaNegocio() {
        ResponseEntity<ErrorResponse> response = handler.manejarReglaNegocio(
                new ReglaNegocioException("email repetido"), request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("email repetido", response.getBody().getMessage());
    }

    @Test
    void debeResponder401ParaCredencialesInvalidas() {
        ResponseEntity<ErrorResponse> response = handler.manejarCredenciales(
                new BadCredentialsException("bad"), request);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Email o contraseña incorrectos", response.getBody().getMessage());
    }

    @Test
    void debeResponder500ParaErrorGeneral() {
        ResponseEntity<ErrorResponse> response = handler.manejarGeneral(
                new RuntimeException("boom"), request);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Error interno del servidor", response.getBody().getMessage());
    }
}
