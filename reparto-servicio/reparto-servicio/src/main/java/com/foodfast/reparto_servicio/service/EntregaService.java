package com.foodfast.reparto_servicio.service;

import com.foodfast.reparto_servicio.client.PedidoClient;
import com.foodfast.reparto_servicio.dto.CambiarEstadoEntregaRequest;
import com.foodfast.reparto_servicio.dto.EntregaRequest;
import com.foodfast.reparto_servicio.dto.EntregaResponse;
import com.foodfast.reparto_servicio.dto.PedidoResponse;
import com.foodfast.reparto_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.reparto_servicio.exception.ReglaNegocioException;
import com.foodfast.reparto_servicio.model.Entrega;
import com.foodfast.reparto_servicio.model.EstadoEntrega;
import com.foodfast.reparto_servicio.model.Repartidor;
import com.foodfast.reparto_servicio.repository.EntregaRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntregaService {

    private final EntregaRepository entregaRepository;
    private final RepartidorService repartidorService;
    private final PedidoClient pedidoClient;

    @Value("${services.pedido.enabled:false}")
    private boolean pedidoIntegrationEnabled;

    @Transactional(readOnly = true)
    public List<EntregaResponse> listar() {
        log.info("Listando entregas");
        return entregaRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EntregaResponse buscarPorId(Long id) {
        validarId(id, "id");
        log.info("Buscando entrega id={}", id);
        return toResponse(obtenerEntidad(id));
    }

    @Transactional(readOnly = true)
    public List<EntregaResponse> listarPorPedido(Long pedidoId) {
        validarId(pedidoId, "pedidoId");
        log.info("Listando entregas para pedidoId={}", pedidoId);
        return entregaRepository.findByPedidoId(pedidoId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<EntregaResponse> listarPorRepartidor(Long repartidorId) {
        validarId(repartidorId, "repartidorId");
        log.info("Listando entregas para repartidorId={}", repartidorId);
        return entregaRepository.findByRepartidorId(repartidorId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public EntregaResponse crear(EntregaRequest request) {
        log.info("Creando entrega para pedidoId={}", request.getPedidoId());
        validarPedidoRemotoSiCorresponde(request.getPedidoId());

        if (entregaRepository.existsByPedidoId(request.getPedidoId())) {
            throw new ReglaNegocioException("Ya existe una entrega para el pedidoId: " + request.getPedidoId());
        }

        Repartidor repartidor = null;
        EstadoEntrega estado = EstadoEntrega.CREADA;

        if (request.getRepartidorId() != null) {
            repartidor = validarRepartidorDisponible(request.getRepartidorId());
            repartidor.setDisponible(false);
            estado = EstadoEntrega.ASIGNADA;
        }

        Entrega entrega = Entrega.builder()
                .pedidoId(request.getPedidoId())
                .repartidor(repartidor)
                .direccionEntrega(request.getDireccionEntrega())
                .estadoEntrega(estado)
                .fechaCreacion(LocalDateTime.now())
                .build();

        Entrega guardada = entregaRepository.save(entrega);
        log.info("Entrega creada id={} para pedidoId={}", guardada.getId(), guardada.getPedidoId());
        return toResponse(guardada);
    }

    @Transactional
    public EntregaResponse actualizar(Long id, EntregaRequest request) {
        validarId(id, "id");
        Entrega entrega = obtenerEntidad(id);
        log.info("Actualizando entrega id={}", id);

        if (entrega.getEstadoEntrega() == EstadoEntrega.ENTREGADA || entrega.getEstadoEntrega() == EstadoEntrega.CANCELADA) {
            throw new ReglaNegocioException("No se puede actualizar una entrega finalizada o cancelada");
        }

        if (!entrega.getPedidoId().equals(request.getPedidoId())) {
            validarPedidoRemotoSiCorresponde(request.getPedidoId());
            if (entregaRepository.existsByPedidoId(request.getPedidoId())) {
                throw new ReglaNegocioException("Ya existe una entrega para el pedidoId: " + request.getPedidoId());
            }
        }

        entrega.setPedidoId(request.getPedidoId());
        entrega.setDireccionEntrega(request.getDireccionEntrega());
        entrega.setFechaActualizacion(LocalDateTime.now());

        return toResponse(entregaRepository.save(entrega));
    }

    @Transactional
    public EntregaResponse asignarRepartidor(Long entregaId, Long repartidorId) {
        validarId(entregaId, "entregaId");
        validarId(repartidorId, "repartidorId");
        log.info("Asignando repartidor id={} a entrega id={}", repartidorId, entregaId);

        Entrega entrega = obtenerEntidad(entregaId);
        if (entrega.getEstadoEntrega() != EstadoEntrega.CREADA) {
            throw new ReglaNegocioException("Solo se puede asignar repartidor a entregas creadas");
        }

        Repartidor repartidor = validarRepartidorDisponible(repartidorId);
        repartidor.setDisponible(false);
        entrega.setRepartidor(repartidor);
        entrega.setEstadoEntrega(EstadoEntrega.ASIGNADA);
        entrega.setFechaActualizacion(LocalDateTime.now());
        return toResponse(entregaRepository.save(entrega));
    }

    @Transactional
    public EntregaResponse cambiarEstado(Long id, CambiarEstadoEntregaRequest request) {
        validarId(id, "id");
        log.info("Cambiando estado de entrega id={} a {}", id, request.getEstadoEntrega());

        Entrega entrega = obtenerEntidad(id);
        validarCambioEstado(entrega, request.getEstadoEntrega());
        entrega.setEstadoEntrega(request.getEstadoEntrega());
        entrega.setFechaActualizacion(LocalDateTime.now());

        if (request.getEstadoEntrega() == EstadoEntrega.ENTREGADA || request.getEstadoEntrega() == EstadoEntrega.CANCELADA) {
            liberarRepartidor(entrega);
        }

        return toResponse(entregaRepository.save(entrega));
    }

    @Transactional
    public void eliminar(Long id) {
        validarId(id, "id");
        Entrega entrega = obtenerEntidad(id);
        if (entrega.getEstadoEntrega() == EstadoEntrega.EN_CAMINO) {
            throw new ReglaNegocioException("No se puede eliminar una entrega en camino");
        }
        liberarRepartidor(entrega);
        log.warn("Eliminando entrega id={}", id);
        entregaRepository.delete(entrega);
    }

    private void validarPedidoRemotoSiCorresponde(Long pedidoId) {
        if (!pedidoIntegrationEnabled) {
            return;
        }
        PedidoResponse pedido = pedidoClient.obtenerPedido(pedidoId);
        if (pedido.getId() == null || !pedido.getId().equals(pedidoId)) {
            throw new ReglaNegocioException("pedido-servicio devolvió un pedido inconsistente");
        }
    }

    private Repartidor validarRepartidorDisponible(Long repartidorId) {
        Repartidor repartidor = repartidorService.obtenerEntidad(repartidorId);
        if (!Boolean.TRUE.equals(repartidor.getActivo()) || !Boolean.TRUE.equals(repartidor.getDisponible())) {
            throw new ReglaNegocioException("El repartidor no esta activo o disponible");
        }
        return repartidor;
    }

    private void validarCambioEstado(Entrega entrega, EstadoEntrega nuevo) {
        EstadoEntrega actual = entrega.getEstadoEntrega();
        if (nuevo == null) {
            throw new ReglaNegocioException("El nuevo estado de entrega es obligatorio");
        }
        if (actual == EstadoEntrega.ENTREGADA || actual == EstadoEntrega.CANCELADA) {
            throw new ReglaNegocioException("No se puede modificar una entrega finalizada o cancelada");
        }
        if (nuevo == EstadoEntrega.CREADA && actual != EstadoEntrega.CREADA) {
            throw new ReglaNegocioException("No se puede volver una entrega al estado CREADA");
        }
        if ((nuevo == EstadoEntrega.EN_CAMINO || nuevo == EstadoEntrega.ENTREGADA) && entrega.getRepartidor() == null) {
            throw new ReglaNegocioException("La entrega necesita un repartidor asignado antes de avanzar");
        }
        if (actual == EstadoEntrega.CREADA && !(nuevo == EstadoEntrega.ASIGNADA || nuevo == EstadoEntrega.CANCELADA)) {
            throw new ReglaNegocioException("Desde CREADA solo se permite pasar a ASIGNADA o CANCELADA");
        }
        if (actual == EstadoEntrega.ASIGNADA && !(nuevo == EstadoEntrega.EN_CAMINO || nuevo == EstadoEntrega.CANCELADA)) {
            throw new ReglaNegocioException("Desde ASIGNADA solo se permite pasar a EN_CAMINO o CANCELADA");
        }
        if (actual == EstadoEntrega.EN_CAMINO && !(nuevo == EstadoEntrega.ENTREGADA || nuevo == EstadoEntrega.CANCELADA)) {
            throw new ReglaNegocioException("Desde EN_CAMINO solo se permite pasar a ENTREGADA o CANCELADA");
        }
    }

    private void liberarRepartidor(Entrega entrega) {
        if (entrega.getRepartidor() != null) {
            entrega.getRepartidor().setDisponible(true);
        }
    }

    private Entrega obtenerEntidad(Long id) {
        return entregaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Entrega no encontrada con id: " + id));
    }

    private void validarId(Long id, String campo) {
        if (id == null || id <= 0) {
            throw new ReglaNegocioException("El campo " + campo + " debe ser mayor que cero");
        }
    }

    private EntregaResponse toResponse(Entrega entrega) {
        Repartidor repartidor = entrega.getRepartidor();
        return EntregaResponse.builder()
                .id(entrega.getId())
                .pedidoId(entrega.getPedidoId())
                .repartidorId(repartidor != null ? repartidor.getId() : null)
                .repartidorNombre(repartidor != null ? repartidor.getNombre() : null)
                .direccionEntrega(entrega.getDireccionEntrega())
                .estadoEntrega(entrega.getEstadoEntrega())
                .fechaCreacion(entrega.getFechaCreacion())
                .fechaActualizacion(entrega.getFechaActualizacion())
                .build();
    }
}
