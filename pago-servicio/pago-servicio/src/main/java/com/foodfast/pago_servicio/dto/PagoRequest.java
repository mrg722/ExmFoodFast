package com.foodfast.pago_servicio.dto;

import com.foodfast.pago_servicio.model.MetodoPago;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Schema(description = "Datos necesarios para registrar un pago pendiente")
public class PagoRequest {

    @NotNull(message = "El pedidoId es obligatorio")
    @Schema(example = "1", description = "Identificador del pedido asociado al pago")
    private Long pedidoId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "1.0", message = "El monto debe ser mayor que cero")
    @Schema(example = "15990", description = "Monto total del pago")
    private BigDecimal monto;

    @NotNull(message = "El método de pago es obligatorio")
    @Schema(example = "WEBPAY_SIMULADO", description = "Método utilizado por el cliente")
    private MetodoPago metodoPago;
}
