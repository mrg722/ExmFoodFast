package com.foodfast.catalogo_servicio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private InventarioClient inventarioClient;

    @InjectMocks
    private ProductoService productoService;

    private Categoria categoria;
    private Producto producto;
    private ProductoRequest request;

    @BeforeEach
    void setUp() {
        categoria = new Categoria(1L, "Comida rápida", "Categoría activa", true);
        producto = new Producto(1L, "Hamburguesa", "Hamburguesa clásica", new BigDecimal("4990"), true, categoria);
        request = new ProductoRequest();
        request.setNombre("Hamburguesa");
        request.setDescripcion("Hamburguesa clásica");
        request.setPrecio(new BigDecimal("4990"));
        request.setDisponible(true);
        request.setCategoriaId(1L);
    }

    @Test
    void listar_debeRetornarProductos() {
        when(productoRepository.findAll()).thenReturn(List.of(producto));

        List<ProductoResponse> resultado = productoService.listar();

        assertEquals(1, resultado.size());
        assertEquals("Hamburguesa", resultado.get(0).getNombre());
    }

    @Test
    void listarDisponibles_debeRetornarDisponibles() {
        when(productoRepository.findByDisponibleTrue()).thenReturn(List.of(producto));

        List<ProductoResponse> resultado = productoService.listarDisponibles();

        assertEquals(1, resultado.size());
        assertEquals(true, resultado.get(0).getDisponible());
    }

    @Test
    void listarPorCategoria_debeValidarCategoriaYRetornarProductos() {
        when(categoriaService.obtenerEntidad(1L)).thenReturn(categoria);
        when(productoRepository.findByCategoriaId(1L)).thenReturn(List.of(producto));

        List<ProductoResponse> resultado = productoService.listarPorCategoria(1L);

        assertEquals(1, resultado.size());
        verify(categoriaService).obtenerEntidad(1L);
    }

    @Test
    void listarDisponiblesPorCategoria_debeRetornarProductos() {
        when(categoriaService.obtenerEntidad(1L)).thenReturn(categoria);
        when(productoRepository.findByCategoriaIdAndDisponibleTrue(1L)).thenReturn(List.of(producto));

        List<ProductoResponse> resultado = productoService.listarDisponiblesPorCategoria(1L);

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarPorId_debeRetornarProducto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        ProductoResponse resultado = productoService.buscarPorId(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("Comida rápida", resultado.getCategoriaNombre());
    }

    @Test
    void buscarPorId_debeLanzarErrorCuandoNoExiste() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> productoService.buscarPorId(99L));
    }

    @Test
    void buscarPorId_debeLanzarErrorCuandoIdInvalido() {
        assertThrows(ReglaNegocioException.class, () -> productoService.buscarPorId(0L));
        assertThrows(ReglaNegocioException.class, () -> productoService.buscarPorId(null));
    }

    @Test
    void buscarProductoConStock_debeConsultarInventario() {
        InventarioStockResponse stock = new InventarioStockResponse(1L, 10, 2, 8, true);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(inventarioClient.consultarStock(1L)).thenReturn(stock);
        when(inventarioClient.isIntegrationEnabled()).thenReturn(true);

        ProductoStockResponse resultado = productoService.buscarProductoConStock(1L);

        assertEquals(1L, resultado.getProducto().getId());
        assertEquals(8, resultado.getStock().getStockReal());
        assertEquals("Stock consultado desde inventario-servicio", resultado.getMensajeInventario());
    }

    @Test
    void crear_debeGuardarProductoCuandoCategoriaActiva() {
        when(productoRepository.existsByNombreIgnoreCase("Hamburguesa")).thenReturn(false);
        when(categoriaService.obtenerEntidad(1L)).thenReturn(categoria);
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        ProductoResponse resultado = productoService.crear(request);

        assertNotNull(resultado);
        assertEquals("Hamburguesa", resultado.getNombre());
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void crear_debeLanzarErrorCuandoProductoDuplicado() {
        when(productoRepository.existsByNombreIgnoreCase("Hamburguesa")).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> productoService.crear(request));
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void crear_debeLanzarErrorCuandoCategoriaInactiva() {
        Categoria inactiva = new Categoria(2L, "Antigua", "Sin uso", false);
        when(productoRepository.existsByNombreIgnoreCase("Hamburguesa")).thenReturn(false);
        when(categoriaService.obtenerEntidad(1L)).thenReturn(inactiva);

        assertThrows(ReglaNegocioException.class, () -> productoService.crear(request));
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void crear_debeLanzarErrorCuandoRequestInvalido() {
        ProductoRequest invalido = new ProductoRequest();
        invalido.setNombre(" ");
        invalido.setDescripcion("Producto");
        invalido.setPrecio(BigDecimal.ONE);
        invalido.setDisponible(true);
        invalido.setCategoriaId(1L);

        assertThrows(ReglaNegocioException.class, () -> productoService.crear(invalido));
        assertThrows(ReglaNegocioException.class, () -> productoService.crear(null));
    }

    @Test
    void crear_debeLanzarErrorCuandoPrecioInvalido() {
        request.setPrecio(BigDecimal.ZERO);

        assertThrows(ReglaNegocioException.class, () -> productoService.crear(request));
    }

    @Test
    void actualizar_debeActualizarProducto() {
        ProductoRequest actualizado = new ProductoRequest();
        actualizado.setNombre("Hamburguesa Doble");
        actualizado.setDescripcion("Doble carne");
        actualizado.setPrecio(new BigDecimal("6990"));
        actualizado.setDisponible(true);
        actualizado.setCategoriaId(1L);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.existsByNombreIgnoreCase("Hamburguesa Doble")).thenReturn(false);
        when(categoriaService.obtenerEntidad(1L)).thenReturn(categoria);
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoResponse resultado = productoService.actualizar(1L, actualizado);

        assertEquals("Hamburguesa Doble", resultado.getNombre());
        assertEquals(new BigDecimal("6990"), resultado.getPrecio());
    }

    @Test
    void actualizar_debePermitirMismoNombre() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(categoriaService.obtenerEntidad(1L)).thenReturn(categoria);
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoResponse resultado = productoService.actualizar(1L, request);

        assertEquals("Hamburguesa", resultado.getNombre());
    }

    @Test
    void cambiarDisponibilidad_debeActualizarDisponible() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoResponse resultado = productoService.cambiarDisponibilidad(1L, false);

        assertFalse(resultado.getDisponible());
    }

    @Test
    void eliminar_debeEliminarProducto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        productoService.eliminar(1L);

        verify(productoRepository).delete(producto);
    }
}
