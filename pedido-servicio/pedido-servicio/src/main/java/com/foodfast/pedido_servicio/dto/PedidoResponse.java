package com.foodfast.pedido_servicio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Respuesta con la información del pedido")
public class PedidoResponse {
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
