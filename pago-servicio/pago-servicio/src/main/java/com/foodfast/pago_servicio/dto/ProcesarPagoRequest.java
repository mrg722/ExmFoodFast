package com.foodfast.pago_servicio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Solicitud para procesar un pago pendiente")
public class ProcesarPagoRequest {

    @NotNull(message = "El pagoId es obligatorio")
    @Schema(example = "1")
    private Long pagoId;

    @NotNull(message = "Debe indicar si el pago fue aprobado")
    @Schema(example = "true")
    private Boolean aprobado;
}
