package com.foodfast.inventario_servicio.service;

import com.foodfast.inventario_servicio.dto.DescontarStockRequest;
import com.foodfast.inventario_servicio.dto.InventarioRequest;
import com.foodfast.inventario_servicio.dto.InventarioResponse;
import com.foodfast.inventario_servicio.dto.StockResponse;
import com.foodfast.inventario_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.inventario_servicio.exception.ReglaNegocioException;
import com.foodfast.inventario_servicio.exception.StockInsuficienteException;
import com.foodfast.inventario_servicio.model.Inventario;
import com.foodfast.inventario_servicio.repository.InventarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioRepository inventarioRepository;

    @Transactional(readOnly = true)
    public List<InventarioResponse> listar() {
        log.info("Listando inventarios");
        return inventarioRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InventarioResponse> listarPorUbicacion(String ubicacion) {
        if (ubicacion == null || ubicacion.isBlank()) {
            throw new ReglaNegocioException("La ubicación de búsqueda es obligatoria");
        }

        return inventarioRepository.findByUbicacionContainingIgnoreCase(ubicacion)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InventarioResponse buscarPorId(Long id) {
        log.info("Buscando inventario id={}", id);
        return toResponse(obtenerEntidad(id));
    }

    @Transactional(readOnly = true)
    public InventarioResponse buscarPorProductoId(Long productoId) {
        log.info("Buscando inventario productoId={}", productoId);
        return toResponse(obtenerPorProductoId(productoId));
    }

    @Transactional(readOnly = true)
    public StockResponse consultarStock(Long productoId) {
        log.info("Consultando stock productoId={}", productoId);
        Inventario inventario = obtenerPorProductoId(productoId);
        Integer stock = calcularStockReal(inventario);
        return StockResponse.builder()
                .productoId(productoId)
                .stockDisponible(stock)
                .hayStock(stock > 0)
                .build();
    }

    @Transactional
    public InventarioResponse crear(InventarioRequest request) {
        validarRequest(request);

        if (inventarioRepository.existsByProductoId(request.getProductoId())) {
            throw new ReglaNegocioException("Ya existe inventario para el productoId: " + request.getProductoId());
        }

        Inventario inventario = Inventario.builder().build();
        copiarDatos(request, inventario);

        Inventario guardado = inventarioRepository.save(inventario);
        log.info("Inventario creado id={} productoId={} stockReal={}", guardado.getId(), guardado.getProductoId(), calcularStockReal(guardado));
        return toResponse(guardado);
    }

    @Transactional
    public InventarioResponse actualizar(Long id, InventarioRequest request) {
        validarRequest(request);
        Inventario inventario = obtenerEntidad(id);

        inventarioRepository.findByProductoId(request.getProductoId()).ifPresent(encontrado -> {
            if (!encontrado.getId().equals(id)) {
                throw new ReglaNegocioException("Otro registro ya usa el productoId: " + request.getProductoId());
            }
        });

        copiarDatos(request, inventario);

        Inventario actualizado = inventarioRepository.save(inventario);
        log.info("Inventario actualizado id={} productoId={} stockReal={}", actualizado.getId(), actualizado.getProductoId(), calcularStockReal(actualizado));
        return toResponse(actualizado);
    }

    @Transactional
    public InventarioResponse descontarStock(DescontarStockRequest request) {
        if (request == null) {
            throw new ReglaNegocioException("La solicitud para descontar stock es obligatoria");
        }
        validarId(request.getProductoId(), "productoId");
        if (request.getCantidad() == null || request.getCantidad() <= 0) {
            throw new ReglaNegocioException("La cantidad a descontar debe ser mayor que cero");
        }

        Inventario inventario = obtenerPorProductoId(request.getProductoId());
        Integer stockActual = calcularStockReal(inventario);

        if (stockActual < request.getCantidad()) {
            log.warn("Stock insuficiente productoId={} disponible={} solicitado={}", request.getProductoId(), stockActual, request.getCantidad());
            throw new StockInsuficienteException("Stock insuficiente. Disponible: " + stockActual + ", solicitado: " + request.getCantidad());
        }

        inventario.setCantidadDisponible(inventario.getCantidadDisponible() - request.getCantidad());
        validarStock(inventario.getCantidadDisponible(), inventario.getCantidadReservada());

        Inventario actualizado = inventarioRepository.save(inventario);
        log.info("Stock descontado productoId={} cantidad={} stockRealFinal={}", request.getProductoId(), request.getCantidad(), calcularStockReal(actualizado));
        return toResponse(actualizado);
    }

    @Transactional
    public InventarioResponse reservarStock(DescontarStockRequest request) {
        if (request == null) {
            throw new ReglaNegocioException("La solicitud para reservar stock es obligatoria");
        }
        validarId(request.getProductoId(), "productoId");
        if (request.getCantidad() == null || request.getCantidad() <= 0) {
            throw new ReglaNegocioException("La cantidad a reservar debe ser mayor que cero");
        }

        Inventario inventario = obtenerPorProductoId(request.getProductoId());
        Integer stockActual = calcularStockReal(inventario);
        if (stockActual < request.getCantidad()) {
            throw new StockInsuficienteException("No hay stock suficiente para reservar. Disponible: " + stockActual);
        }

        inventario.setCantidadReservada(inventario.getCantidadReservada() + request.getCantidad());
        validarStock(inventario.getCantidadDisponible(), inventario.getCantidadReservada());
        return toResponse(inventarioRepository.save(inventario));
    }

    @Transactional
    public InventarioResponse liberarReserva(DescontarStockRequest request) {
        if (request == null) {
            throw new ReglaNegocioException("La solicitud para liberar reserva es obligatoria");
        }
        validarId(request.getProductoId(), "productoId");
        if (request.getCantidad() == null || request.getCantidad() <= 0) {
            throw new ReglaNegocioException("La cantidad a liberar debe ser mayor que cero");
        }

        Inventario inventario = obtenerPorProductoId(request.getProductoId());
        if (inventario.getCantidadReservada() < request.getCantidad()) {
            throw new ReglaNegocioException("No se puede liberar más stock del reservado");
        }

        inventario.setCantidadReservada(inventario.getCantidadReservada() - request.getCantidad());
        return toResponse(inventarioRepository.save(inventario));
    }

    @Transactional
    public void eliminar(Long id) {
        Inventario inventario = obtenerEntidad(id);
        inventarioRepository.delete(inventario);
        log.info("Inventario eliminado id={}", id);
    }

    private Inventario obtenerEntidad(Long id) {
        validarId(id, "id");
        return inventarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Inventario no encontrado con id: " + id));
    }

    private Inventario obtenerPorProductoId(Long productoId) {
        validarId(productoId, "productoId");
        return inventarioRepository.findByProductoId(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe inventario para productoId: " + productoId));
    }

    private void copiarDatos(InventarioRequest request, Inventario inventario) {
        inventario.setProductoId(request.getProductoId());
        inventario.setCantidadDisponible(request.getCantidadDisponible());
        inventario.setCantidadReservada(request.getCantidadReservada());
        inventario.setUbicacion(request.getUbicacion().trim());
    }

    private void validarRequest(InventarioRequest request) {
        if (request == null) {
            throw new ReglaNegocioException("Los datos de inventario son obligatorios");
        }
        validarId(request.getProductoId(), "productoId");
        validarStock(request.getCantidadDisponible(), request.getCantidadReservada());
        if (request.getUbicacion() == null || request.getUbicacion().isBlank()) {
            throw new ReglaNegocioException("La ubicación es obligatoria");
        }
    }

    private void validarId(Long id, String campo) {
        if (id == null || id <= 0) {
            throw new ReglaNegocioException("El " + campo + " debe ser mayor que cero");
        }
    }

    private void validarStock(Integer disponible, Integer reservado) {
        if (disponible == null || reservado == null) {
            throw new ReglaNegocioException("La cantidad disponible y la cantidad reservada son obligatorias");
        }
        if (disponible < 0 || reservado < 0) {
            throw new ReglaNegocioException("El stock no puede ser negativo");
        }
        if (reservado > disponible) {
            throw new ReglaNegocioException("La cantidad reservada no puede superar la cantidad disponible");
        }
    }

    private Integer calcularStockReal(Inventario inventario) {
        return inventario.getCantidadDisponible() - inventario.getCantidadReservada();
    }

    private InventarioResponse toResponse(Inventario inventario) {
        return InventarioResponse.builder()
                .id(inventario.getId())
                .productoId(inventario.getProductoId())
                .cantidadDisponible(inventario.getCantidadDisponible())
                .cantidadReservada(inventario.getCantidadReservada())
                .stockReal(calcularStockReal(inventario))
                .ubicacion(inventario.getUbicacion())
                .build();
    }
}
