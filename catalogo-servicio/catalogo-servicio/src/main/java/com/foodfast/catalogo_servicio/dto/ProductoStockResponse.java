package com.foodfast.catalogo_servicio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductoStockResponse {
    private ProductoResponse producto;
    private InventarioStockResponse stock;
    private String mensajeInventario;
}
