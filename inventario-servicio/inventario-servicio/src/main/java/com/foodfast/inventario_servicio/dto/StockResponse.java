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
@Schema(description = "Respuesta de consulta de stock")
public class StockResponse {
    @Schema(example = "1")
    private Long productoId;

    @Schema(example = "45")
    private Integer stockDisponible;

    @Schema(example = "true")
    private Boolean hayStock;

    public Integer getStockReal() {
        return stockDisponible;
    }

    public Integer getStock() {
        return stockDisponible;
    }

    public Integer getCantidadDisponible() {
        return stockDisponible;
    }

    public Boolean getDisponible() {
        return hayStock;
    }
}
