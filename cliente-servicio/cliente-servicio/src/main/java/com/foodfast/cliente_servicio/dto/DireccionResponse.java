package com.foodfast.cliente_servicio.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DireccionResponse {
    private Long id;
    private String calle;
    private String numero;
    private String comuna;
    private String ciudad;
    private String referencia;
    private Boolean principal;
}
