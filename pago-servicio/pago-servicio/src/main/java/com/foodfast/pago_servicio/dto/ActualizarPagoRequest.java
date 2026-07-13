package com.foodfast.pago_servicio.dto;

import com.foodfast.pago_servicio.model.MetodoPago;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Schema(description = "Datos permitidos para actualizar un pago pendiente")
public class ActualizarPagoRequest {

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "1.0", message = "El monto debe ser mayor que cero")
    @Schema(example = "17990")
    private BigDecimal monto;

    @NotNull(message = "El método de pago es obligatorio")
    @Schema(example = "TARJETA")
    private MetodoPago metodoPago;
}
