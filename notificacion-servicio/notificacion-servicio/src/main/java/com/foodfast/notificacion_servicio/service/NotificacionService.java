package com.foodfast.notificacion_servicio.service;

import com.foodfast.notificacion_servicio.client.PedidoClient;
import com.foodfast.notificacion_servicio.dto.NotificacionPedidoRequest;
import com.foodfast.notificacion_servicio.dto.NotificacionRequest;
import com.foodfast.notificacion_servicio.dto.NotificacionResponse;
import com.foodfast.notificacion_servicio.dto.PedidoResponse;
import com.foodfast.notificacion_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.notificacion_servicio.exception.ReglaNegocioException;
import com.foodfast.notificacion_servicio.model.EstadoNotificacion;
import com.foodfast.notificacion_servicio.model.Notificacion;
import com.foodfast.notificacion_servicio.model.TipoNotificacion;
import com.foodfast.notificacion_servicio.repository.NotificacionRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final PedidoClient pedidoClient;

    @Transactional(readOnly = true)
    public List<NotificacionResponse> listar() {
        return notificacionRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public NotificacionResponse buscarPorId(Long id) {
        return toResponse(obtenerNotificacion(id));
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponse> listarPorCliente(Long clienteId) {
        validarId(clienteId, "clienteId");
        return notificacionRepository.findByClienteId(clienteId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponse> listarPorEstado(EstadoNotificacion estado) {
        if (estado == null) {
            throw new ReglaNegocioException("El estado es obligatorio");
        }
        return notificacionRepository.findByEstado(estado).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponse> listarPorClienteYEstado(Long clienteId, EstadoNotificacion estado) {
        validarId(clienteId, "clienteId");
        if (estado == null) {
            throw new ReglaNegocioException("El estado es obligatorio");
        }
        return notificacionRepository.findByClienteIdAndEstado(clienteId, estado).stream().map(this::toResponse).toList();
    }

    @Transactional
    public NotificacionResponse crear(NotificacionRequest request) {
        validarReferencia(request.getReferenciaTipo(), request.getReferenciaId());
        Notificacion notificacion = Notificacion.builder()
                .clienteId(request.getClienteId())
                .tipo(request.getTipo())
                .canal(request.getCanal())
                .estado(EstadoNotificacion.PENDIENTE)
                .titulo(request.getTitulo())
                .mensaje(request.getMensaje())
                .referenciaTipo(request.getReferenciaTipo())
                .referenciaId(request.getReferenciaId())
                .fechaCreacion(LocalDateTime.now())
                .build();
        Notificacion guardada = notificacionRepository.save(notificacion);
        log.info("Notificación creada id={} clienteId={}", guardada.getId(), guardada.getClienteId());
        return toResponse(guardada);
    }

    @Transactional
    public NotificacionResponse crearParaPedido(NotificacionPedidoRequest request) {
        validarId(request.getPedidoId(), "pedidoId");

        Long clienteId = request.getClienteId();
        if (pedidoClient.isEnabled()) {
            PedidoResponse pedido = pedidoClient.buscarPedido(request.getPedidoId());
            clienteId = pedido.getClienteId();
        }

        if (clienteId == null || clienteId <= 0) {
            throw new ReglaNegocioException("Debe enviar clienteId cuando la integración con pedido-servicio está desactivada");
        }

        NotificacionRequest notificacionRequest = NotificacionRequest.builder()
                .clienteId(clienteId)
                .tipo(request.getTipo())
                .canal(request.getCanal())
                .titulo(tituloPorTipo(request.getTipo()))
                .mensaje("Tu pedido #" + request.getPedidoId() + " tiene una actualización: " + request.getTipo())
                .referenciaTipo("PEDIDO")
                .referenciaId(request.getPedidoId())
                .build();

        return crear(notificacionRequest);
    }

    @Transactional
    public NotificacionResponse actualizar(Long id, NotificacionRequest request) {
        validarReferencia(request.getReferenciaTipo(), request.getReferenciaId());
        Notificacion notificacion = obtenerNotificacion(id);

        if (notificacion.getEstado() == EstadoNotificacion.ENVIADA || notificacion.getEstado() == EstadoNotificacion.LEIDA) {
            throw new ReglaNegocioException("No se puede editar una notificación enviada o leída");
        }

        notificacion.setClienteId(request.getClienteId());
        notificacion.setTipo(request.getTipo());
        notificacion.setCanal(request.getCanal());
        notificacion.setTitulo(request.getTitulo());
        notificacion.setMensaje(request.getMensaje());
        notificacion.setReferenciaTipo(request.getReferenciaTipo());
        notificacion.setReferenciaId(request.getReferenciaId());

        return toResponse(notificacionRepository.save(notificacion));
    }

    @Transactional
    public NotificacionResponse enviar(Long id) {
        Notificacion notificacion = obtenerNotificacion(id);

        if (notificacion.getEstado() == EstadoNotificacion.ENVIADA) {
            throw new ReglaNegocioException("La notificación ya fue enviada");
        }
        if (notificacion.getEstado() == EstadoNotificacion.LEIDA) {
            throw new ReglaNegocioException("No se puede reenviar una notificación ya leída");
        }

        notificacion.setEstado(EstadoNotificacion.ENVIADA);
        notificacion.setFechaEnvio(LocalDateTime.now());
        notificacion.setErrorEnvio(null);
        return toResponse(notificacionRepository.save(notificacion));
    }

    @Transactional
    public NotificacionResponse registrarErrorEnvio(Long id, String motivo) {
        Notificacion notificacion = obtenerNotificacion(id);
        if (notificacion.getEstado() == EstadoNotificacion.LEIDA) {
            throw new ReglaNegocioException("No se puede registrar error en una notificación leída");
        }
        notificacion.setEstado(EstadoNotificacion.ERROR);
        notificacion.setErrorEnvio(motivo == null || motivo.isBlank() ? "Error no especificado" : motivo);
        return toResponse(notificacionRepository.save(notificacion));
    }

    @Transactional
    public NotificacionResponse marcarComoLeida(Long id) {
        Notificacion notificacion = obtenerNotificacion(id);

        if (notificacion.getEstado() != EstadoNotificacion.ENVIADA) {
            throw new ReglaNegocioException("Solo se puede marcar como leída una notificación enviada");
        }

        notificacion.setEstado(EstadoNotificacion.LEIDA);
        notificacion.setFechaLectura(LocalDateTime.now());
        return toResponse(notificacionRepository.save(notificacion));
    }

    @Transactional
    public void eliminar(Long id) {
        Notificacion notificacion = obtenerNotificacion(id);
        notificacionRepository.delete(notificacion);
    }

    private Notificacion obtenerNotificacion(Long id) {
        validarId(id, "id");
        return notificacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificación no encontrada con id: " + id));
    }

    private void validarId(Long id, String campo) {
        if (id == null || id <= 0) {
            throw new ReglaNegocioException("El campo " + campo + " debe ser mayor que cero");
        }
    }

    private void validarReferencia(String referenciaTipo, Long referenciaId) {
        boolean tieneTipo = referenciaTipo != null && !referenciaTipo.isBlank();
        boolean tieneId = referenciaId != null;
        if (tieneTipo != tieneId) {
            throw new ReglaNegocioException("referenciaTipo y referenciaId deben enviarse juntos");
        }
        if (referenciaId != null && referenciaId <= 0) {
            throw new ReglaNegocioException("referenciaId debe ser mayor que cero");
        }
    }

    private String tituloPorTipo(TipoNotificacion tipo) {
        return switch (tipo) {
            case PEDIDO_CONFIRMADO -> "Pedido confirmado";
            case PEDIDO_EN_REPARTO -> "Pedido en reparto";
            case PEDIDO_ENTREGADO -> "Pedido entregado";
            case PAGO_APROBADO -> "Pago aprobado";
            case PAGO_RECHAZADO -> "Pago rechazado";
            case PROMOCION -> "Promoción FoodFast";
            case SISTEMA -> "Notificación del sistema";
        };
    }

    private NotificacionResponse toResponse(Notificacion notificacion) {
        return NotificacionResponse.builder()
                .id(notificacion.getId())
                .clienteId(notificacion.getClienteId())
                .tipo(notificacion.getTipo())
                .canal(notificacion.getCanal())
                .estado(notificacion.getEstado())
                .titulo(notificacion.getTitulo())
                .mensaje(notificacion.getMensaje())
                .referenciaTipo(notificacion.getReferenciaTipo())
                .referenciaId(notificacion.getReferenciaId())
                .fechaCreacion(notificacion.getFechaCreacion())
                .fechaEnvio(notificacion.getFechaEnvio())
                .fechaLectura(notificacion.getFechaLectura())
                .errorEnvio(notificacion.getErrorEnvio())
                .build();
    }
}
