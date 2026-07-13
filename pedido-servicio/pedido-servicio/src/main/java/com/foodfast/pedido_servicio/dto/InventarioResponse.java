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
@Schema(description = "Respuesta recibida desde inventario-servicio al descontar stock")
public class InventarioResponse {

    private Long id;
    private Long productoId;
    private Integer cantidadDisponible;
    private Integer cantidadReservada;
    private Integer stockReal;
    private String ubicacion;
    private String estadoStock;
}
