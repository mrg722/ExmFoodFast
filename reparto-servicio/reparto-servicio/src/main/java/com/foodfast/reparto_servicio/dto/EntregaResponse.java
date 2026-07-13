package com.foodfast.reparto_servicio.dto;

import com.foodfast.reparto_servicio.model.EstadoEntrega;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EntregaResponse {
    private Long id;
    private Long pedidoId;
    private Long repartidorId;
    private String repartidorNombre;
    private String direccionEntrega;
    private EstadoEntrega estadoEntrega;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
