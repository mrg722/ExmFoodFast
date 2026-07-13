package com.foodfast.reparto_servicio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EntregaRequest {

    @NotNull(message = "El pedidoId es obligatorio")
    @Positive(message = "El pedidoId debe ser mayor que cero")
    private Long pedidoId;

    @Positive(message = "El repartidorId debe ser mayor que cero")
    private Long repartidorId;

    @NotBlank(message = "La direccion de entrega es obligatoria")
    @Size(max = 180, message = "La direccion de entrega no puede superar 180 caracteres")
    private String direccionEntrega;
}
