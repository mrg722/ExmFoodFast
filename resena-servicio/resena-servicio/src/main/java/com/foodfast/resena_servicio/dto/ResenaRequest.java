package com.foodfast.resena_servicio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
@Schema(description = "Datos necesarios para crear o actualizar una reseña")
public class ResenaRequest {

    @Schema(example = "1", description = "Identificador del cliente que escribe la reseña")
    @NotNull(message = "El clienteId es obligatorio")
    @Positive(message = "El clienteId debe ser mayor que cero")
    private Long clienteId;

    @Schema(example = "10", description = "Identificador del producto reseñado")
    @NotNull(message = "El productoId es obligatorio")
    @Positive(message = "El productoId debe ser mayor que cero")
    private Long productoId;

    @Schema(example = "2", description = "Identificador opcional del restaurante asociado")
    @Positive(message = "El restauranteId debe ser mayor que cero")
    private Long restauranteId;

    @Schema(example = "5", description = "Calificación entre 1 y 5")
    @NotNull(message = "La calificacion es obligatoria")
    @Min(value = 1, message = "La calificacion minima es 1")
    @Max(value = 5, message = "La calificacion maxima es 5")
    private Integer calificacion;

    @Schema(example = "Muy buen producto, llegó caliente y rápido", description = "Comentario visible para otros usuarios")
    @NotBlank(message = "El comentario es obligatorio")
    @Size(min = 3, max = 500, message = "El comentario debe tener entre 3 y 500 caracteres")
    private String comentario;
}
