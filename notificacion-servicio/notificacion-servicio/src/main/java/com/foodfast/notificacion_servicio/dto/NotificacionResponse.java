package com.foodfast.notificacion_servicio.dto;

import com.foodfast.notificacion_servicio.model.CanalNotificacion;
import com.foodfast.notificacion_servicio.model.EstadoNotificacion;
import com.foodfast.notificacion_servicio.model.TipoNotificacion;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionResponse {
    private Long id;
    private Long clienteId;
    private TipoNotificacion tipo;
    private CanalNotificacion canal;
    private EstadoNotificacion estado;
    private String titulo;
    private String mensaje;
    private String referenciaTipo;
    private Long referenciaId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEnvio;
    private LocalDateTime fechaLectura;
    private String errorEnvio;
}
