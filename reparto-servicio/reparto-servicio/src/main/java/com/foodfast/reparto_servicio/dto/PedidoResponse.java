package com.foodfast.reparto_servicio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponse {
    private Long id;
    private Long clienteId;
    private Long productoId;
    private Integer cantidad;
    private String estado;
}
