package com.foodfast.restaurante_servicio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class HorarioRestauranteRequest {

    @NotNull(message = "El id del restaurante es obligatorio")
    private Long restauranteId;

    @NotBlank(message = "El día de la semana es obligatorio")
    private String diaSemana;

    @NotNull(message = "La hora de apertura es obligatoria")
    private LocalTime horaApertura;

    @NotNull(message = "La hora de cierre es obligatoria")
    private LocalTime horaCierre;
}
