package com.foodfast.restaurante_servicio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RestauranteRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
    private String descripcion;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 180, message = "La dirección no puede superar 180 caracteres")
    private String direccion;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
    private String telefono;

    @Email(message = "El email debe tener formato válido")
    @Size(max = 120, message = "El email no puede superar 120 caracteres")
    private String email;

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;

    @NotNull(message = "El estado abierto es obligatorio")
    private Boolean abierto;
}
