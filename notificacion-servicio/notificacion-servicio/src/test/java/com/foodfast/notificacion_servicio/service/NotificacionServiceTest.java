package com.foodfast.notificacion_servicio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.foodfast.notificacion_servicio.client.PedidoClient;
import com.foodfast.notificacion_servicio.dto.NotificacionPedidoRequest;
import com.foodfast.notificacion_servicio.dto.NotificacionRequest;
import com.foodfast.notificacion_servicio.dto.NotificacionResponse;
import com.foodfast.notificacion_servicio.dto.PedidoResponse;
import com.foodfast.notificacion_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.notificacion_servicio.exception.ReglaNegocioException;
import com.foodfast.notificacion_servicio.model.CanalNotificacion;
import com.foodfast.notificacion_servicio.model.EstadoNotificacion;
import com.foodfast.notificacion_servicio.model.Notificacion;
import com.foodfast.notificacion_servicio.model.TipoNotificacion;
import com.foodfast.notificacion_servicio.repository.NotificacionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository repository;

    @Mock
    private PedidoClient pedidoClient;

    private NotificacionService service;

    @BeforeEach
    void setUp() {
        service = new NotificacionService(repository, pedidoClient);
    }

    @Test
    void debeCrearNotificacionPendiente() {
        // Given
        NotificacionRequest request = requestBase();
        when(repository.save(any(Notificacion.class))).thenAnswer(invocation -> {
            Notificacion n = invocation.getArgument(0);
            n.setId(1L);
            return n;
        });

        // When
        NotificacionResponse response = service.crear(request);

        // Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEstado()).isEqualTo(EstadoNotificacion.PENDIENTE);
        assertThat(response.getClienteId()).isEqualTo(10L);
        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getFechaCreacion()).isNotNull();
    }

    @Test
    void debeRechazarReferenciaIncompleta() {
        NotificacionRequest request = requestBase();
        request.setReferenciaTipo("PEDIDO");
        request.setReferenciaId(null);

        assertThatThrownBy(() -> service.crear(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("referenciaTipo");
        verify(repository, never()).save(any());
    }

    @Test
    void debeCrearNotificacionParaPedidoSinIntegracionUsandoClienteIdDelRequest() {
        when(pedidoClient.isEnabled()).thenReturn(false);
        when(repository.save(any(Notificacion.class))).thenAnswer(invocation -> {
            Notificacion n = invocation.getArgument(0);
            n.setId(2L);
            return n;
        });

        NotificacionPedidoRequest request = NotificacionPedidoRequest.builder()
                .pedidoId(50L)
                .clienteId(99L)
                .tipo(TipoNotificacion.PEDIDO_EN_REPARTO)
                .canal(CanalNotificacion.PUSH)
                .build();

        NotificacionResponse response = service.crearParaPedido(request);

        assertThat(response.getClienteId()).isEqualTo(99L);
        assertThat(response.getReferenciaTipo()).isEqualTo("PEDIDO");
        assertThat(response.getReferenciaId()).isEqualTo(50L);
    }

    @Test
    void debeCrearNotificacionParaPedidoConClienteDesdePedidoServicio() {
        when(pedidoClient.isEnabled()).thenReturn(true);
        when(pedidoClient.buscarPedido(77L)).thenReturn(PedidoResponse.builder()
                .id(77L)
                .clienteId(88L)
                .estado("CONFIRMADO")
                .total(BigDecimal.valueOf(12000))
                .build());
        when(repository.save(any(Notificacion.class))).thenAnswer(invocation -> {
            Notificacion n = invocation.getArgument(0);
            n.setId(3L);
            return n;
        });

        NotificacionPedidoRequest request = NotificacionPedidoRequest.builder()
                .pedidoId(77L)
                .tipo(TipoNotificacion.PEDIDO_CONFIRMADO)
                .canal(CanalNotificacion.EMAIL)
                .build();

        NotificacionResponse response = service.crearParaPedido(request);

        assertThat(response.getClienteId()).isEqualTo(88L);
        verify(pedidoClient).buscarPedido(77L);
    }

    @Test
    void debeRechazarNotificacionParaPedidoSinClienteCuandoIntegracionEstaDesactivada() {
        when(pedidoClient.isEnabled()).thenReturn(false);
        NotificacionPedidoRequest request = NotificacionPedidoRequest.builder()
                .pedidoId(77L)
                .tipo(TipoNotificacion.SISTEMA)
                .canal(CanalNotificacion.SISTEMA)
                .build();

        assertThatThrownBy(() -> service.crearParaPedido(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("clienteId");
    }

    @Test
    void debeListarYBuscarPorFiltros() {
        Notificacion n1 = entidad(1L, EstadoNotificacion.PENDIENTE);
        Notificacion n2 = entidad(2L, EstadoNotificacion.ENVIADA);
        when(repository.findAll()).thenReturn(List.of(n1, n2));
        when(repository.findByClienteId(10L)).thenReturn(List.of(n1, n2));
        when(repository.findByEstado(EstadoNotificacion.PENDIENTE)).thenReturn(List.of(n1));
        when(repository.findByClienteIdAndEstado(10L, EstadoNotificacion.ENVIADA)).thenReturn(List.of(n2));

        assertThat(service.listar()).hasSize(2);
        assertThat(service.listarPorCliente(10L)).hasSize(2);
        assertThat(service.listarPorEstado(EstadoNotificacion.PENDIENTE)).hasSize(1);
        assertThat(service.listarPorClienteYEstado(10L, EstadoNotificacion.ENVIADA)).hasSize(1);
    }

    @Test
    void debeBuscarPorIdExistenteYRechazarIdInvalidoONoEncontrado() {
        when(repository.findById(1L)).thenReturn(Optional.of(entidad(1L, EstadoNotificacion.PENDIENTE)));
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThat(service.buscarPorId(1L).getId()).isEqualTo(1L);
        assertThatThrownBy(() -> service.buscarPorId(0L)).isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> service.buscarPorId(2L)).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void debeActualizarSoloSiEstaPendienteOError() {
        Notificacion notificacion = entidad(1L, EstadoNotificacion.PENDIENTE);
        when(repository.findById(1L)).thenReturn(Optional.of(notificacion));
        when(repository.save(any(Notificacion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        NotificacionRequest request = requestBase();
        request.setTitulo("Actualizado");

        NotificacionResponse response = service.actualizar(1L, request);

        assertThat(response.getTitulo()).isEqualTo("Actualizado");
    }

    @Test
    void debeRechazarActualizarSiYaFueEnviadaOLeida() {
        when(repository.findById(1L)).thenReturn(Optional.of(entidad(1L, EstadoNotificacion.ENVIADA)));
        when(repository.findById(2L)).thenReturn(Optional.of(entidad(2L, EstadoNotificacion.LEIDA)));

        assertThatThrownBy(() -> service.actualizar(1L, requestBase())).isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> service.actualizar(2L, requestBase())).isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void debeEnviarNotificacionPendienteYLimpiarError() {
        Notificacion notificacion = entidad(1L, EstadoNotificacion.ERROR);
        notificacion.setErrorEnvio("SMTP caído");
        when(repository.findById(1L)).thenReturn(Optional.of(notificacion));
        when(repository.save(any(Notificacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificacionResponse response = service.enviar(1L);

        assertThat(response.getEstado()).isEqualTo(EstadoNotificacion.ENVIADA);
        assertThat(response.getFechaEnvio()).isNotNull();
        assertThat(response.getErrorEnvio()).isNull();
    }

    @Test
    void debeRechazarEnviarSiYaEstaEnviadaOLeida() {
        when(repository.findById(1L)).thenReturn(Optional.of(entidad(1L, EstadoNotificacion.ENVIADA)));
        when(repository.findById(2L)).thenReturn(Optional.of(entidad(2L, EstadoNotificacion.LEIDA)));

        assertThatThrownBy(() -> service.enviar(1L)).isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> service.enviar(2L)).isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void debeRegistrarErrorDeEnvio() {
        Notificacion notificacion = entidad(1L, EstadoNotificacion.PENDIENTE);
        when(repository.findById(1L)).thenReturn(Optional.of(notificacion));
        when(repository.save(any(Notificacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificacionResponse response = service.registrarErrorEnvio(1L, "Correo inválido");

        assertThat(response.getEstado()).isEqualTo(EstadoNotificacion.ERROR);
        assertThat(response.getErrorEnvio()).isEqualTo("Correo inválido");
    }

    @Test
    void debeRegistrarErrorGenericoCuandoMotivoVieneVacioYRechazarSiEstaLeida() {
        when(repository.findById(1L)).thenReturn(Optional.of(entidad(1L, EstadoNotificacion.PENDIENTE)));
        when(repository.findById(2L)).thenReturn(Optional.of(entidad(2L, EstadoNotificacion.LEIDA)));
        when(repository.save(any(Notificacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.registrarErrorEnvio(1L, " ").getErrorEnvio()).contains("Error no especificado");
        assertThatThrownBy(() -> service.registrarErrorEnvio(2L, "error"))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void debeMarcarComoLeidaSoloSiEstaEnviada() {
        Notificacion enviada = entidad(1L, EstadoNotificacion.ENVIADA);
        when(repository.findById(1L)).thenReturn(Optional.of(enviada));
        when(repository.findById(2L)).thenReturn(Optional.of(entidad(2L, EstadoNotificacion.PENDIENTE)));
        when(repository.save(any(Notificacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificacionResponse response = service.marcarComoLeida(1L);

        assertThat(response.getEstado()).isEqualTo(EstadoNotificacion.LEIDA);
        assertThat(response.getFechaLectura()).isNotNull();
        assertThatThrownBy(() -> service.marcarComoLeida(2L)).isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void debeEliminarNotificacionExistente() {
        Notificacion notificacion = entidad(1L, EstadoNotificacion.PENDIENTE);
        when(repository.findById(1L)).thenReturn(Optional.of(notificacion));

        service.eliminar(1L);

        verify(repository).delete(notificacion);
    }

    private NotificacionRequest requestBase() {
        return NotificacionRequest.builder()
                .clienteId(10L)
                .tipo(TipoNotificacion.PEDIDO_CONFIRMADO)
                .canal(CanalNotificacion.EMAIL)
                .titulo("Pedido confirmado")
                .mensaje("Tu pedido fue confirmado")
                .referenciaTipo("PEDIDO")
                .referenciaId(100L)
                .build();
    }

    private Notificacion entidad(Long id, EstadoNotificacion estado) {
        return Notificacion.builder()
                .id(id)
                .clienteId(10L)
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
