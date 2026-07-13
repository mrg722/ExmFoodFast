package com.foodfast.reparto_servicio.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(
            RecursoNoEncontradoException ex,
            HttpServletRequest request) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> manejarReglaNegocio(
            ReglaNegocioException ex,
            HttpServletRequest request) {
        log.warn("Regla de negocio incumplida: {}", ex.getMessage());
        return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(ServicioExternoException.class)
    public ResponseEntity<ErrorResponse> manejarServicioExterno(
            ServicioExternoException ex,
            HttpServletRequest request) {
        log.warn("Error en servicio externo: {}", ex.getMessage());
        return construirRespuesta(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidaciones(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage())
        );
        log.warn("Errores de validacion: {}", errores);
        return construirRespuesta(HttpStatus.BAD_REQUEST, "Error de validacion", request.getRequestURI(), errores);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> manejarIntegridadDatos(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {
        log.warn("Error de integridad de datos", ex);
        return construirRespuesta(HttpStatus.BAD_REQUEST,
                "La operacion no puede realizarse porque viola una restriccion de base de datos",
                request.getRequestURI(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarErrorGeneral(
            Exception ex,
            HttpServletRequest request) {
        log.error("Error inesperado en reparto-servicio", ex);
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor", request.getRequestURI(), null);
    }

    private ResponseEntity<ErrorResponse> construirRespuesta(
            HttpStatus status,
            String message,
            String path,
            Map<String, String> validations) {
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
