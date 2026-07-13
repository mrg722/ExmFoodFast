package com.foodfast.pedido_servicio.exception;

import com.foodfast.pedido_servicio.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidaciones(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> validaciones = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                validaciones.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse response = construirRespuesta(
                HttpStatus.BAD_REQUEST,
                "Error de validación",
                request.getRequestURI()
        );
        response.setValidaciones(validaciones);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> manejarReglaNegocio(
            ReglaNegocioException ex,
            HttpServletRequest request) {

        log.warn("Regla de negocio incumplida: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI())
        );
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(
            RecursoNoEncontradoException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI())
        );
    }

    @ExceptionHandler(ServicioExternoException.class)
    public ResponseEntity<ErrorResponse> manejarServicioExterno(
            ServicioExternoException ex,
            HttpServletRequest request) {

        log.error("Error de comunicación entre microservicios: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                construirRespuesta(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request.getRequestURI())
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> manejarRutaNoEncontrada(
            NoResourceFoundException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                construirRespuesta(HttpStatus.NOT_FOUND, "Ruta no encontrada", request.getRequestURI())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarErrorGeneral(
            Exception ex,
            HttpServletRequest request) {

        log.error("Error interno en ruta={}", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", request.getRequestURI())
        );
    }

    private ErrorResponse construirRespuesta(HttpStatus status, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();
    }
}
