package com.foodfast.notificacion_servicio.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.foodfast.notificacion_servicio.model.CanalNotificacion;
import com.foodfast.notificacion_servicio.model.EstadoNotificacion;
import com.foodfast.notificacion_servicio.model.Notificacion;
import com.foodfast.notificacion_servicio.model.TipoNotificacion;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class NotificacionRepositoryTest {

    @Autowired
    private NotificacionRepository repository;

    @Test
    void debeGuardarBuscarListarYEliminar() {
        Notificacion guardada = repository.save(entidad(10L, EstadoNotificacion.PENDIENTE));

        assertThat(guardada.getId()).isNotNull();
        assertThat(repository.findById(guardada.getId())).isPresent();
        assertThat(repository.findAll()).hasSize(1);

        repository.delete(guardada);

        assertThat(repository.findById(guardada.getId())).isEmpty();
    }

    @Test
    void debeBuscarPorClienteEstadoYClienteEstado() {
        repository.save(entidad(10L, EstadoNotificacion.PENDIENTE));
        repository.save(entidad(10L, EstadoNotificacion.ENVIADA));
        repository.save(entidad(20L, EstadoNotificacion.ENVIADA));

        assertThat(repository.findByClienteId(10L)).hasSize(2);
        assertThat(repository.findByEstado(EstadoNotificacion.ENVIADA)).hasSize(2);
        assertThat(repository.findByClienteIdAndEstado(10L, EstadoNotificacion.ENVIADA)).hasSize(1);
    }

    private Notificacion entidad(Long clienteId, EstadoNotificacion estado) {
        return Notificacion.builder()
                .clienteId(clienteId)
                .tipo(TipoNotificacion.PEDIDO_CONFIRMADO)
                .canal(CanalNotificacion.EMAIL)
                .estado(estado)
                .titulo("Pedido confirmado")
                .mensaje("Tu pedido fue confirmado")
                .referenciaTipo("PEDIDO")
                .referenciaId(100L)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }
}
