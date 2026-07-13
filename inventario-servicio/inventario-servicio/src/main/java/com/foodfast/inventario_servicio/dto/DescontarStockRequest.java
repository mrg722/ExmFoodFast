package com.foodfast.inventario_servicio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud para descontar stock de un producto")
public class DescontarStockRequest {

    @NotNull(message = "El productoId es obligatorio")
    @Schema(example = "1", description = "ID del producto al que se descuenta stock")
    private Long productoId;

    @NotNull(message = "La cantidad a descontar es obligatoria")
    @Min(value = 1, message = "La cantidad a descontar debe ser mayor que cero")
    @Schema(example = "2", description = "Cantidad de unidades a descontar")
    private Integer cantidad;
}
