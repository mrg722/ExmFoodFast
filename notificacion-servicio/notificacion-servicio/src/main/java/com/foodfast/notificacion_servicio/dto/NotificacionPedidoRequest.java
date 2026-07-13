package com.foodfast.notificacion_servicio.dto;

import com.foodfast.notificacion_servicio.model.CanalNotificacion;
import com.foodfast.notificacion_servicio.model.TipoNotificacion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class NotificacionPedidoRequest {

    @NotNull(message = "El pedidoId es obligatorio")
    @Positive(message = "El pedidoId debe ser mayor que cero")
    private Long pedidoId;

    @Positive(message = "El clienteId debe ser mayor que cero")
    private Long clienteId;

    @NotNull(message = "El tipo de notificacion es obligatorio")
    private TipoNotificacion tipo;

    @NotNull(message = "El canal de notificacion es obligatorio")
    private CanalNotificacion canal;
}
