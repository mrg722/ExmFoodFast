package com.foodfast.cliente_servicio.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoResumenResponse {

    private Long id;
    private Long clienteId;
    private Long productoId;
    private Integer cantidad;
    private LocalDateTime fechaCreacion;
    private String estado;
    private BigDecimal total;
    private String direccionEntrega;
    private String observacion;
    private String estadoStock;
    private String mensajeInventario;
}