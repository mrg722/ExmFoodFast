package com.foodfast.cliente_servicio.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene formato valido")
    private String email;

    @NotBlank(message = "El telefono es obligatorio")
    private String telefono;

    @Valid
    private List<DireccionRequest> direcciones;
}
