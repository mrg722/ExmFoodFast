package com.foodfast.restaurante_servicio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionRequest {
    private Long clienteId;
    private String tipo;
    private String canal;
    private String titulo;
    private String mensaje;
    private String referenciaTipo;
    private Long referenciaId;
}
