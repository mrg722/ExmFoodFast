package com.foodfast.catalogo_servicio.service;

import com.foodfast.catalogo_servicio.client.InventarioClient;
import com.foodfast.catalogo_servicio.dto.InventarioStockResponse;
import com.foodfast.catalogo_servicio.dto.ProductoRequest;
import com.foodfast.catalogo_servicio.dto.ProductoResponse;
import com.foodfast.catalogo_servicio.dto.ProductoStockResponse;
import com.foodfast.catalogo_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.catalogo_servicio.exception.ReglaNegocioException;
import com.foodfast.catalogo_servicio.model.Categoria;
import com.foodfast.catalogo_servicio.model.Producto;
import com.foodfast.catalogo_servicio.repository.ProductoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaService categoriaService;
    private final InventarioClient inventarioClient;

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar() {
        return productoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarDisponibles() {
        return productoRepository.findByDisponibleTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarPorCategoria(Long categoriaId) {
        validarId(categoriaId, "categoriaId");
        categoriaService.obtenerEntidad(categoriaId);
        return productoRepository.findByCategoriaId(categoriaId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarDisponiblesPorCategoria(Long categoriaId) {
        validarId(categoriaId, "categoriaId");
        categoriaService.obtenerEntidad(categoriaId);
        return productoRepository.findByCategoriaIdAndDisponibleTrue(categoriaId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse buscarPorId(Long id) {
        return toResponse(obtenerEntidad(id));
    }

    @Transactional(readOnly = true)
    public ProductoStockResponse buscarProductoConStock(Long id) {
        ProductoResponse producto = buscarPorId(id);
        InventarioStockResponse stock = inventarioClient.consultarStock(producto.getId());
        String mensaje = inventarioClient.isIntegrationEnabled()
                ? "Stock consultado desde inventario-servicio"
                : "Integración con inventario desactivada por configuración";
        return new ProductoStockResponse(producto, stock, mensaje);
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        validarRequest(request);
        validarNombreDuplicado(request.getNombre());

        Categoria categoria = categoriaService.obtenerEntidad(request.getCategoriaId());
        validarCategoriaActiva(categoria);

        Producto producto = new Producto();
        copiarDatos(request, producto, categoria);

        Producto guardado = productoRepository.save(producto);
        log.info("Producto creado id={} nombre={} categoriaId={}", guardado.getId(), guardado.getNombre(), categoria.getId());
        return toResponse(guardado);
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        validarRequest(request);
        Producto producto = obtenerEntidad(id);

        if (!producto.getNombre().equalsIgnoreCase(request.getNombre().trim())) {
            validarNombreDuplicado(request.getNombre());
        }

        Categoria categoria = categoriaService.obtenerEntidad(request.getCategoriaId());
        validarCategoriaActiva(categoria);

        copiarDatos(request, producto, categoria);
        Producto actualizado = productoRepository.save(producto);
        log.info("Producto actualizado id={}", actualizado.getId());
        return toResponse(actualizado);
    }

    @Transactional
    public ProductoResponse cambiarDisponibilidad(Long id, boolean disponible) {
        Producto producto = obtenerEntidad(id);
        producto.setDisponible(disponible);
        Producto actualizado = productoRepository.save(producto);
        log.info("Producto id={} disponible={}", id, disponible);
        return toResponse(actualizado);
    }

    @Transactional
    public void eliminar(Long id) {
        Producto producto = obtenerEntidad(id);
        productoRepository.delete(producto);
        log.info("Producto eliminado id={}", id);
    }

    private Producto obtenerEntidad(Long id) {
        validarId(id, "id");
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id: " + id));
    }

    private void validarId(Long id, String campo) {
        if (id == null || id <= 0) {
            throw new ReglaNegocioException("El campo " + campo + " debe ser mayor que cero");
        }
    }

    private void validarRequest(ProductoRequest request) {
        if (request == null) {
            throw new ReglaNegocioException("La solicitud de producto es obligatoria");
        }
        if (request.getNombre() == null || request.getNombre().trim().isBlank()) {
            throw new ReglaNegocioException("El nombre del producto es obligatorio");
        }
        if (request.getDescripcion() == null || request.getDescripcion().trim().isBlank()) {
            throw new ReglaNegocioException("La descripción del producto es obligatoria");
        }
        if (request.getPrecio() == null || request.getPrecio().signum() <= 0) {
            throw new ReglaNegocioException("El precio debe ser mayor que cero");
        }
        if (request.getDisponible() == null) {
            throw new ReglaNegocioException("El estado disponible es obligatorio");
        }
        validarId(request.getCategoriaId(), "categoriaId");
    }

    private void validarNombreDuplicado(String nombre) {
        if (productoRepository.existsByNombreIgnoreCase(nombre.trim())) {
            throw new ReglaNegocioException("Ya existe un producto con el nombre: " + nombre);
        }
    }

    private void validarCategoriaActiva(Categoria categoria) {
        if (Boolean.FALSE.equals(categoria.getActiva())) {
            throw new ReglaNegocioException("No se puede asociar un producto a una categoría inactiva");
        }
    }

    private void copiarDatos(ProductoRequest request, Producto producto, Categoria categoria) {
        producto.setNombre(request.getNombre().trim());
        producto.setDescripcion(request.getDescripcion().trim());
        producto.setPrecio(request.getPrecio());
        producto.setDisponible(request.getDisponible());
        producto.setCategoria(categoria);
    }

    private ProductoResponse toResponse(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getDisponible(),
                producto.getCategoria().getId(),
                producto.getCategoria().getNombre()
        );
    }
}
