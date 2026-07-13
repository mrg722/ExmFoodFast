package com.foodfast.pedido_servicio.service;

import com.foodfast.pedido_servicio.client.InventarioClient;
import com.foodfast.pedido_servicio.dto.InventarioStockResponse;
import com.foodfast.pedido_servicio.dto.PedidoRequest;
import com.foodfast.pedido_servicio.dto.PedidoResponse;
import com.foodfast.pedido_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.pedido_servicio.exception.ReglaNegocioException;
import com.foodfast.pedido_servicio.model.EstadoPedido;
import com.foodfast.pedido_servicio.model.Pedido;
import com.foodfast.pedido_servicio.repository.PedidoRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final InventarioClient inventarioClient;

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar() {
        return pedidoRepository.findAll()
                .stream()
                .map(this::mapearResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(Long id) {
        return mapearResponse(obtenerPedido(id));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPorCliente(Long clienteId) {
        validarId(clienteId, "clienteId");

        return pedidoRepository.findByClienteId(clienteId)
                .stream()
                .map(this::mapearResponse)
                .toList();
    }

    @Transactional
    public PedidoResponse crear(PedidoRequest request) {
        return crear(request, null);
    }

    @Transactional
    public PedidoResponse crear(PedidoRequest request, String authorizationHeader) {
        validarDatosInventario(request);

        String estadoStock = "NO CONSULTADO";
        String mensajeInventario = "Pedido creado sin validación de inventario";

        if (usaInventario(request)) {
            InventarioStockResponse stock = hasAuthorization(authorizationHeader)
                    ? inventarioClient.consultarStock(request.getProductoId(), authorizationHeader)
                    : inventarioClient.consultarStock(request.getProductoId());
            validarStockSuficiente(stock, request.getCantidad());
            estadoStock = "STOCK DISPONIBLE";
            mensajeInventario = "Stock validado en inventario-servicio";
        }

        Pedido pedido = Pedido.builder()
                .clienteId(request.getClienteId())
                .productoId(request.getProductoId())
                .cantidad(request.getCantidad())
                .fechaCreacion(LocalDateTime.now())
                .estado(EstadoPedido.CONFIRMADO)
                .total(request.getTotal())
                .direccionEntrega(request.getDireccionEntrega())
                .observacion(request.getObservacion())
                .build();

        Pedido guardado = pedidoRepository.save(pedido);
        log.info("Pedido creado id={} clienteId={}", guardado.getId(), guardado.getClienteId());

        if (usaInventario(request)) {
            if (hasAuthorization(authorizationHeader)) {
                inventarioClient.descontarStock(request.getProductoId(), request.getCantidad(), authorizationHeader);
            } else {
                inventarioClient.descontarStock(request.getProductoId(), request.getCantidad());
            }
            mensajeInventario = "Stock descontado correctamente en inventario-servicio";
        }

        PedidoResponse response = mapearResponse(guardado);
        response.setEstadoStock(estadoStock);
        response.setMensajeInventario(mensajeInventario);
        return response;
    }

    @Transactional
    public PedidoResponse actualizar(Long id, PedidoRequest request) {
        validarDatosInventario(request);
        Pedido pedido = obtenerPedido(id);

        if (EstadoPedido.CANCELADO.equals(pedido.getEstado())) {
            throw new ReglaNegocioException("No se puede actualizar un pedido cancelado");
        }

        String estadoStock = "NO CONSULTADO";
        String mensajeInventario = "Pedido actualizado sin validación de inventario";

        if (usaInventario(request)) {
            InventarioStockResponse stock = inventarioClient.consultarStock(request.getProductoId());
            validarStockSuficiente(stock, request.getCantidad());
            estadoStock = "STOCK DISPONIBLE";
            mensajeInventario = "Stock validado en inventario-servicio";
        }

        pedido.setClienteId(request.getClienteId());
        pedido.setProductoId(request.getProductoId());
        pedido.setCantidad(request.getCantidad());
        pedido.setTotal(request.getTotal());
        pedido.setDireccionEntrega(request.getDireccionEntrega());
        pedido.setObservacion(request.getObservacion());

        Pedido actualizado = pedidoRepository.save(pedido);
        log.info("Pedido actualizado id={}", actualizado.getId());

        PedidoResponse response = mapearResponse(actualizado);
        response.setEstadoStock(estadoStock);
        response.setMensajeInventario(mensajeInventario);
        return response;
    }

    @Transactional
    public void cancelar(Long id) {
        Pedido pedido = obtenerPedido(id);

        if (EstadoPedido.CANCELADO.equals(pedido.getEstado())) {
            throw new ReglaNegocioException("El pedido ya se encuentra cancelado");
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
        pedidoRepository.save(pedido);
        log.info("Pedido cancelado id={}", id);
    }

    @Transactional
    public void eliminar(Long id) {
        Pedido pedido = obtenerPedido(id);
        pedidoRepository.delete(pedido);
        log.info("Pedido eliminado id={}", id);
    }

    private Pedido obtenerPedido(Long id) {
        validarId(id, "id");

        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado con id: " + id));
    }

    private void validarId(Long id, String campo) {
        if (id == null || id <= 0) {
            throw new ReglaNegocioException("El campo " + campo + " debe ser mayor a cero");
        }
    }

    private void validarDatosInventario(PedidoRequest request) {
        boolean tieneProducto = request.getProductoId() != null;
        boolean tieneCantidad = request.getCantidad() != null;

        if (tieneProducto != tieneCantidad) {
            throw new ReglaNegocioException("Para validar inventario debe enviar productoId y cantidad juntos");
        }
    }

    private boolean usaInventario(PedidoRequest request) {
        return request.getProductoId() != null && request.getCantidad() != null;
    }

    private boolean hasAuthorization(String authorizationHeader) {
        return authorizationHeader != null && !authorizationHeader.isBlank();
    }

    private void validarStockSuficiente(InventarioStockResponse stock, Integer cantidadSolicitada) {
        Integer stockDisponible = obtenerStockDisponible(stock);

        if (stockDisponible == null) {
            throw new ReglaNegocioException("Inventario no devolvió una cantidad de stock válida");
        }

        Boolean hayStock = stock.getHayStock() != null ? stock.getHayStock() : stock.getDisponible();
        if (Boolean.FALSE.equals(hayStock) || stockDisponible < cantidadSolicitada) {
            log.warn("Stock insuficiente productoId={} stockDisponible={} cantidadSolicitada={}",
                    stock.getProductoId(), stockDisponible, cantidadSolicitada);
            throw new ReglaNegocioException("No hay stock suficiente para crear el pedido");
        }
    }

    private Integer obtenerStockDisponible(InventarioStockResponse stock) {
        if (stock == null) {
            throw new ReglaNegocioException("Inventario no devolvió información de stock");
        }
        if (stock.getStockDisponible() != null) {
            return stock.getStockDisponible();
        }
        if (stock.getStockReal() != null) {
            return stock.getStockReal();
        }
        if (stock.getStock() != null) {
            return stock.getStock();
        }
        return stock.getCantidadDisponible();
    }

    private PedidoResponse mapearResponse(Pedido pedido) {
        return PedidoResponse.builder()
                .id(pedido.getId())
                .clienteId(pedido.getClienteId())
                .productoId(pedido.getProductoId())
                .cantidad(pedido.getCantidad())
                .fechaCreacion(pedido.getFechaCreacion())
                .estado(pedido.getEstado().name())
                .total(pedido.getTotal())
                .direccionEntrega(pedido.getDireccionEntrega())
                .observacion(pedido.getObservacion())
                .build();
    }
}
