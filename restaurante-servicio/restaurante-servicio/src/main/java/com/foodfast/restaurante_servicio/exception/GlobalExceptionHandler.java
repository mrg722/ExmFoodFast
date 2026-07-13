package com.foodfast.restaurante_servicio.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(RecursoNoEncontradoException ex, HttpServletRequest request) {
        return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> manejarReglaNegocio(ReglaNegocioException ex, HttpServletRequest request) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(ServicioExternoException.class)
    public ResponseEntity<ErrorResponse> manejarServicioExterno(ServicioExternoException ex, HttpServletRequest request) {
        return construirRespuesta(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidaciones(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));
        return construirRespuesta(HttpStatus.BAD_REQUEST, "Error de validación", request.getRequestURI(), errores);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarErrorGeneral(Exception ex, HttpServletRequest request) {
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servicio", request.getRequestURI(), null);
    }

    private ResponseEntity<ErrorResponse> construirRespuesta(HttpStatus status, String message, String path, Map<String, String> validations) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .validations(validations)
                .build();
        return ResponseEntity.status(status).body(response);
    }
}
