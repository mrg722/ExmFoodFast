package com.foodfast.restaurante_servicio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionResponse {
    private Long id;
    private Long clienteId;
    private String tipo;
    private String canal;
    private String estado;
    private String titulo;
    private String mensaje;
}
