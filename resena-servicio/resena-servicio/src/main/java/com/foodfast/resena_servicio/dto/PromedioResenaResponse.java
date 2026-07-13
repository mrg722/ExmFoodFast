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
public class PromedioResenaResponse {
    private Long productoId;
    private Double promedio;
    private Long totalResenas;
}
