package com.foodfast.reparto_servicio.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RepartidorResponse {
    private Long id;
    private String nombre;
    private String telefono;
    private String vehiculo;
    private Boolean activo;
    private Boolean disponible;
}