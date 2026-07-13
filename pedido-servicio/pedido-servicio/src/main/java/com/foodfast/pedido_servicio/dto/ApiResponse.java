package com.foodfast.pedido_servicio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Respuesta estándar utilizada por el microservicio")
public class ApiResponse<T> {

    @Schema(example = "true")
    private Boolean success;

    @Schema(example = "Operación realizada correctamente")
    private String message;

    private T data;
}
