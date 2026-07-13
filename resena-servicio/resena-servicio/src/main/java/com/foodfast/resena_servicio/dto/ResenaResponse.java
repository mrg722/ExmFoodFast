package com.foodfast.resena_servicio.dto;

import java.time.LocalDateTime;
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
public class ResenaResponse {
    private Long id;
    private Long clienteId;
    private Long productoId;
    private Long restauranteId;
    private Integer calificacion;
    private String comentario;
    private Boolean activa;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
