package com.foodfast.cliente_servicio.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private Boolean activo;
    private List<DireccionResponse> direcciones;
}
