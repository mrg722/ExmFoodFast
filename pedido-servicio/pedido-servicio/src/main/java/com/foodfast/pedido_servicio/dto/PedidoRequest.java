package com.foodfast.pedido_servicio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "Datos necesarios para crear o actualizar un pedido")
public class PedidoRequest {

    @NotNull(message = "El clienteId es obligatorio")
    @Positive(message = "El clienteId debe ser mayor a cero")
    @Schema(description = "Identificador del cliente que realiza el pedido", example = "1")
    private Long clienteId;

    @Positive(message = "El productoId debe ser mayor a cero")
    @Schema(description = "Producto solicitado. Si se informa, también debe enviarse cantidad", example = "72")
    private Long productoId;

    @Min(value = 1, message = "La cantidad debe ser mayor a cero")
    @Schema(description = "Cantidad solicitada. Si se informa, también debe enviarse productoId", example = "2")
    private Integer cantidad;

    @NotNull(message = "El total es obligatorio")
    @DecimalMin(value = "0.01", message = "El total debe ser mayor a cero")
    @Schema(description = "Monto total del pedido", example = "15990")
    private BigDecimal total;

    @NotBlank(message = "La dirección de entrega es obligatoria")
    @Size(max = 150, message = "La dirección de entrega no puede superar 150 caracteres")
    @Schema(description = "Dirección donde se entregará el pedido", example = "Av. FoodFast 123")
    private String direccionEntrega;

    @Size(max = 255, message = "La observación no puede superar 255 caracteres")
    @Schema(description = "Observación opcional para cocina o reparto", example = "Sin cebolla")
    private String observacion;
}
