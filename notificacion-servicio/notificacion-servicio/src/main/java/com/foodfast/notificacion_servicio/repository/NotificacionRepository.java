package com.foodfast.notificacion_servicio.repository;

import com.foodfast.notificacion_servicio.model.EstadoNotificacion;
import com.foodfast.notificacion_servicio.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByClienteId(Long clienteId);
    List<Notificacion> findByEstado(EstadoNotificacion estado);
    List<Notificacion> findByClienteIdAndEstado(Long clienteId, EstadoNotificacion estado);
}
