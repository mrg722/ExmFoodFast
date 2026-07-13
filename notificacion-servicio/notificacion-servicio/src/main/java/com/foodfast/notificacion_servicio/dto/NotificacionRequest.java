package com.foodfast.notificacion_servicio.dto;

import com.foodfast.notificacion_servicio.model.CanalNotificacion;
import com.foodfast.notificacion_servicio.model.TipoNotificacion;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionRequest {

    @NotNull(message = "El clienteId es obligatorio")
    @Positive(message = "El clienteId debe ser mayor que cero")
    private Long clienteId;

    @NotNull(message = "El tipo de notificacion es obligatorio")
    private TipoNotificacion tipo;

    @NotNull(message = "El canal de notificacion es obligatorio")
    private CanalNotificacion canal;

    @NotBlank(message = "El titulo es obligatorio")
    @Size(max = 150, message = "El titulo no puede superar 150 caracteres")
    private String titulo;

    @NotBlank(message = "El mensaje es obligatorio")
    @Size(max = 500, message = "El mensaje no puede superar 500 caracteres")
    private String mensaje;

    private String referenciaTipo;
    private Long referenciaId;
}
