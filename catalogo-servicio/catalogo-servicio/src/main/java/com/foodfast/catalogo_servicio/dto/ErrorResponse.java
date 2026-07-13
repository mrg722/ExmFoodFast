package com.foodfast.catalogo_servicio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime fecha;
    private int estado;
    private String error;
    private String mensaje;
    private String ruta;
    private Map<String, String> validaciones;
}
