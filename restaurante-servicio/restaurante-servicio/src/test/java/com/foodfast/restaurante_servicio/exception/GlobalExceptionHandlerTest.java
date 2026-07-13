package com.foodfast.restaurante_servicio.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void debeManejarNoEncontrado() {
        HttpServletRequest request = request("/api/restaurantes/99");

        ResponseEntity<ErrorResponse> response = handler.manejarNoEncontrado(new RecursoNoEncontradoException("No existe"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("No existe");
    }

    @Test
    void debeManejarReglaNegocio() {
        ResponseEntity<ErrorResponse> response = handler.manejarReglaNegocio(new ReglaNegocioException("Regla inválida"), request("/api/restaurantes"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void debeManejarServicioExterno() {
        ResponseEntity<ErrorResponse> response = handler.manejarServicioExterno(new ServicioExternoException("Error remoto"), request("/api/restaurantes"));

        assertThat(response.getStatusCode().value()).isEqualTo(503);
    }

    @Test
    void debeManejarErrorGeneral() {
        ResponseEntity<ErrorResponse> response = handler.manejarErrorGeneral(new RuntimeException("boom"), request("/api/restaurantes"));

        assertThat(response.getStatusCode().value()).isEqualTo(500);
    }

    @Test
    void debeManejarValidaciones() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "restauranteRequest");
        bindingResult.addError(new FieldError("restauranteRequest", "nombre", "El nombre es obligatorio"));
        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("metodoDummy", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.manejarValidaciones(exception, request("/api/restaurantes"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getValidations()).containsKey("nombre");
    }

    @SuppressWarnings("unused")
    private void metodoDummy(String valor) {
    }

    private HttpServletRequest request(String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(uri);
        return request;
    }
}
