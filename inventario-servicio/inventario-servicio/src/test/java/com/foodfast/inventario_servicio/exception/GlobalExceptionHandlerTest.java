package com.foodfast.inventario_servicio.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.foodfast.inventario_servicio.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/inventarios/1");
    }

    @Test
    void debeManejarRecursoNoEncontradoCon404() {
        ResponseEntity<ErrorResponse> respuesta = handler.manejarNoEncontrado(
                new RecursoNoEncontradoException("Inventario no encontrado"),
                request
        );

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().getMensaje()).isEqualTo("Inventario no encontrado");
        assertThat(respuesta.getBody().getRuta()).isEqualTo("/api/inventarios/1");
    }

    @Test
    void debeManejarReglaNegocioCon400() {
        ResponseEntity<ErrorResponse> respuesta = handler.manejarReglaNegocio(
                new ReglaNegocioException("Stock inválido"),
                request
        );

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().getMensaje()).isEqualTo("Stock inválido");
    }

    @Test
    void debeManejarStockInsuficienteCon400() {
        ResponseEntity<ErrorResponse> respuesta = handler.manejarReglaNegocio(
                new StockInsuficienteException("Stock insuficiente"),
                request
        );

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().getMensaje()).contains("Stock insuficiente");
    }

    @Test
    void debeManejarIntegridadCon409() {
        ResponseEntity<ErrorResponse> respuesta = handler.manejarIntegridad(
                new DataIntegrityViolationException("duplicate"),
                request
        );

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().getMensaje()).contains("productoId");
    }

    @Test
    void debeManejarErrorGeneralCon500() {
        ResponseEntity<ErrorResponse> respuesta = handler.manejarGeneral(
                new RuntimeException("boom"),
                request
        );

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().getMensaje()).isEqualTo("Error interno del servidor");
    }
}
