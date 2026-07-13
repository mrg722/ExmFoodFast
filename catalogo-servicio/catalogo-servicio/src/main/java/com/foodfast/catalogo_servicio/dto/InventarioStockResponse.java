package com.foodfast.catalogo_servicio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventarioStockResponse {
    private Long productoId;
    private Integer cantidadDisponible;
    private Integer cantidadReservada;
    private Integer stockReal;
    private Boolean hayStock;
}
