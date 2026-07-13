package com.foodfast.catalogo_servicio.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductoRequest {

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres")
    private String nombre;

    @NotBlank(message = "La descripción del producto es obligatoria")
    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero")
    private BigDecimal precio;

    @NotNull(message = "El estado disponible es obligatorio")
    private Boolean disponible;

    @NotNull(message = "El categoriaId es obligatorio")
    private Long categoriaId;
}
