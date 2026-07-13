package com.foodfast.pedido_servicio.dto;

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
@Schema(description = "Respuesta recibida desde inventario-servicio al consultar stock")
public class InventarioStockResponse {

    @Schema(example = "72")
    private Long productoId;

    @Schema(example = "15")
    private Integer stockDisponible;

    @Schema(example = "15")
    private Integer stock;

    @Schema(example = "15")
    private Integer stockReal;

    @Schema(example = "15")
    private Integer cantidadDisponible;

    @Schema(example = "true")
    private Boolean hayStock;

    @Schema(example = "true")
    private Boolean disponible;
}
