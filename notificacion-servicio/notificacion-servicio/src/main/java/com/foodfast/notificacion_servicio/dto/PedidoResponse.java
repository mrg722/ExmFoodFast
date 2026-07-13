package com.foodfast.notificacion_servicio.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
public class PedidoResponse {
    private Long id;
    private Long clienteId;
    private String estado;
    private BigDecimal total;
    private String direccionEntrega;
    private LocalDateTime fechaCreacion;
}
