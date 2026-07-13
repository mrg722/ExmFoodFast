package com.foodfast.cliente_servicio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DireccionRequest {

    @NotBlank(message = "La calle es obligatoria")
    private String calle;

    @NotBlank(message = "El numero es obligatorio")
    private String numero;

    @NotBlank(message = "La comuna es obligatoria")
    private String comuna;

    @NotBlank(message = "La ciudad es obligatoria")
    private String ciudad;

    private String referencia;

    @NotNull(message = "Debe indicar si la direccion es principal")
    private Boolean principal;
}
