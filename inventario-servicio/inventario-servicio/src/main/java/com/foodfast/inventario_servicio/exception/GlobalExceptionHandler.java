package com.foodfast.inventario_servicio.exception;

import com.foodfast.inventario_servicio.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(
            RecursoNoEncontradoException ex,
            HttpServletRequest request
    ) {
        log.warn("Recurso no encontrado: {} ruta={}", ex.getMessage(), request.getRequestURI());
        return construirError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler({ReglaNegocioException.class, StockInsuficienteException.class})
    public ResponseEntity<ErrorResponse> manejarReglaNegocio(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        log.warn("Regla de negocio incumplida: {} ruta={}", ex.getMessage(), request.getRequestURI());
        return construirError(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidaciones(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> validaciones = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                validaciones.put(error.getField(), error.getDefaultMessage())
        );
        log.warn("Validación fallida ruta={} errores={}", request.getRequestURI(), validaciones);
        return construirError(HttpStatus.BAD_REQUEST, "Error de validación", request.getRequestURI(), validaciones);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> manejarIntegridad(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        log.warn("Error de integridad de datos ruta={} detalle={}", request.getRequestURI(), ex.getMessage());
        return construirError(HttpStatus.CONFLICT, "Existe un productoId duplicado o una restricción inválida", request.getRequestURI(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarGeneral(Exception ex, HttpServletRequest request) {
        log.error("Error interno ruta={}", request.getRequestURI(), ex);
        return construirError(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", request.getRequestURI(), null);
    }

    private ResponseEntity<ErrorResponse> construirError(
            HttpStatus estado,
            String mensaje,
            String ruta,
            Map<String, String> validaciones
    ) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                estado.value(),
                estado.getReasonPhrase(),
                mensaje,
                ruta,
                validaciones
        );
        return ResponseEntity.status(estado).body(error);
    }
}
