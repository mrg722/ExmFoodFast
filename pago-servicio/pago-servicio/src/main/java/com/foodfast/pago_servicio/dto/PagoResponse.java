package com.foodfast.pago_servicio.dto;

import com.foodfast.pago_servicio.model.EstadoPago;
import com.foodfast.pago_servicio.model.MetodoPago;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PagoResponse {
    private Long id;
    private Long pedidoId;
    private BigDecimal monto;
    private MetodoPago metodoPago;
    private EstadoPago estadoPago;
    private String codigoTransaccion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaProcesamiento;
    private String mensajePedido;
}
