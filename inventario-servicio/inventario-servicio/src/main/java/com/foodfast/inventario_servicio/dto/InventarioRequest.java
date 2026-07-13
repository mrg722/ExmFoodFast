package com.foodfast.inventario_servicio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Datos para crear o actualizar inventario")
public class InventarioRequest {

    @NotNull(message = "El productoId es obligatorio")
    @Schema(example = "1", description = "ID del producto asociado al inventario")
    private Long productoId;

    @NotNull(message = "La cantidad disponible es obligatoria")
    @Min(value = 0, message = "La cantidad disponible no puede ser negativa")
    @Schema(example = "50", description = "Stock físico disponible")
    private Integer cantidadDisponible;

    @NotNull(message = "La cantidad reservada es obligatoria")
    @Min(value = 0, message = "La cantidad reservada no puede ser negativa")
    @Schema(example = "5", description = "Stock reservado por pedidos en curso")
    private Integer cantidadReservada;

    @NotBlank(message = "La ubicación es obligatoria")
    @Size(max = 120, message = "La ubicación no puede superar 120 caracteres")
    @Schema(example = "Bodega Central A1", description = "Ubicación física del stock")
    private String ubicacion;
}
