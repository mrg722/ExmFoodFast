package com.foodfast.reparto_servicio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RepartidorRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
    private String nombre;

    @NotBlank(message = "El telefono es obligatorio")
    @Size(max = 30, message = "El telefono no puede superar 30 caracteres")
    private String telefono;

    @NotBlank(message = "El vehiculo es obligatorio")
    @Size(max = 50, message = "El vehiculo no puede superar 50 caracteres")
    private String vehiculo;

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;

    @NotNull(message = "La disponibilidad es obligatoria")
    private Boolean disponible;
}
