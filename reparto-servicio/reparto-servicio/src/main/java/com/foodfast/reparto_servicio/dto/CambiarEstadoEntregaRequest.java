package com.foodfast.reparto_servicio.dto;

import com.foodfast.reparto_servicio.model.EstadoEntrega;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CambiarEstadoEntregaRequest {

    @NotNull(message = "El estado de entrega es obligatorio")
    private EstadoEntrega estadoEntrega;
}
