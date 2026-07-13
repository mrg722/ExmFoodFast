package com.foodfast.inventario_servicio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Respuesta con el estado del inventario")
public class InventarioResponse {
    @Schema(example = "1")
    private Long id;

    @Schema(example = "1")
    private Long productoId;

    @Schema(example = "50")
    private Integer cantidadDisponible;

    @Schema(example = "5")
    private Integer cantidadReservada;

    @Schema(example = "45")
    private Integer stockReal;

    @Schema(example = "Bodega Central A1")
    private String ubicacion;
}
