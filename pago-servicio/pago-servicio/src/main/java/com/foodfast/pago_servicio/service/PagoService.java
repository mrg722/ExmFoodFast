package com.foodfast.pago_servicio.service;

import com.foodfast.pago_servicio.client.PedidoClient;
import com.foodfast.pago_servicio.dto.ActualizarPagoRequest;
import com.foodfast.pago_servicio.dto.PagoRequest;
import com.foodfast.pago_servicio.dto.PagoResponse;
import com.foodfast.pago_servicio.dto.PedidoResponse;
import com.foodfast.pago_servicio.dto.ProcesarPagoRequest;
import com.foodfast.pago_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.pago_servicio.exception.ReglaNegocioException;
import com.foodfast.pago_servicio.model.EstadoPago;
import com.foodfast.pago_servicio.model.Pago;
import com.foodfast.pago_servicio.repository.PagoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final PedidoClient pedidoClient;

    @Transactional(readOnly = true)
    public List<PagoResponse> listar() {
        return pagoRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PagoResponse buscarPorId(Long id) {
        return toResponse(obtenerEntidad(id));
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> buscarPorPedidoId(Long pedidoId) {
        validarId(pedidoId, "pedidoId");
        return pagoRepository.findByPedidoId(pedidoId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public PagoResponse crear(PagoRequest request) {
        validarDatosBase(request.getPedidoId(), request.getMonto());

        if (pagoRepository.existsByPedidoId(request.getPedidoId())) {
            throw new ReglaNegocioException("Ya existe un pago registrado para este pedido");
        }

        PedidoResponse pedido = pedidoClient.obtenerPedido(request.getPedidoId());
        validarPedidoPagable(pedido);

        Pago pago = Pago.builder()
                .pedidoId(request.getPedidoId())
                .monto(request.getMonto())
                .metodoPago(request.getMetodoPago())
                .estadoPago(EstadoPago.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .build();

        Pago guardado = pagoRepository.save(pago);
        log.info("Pago creado id={} pedidoId={}", guardado.getId(), guardado.getPedidoId());

        PagoResponse response = toResponse(guardado);
        response.setMensajePedido("Pedido validado o integración remota desactivada");
        return response;
    }

    @Transactional
    public PagoResponse actualizar(Long id, ActualizarPagoRequest request) {
        validarDatosBase(id, request.getMonto());
        Pago pago = obtenerEntidad(id);

        if (pago.getEstadoPago() != EstadoPago.PENDIENTE) {
            throw new ReglaNegocioException("Solo se pueden actualizar pagos pendientes");
        }

        pago.setMonto(request.getMonto());
        pago.setMetodoPago(request.getMetodoPago());
        return toResponse(pagoRepository.save(pago));
    }

    @Transactional
    public PagoResponse procesar(ProcesarPagoRequest request) {
        validarId(request.getPagoId(), "pagoId");
        Pago pago = obtenerEntidad(request.getPagoId());

        if (pago.getEstadoPago() != EstadoPago.PENDIENTE) {
            throw new ReglaNegocioException("Solo se pueden procesar pagos pendientes");
        }

        pago.setEstadoPago(Boolean.TRUE.equals(request.getAprobado()) ? EstadoPago.APROBADO : EstadoPago.RECHAZADO);
        pago.setCodigoTransaccion("FF-" + UUID.randomUUID());
        pago.setFechaProcesamiento(LocalDateTime.now());
        return toResponse(pagoRepository.save(pago));
    }

    @Transactional
    public PagoResponse anular(Long id) {
        Pago pago = obtenerEntidad(id);

        if (pago.getEstadoPago() == EstadoPago.APROBADO) {
            throw new ReglaNegocioException("No se puede anular un pago aprobado");
        }
        if (pago.getEstadoPago() == EstadoPago.ANULADO) {
            throw new ReglaNegocioException("El pago ya se encuentra anulado");
        }

        pago.setEstadoPago(EstadoPago.ANULADO);
        pago.setFechaProcesamiento(LocalDateTime.now());
        return toResponse(pagoRepository.save(pago));
    }

    @Transactional
    public void eliminar(Long id) {
        Pago pago = obtenerEntidad(id);
        pagoRepository.delete(pago);
    }

    private Pago obtenerEntidad(Long id) {
        validarId(id, "id");
        return pagoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago no encontrado con id: " + id));
    }

    private void validarDatosBase(Long pedidoId, BigDecimal monto) {
        validarId(pedidoId, "pedidoId");
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El monto debe ser mayor que cero");
        }
    }

    private void validarId(Long id, String campo) {
        if (id == null || id <= 0) {
            throw new ReglaNegocioException("El campo " + campo + " debe ser mayor a cero");
        }
    }

    private void validarPedidoPagable(PedidoResponse pedido) {
        if (pedido == null || pedido.getId() == null) {
            throw new ReglaNegocioException("No se pudo validar el pedido asociado al pago");
        }
        String estado = pedido.getEstado();
        if ("CANCELADO".equalsIgnoreCase(estado) || "ENTREGADO".equalsIgnoreCase(estado)) {
            throw new ReglaNegocioException("El pedido no se encuentra en un estado pagable");
        }
    }

    private PagoResponse toResponse(Pago pago) {
        return PagoResponse.builder()
                .id(pago.getId())
                .pedidoId(pago.getPedidoId())
                .monto(pago.getMonto())
                .metodoPago(pago.getMetodoPago())
                .estadoPago(pago.getEstadoPago())
                .codigoTransaccion(pago.getCodigoTransaccion())
                .fechaCreacion(pago.getFechaCreacion())
                .fechaProcesamiento(pago.getFechaProcesamiento())
                .build();
    }
}
