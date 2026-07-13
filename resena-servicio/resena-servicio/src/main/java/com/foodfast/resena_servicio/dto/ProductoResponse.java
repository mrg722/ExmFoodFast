package com.foodfast.resena_servicio.dto;

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
public class ProductoResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private Boolean activo;
}
