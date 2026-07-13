package com.foodfast.restaurante_servicio.dto;
 
import lombok.Builder;
import lombok.Data;
 
import java.time.LocalTime;
 
@Data
@Builder
public class HorarioRestauranteResponse {
    private Long id;
    private Long restauranteId;
    private String diaSemana;
    private LocalTime horaApertura;
    private LocalTime horaCierre;
}
