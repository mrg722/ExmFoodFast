package com.foodfast.catalogo_servicio.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.foodfast.catalogo_servicio.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/catalogo/test");
    }

    @Test
    void manejarNoEncontrado_debeRetornar404() {
        ResponseEntity<ErrorResponse> response = handler.manejarNoEncontrado(new RecursoNoEncontradoException("No existe"), request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No existe", response.getBody().getMensaje());
    }

    @Test
    void manejarReglaNegocio_debeRetornar400() {
        ResponseEntity<ErrorResponse> response = handler.manejarReglaNegocio(new ReglaNegocioException("Regla inválida"), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Regla inválida", response.getBody().getMensaje());
    }

    @Test
    void manejarServicioExterno_debeRetornar503() {
        ResponseEntity<ErrorResponse> response = handler.manejarServicioExterno(new ServicioExternoException("Inventario caído"), request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("Inventario caído", response.getBody().getMensaje());
    }

    @Test
    void manejarIntegridad_debeRetornar409() {
        ResponseEntity<ErrorResponse> response = handler.manejarIntegridad(new DataIntegrityViolationException("duplicado"), request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody().getFecha());
    }

    @Test
    void manejarGeneral_debeRetornar500() {
        ResponseEntity<ErrorResponse> response = handler.manejarGeneral(new RuntimeException("error"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error interno del servidor", response.getBody().getMensaje());
    }
}
