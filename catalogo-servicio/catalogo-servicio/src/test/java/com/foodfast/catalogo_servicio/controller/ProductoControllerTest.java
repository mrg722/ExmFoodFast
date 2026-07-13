package com.foodfast.catalogo_servicio.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodfast.catalogo_servicio.dto.InventarioStockResponse;
import com.foodfast.catalogo_servicio.dto.ProductoRequest;
import com.foodfast.catalogo_servicio.dto.ProductoResponse;
import com.foodfast.catalogo_servicio.dto.ProductoStockResponse;
import com.foodfast.catalogo_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.catalogo_servicio.security.JwtAuthenticationFilter;
import com.foodfast.catalogo_servicio.security.JwtService;
import com.foodfast.catalogo_servicio.service.ProductoService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductoService productoService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private ProductoResponse producto() {
        return new ProductoResponse(1L, "Hamburguesa", "Hamburguesa clásica", new BigDecimal("4990"), true, 1L, "Comida rápida");
    }

    private ProductoRequest request() {
        ProductoRequest request = new ProductoRequest();
        request.setNombre("Hamburguesa");
        request.setDescripcion("Hamburguesa clásica");
        request.setPrecio(new BigDecimal("4990"));
        request.setDisponible(true);
        request.setCategoriaId(1L);
        return request;
    }

    @Test
    void estado_debeRetornarOk() throws Exception {
        mockMvc.perform(get("/api/productos/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos").value("OK"));
    }

    @Test
    void listar_debeRetornarOk() throws Exception {
        when(productoService.listar()).thenReturn(List.of(producto()));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos[0].nombre").value("Hamburguesa"));
    }

    @Test
    void listarDisponibles_debeRetornarOk() throws Exception {
        when(productoService.listarDisponibles()).thenReturn(List.of(producto()));

        mockMvc.perform(get("/api/productos/disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos[0].disponible").value(true));
    }

    @Test
    void listarPorCategoria_debeRetornarOk() throws Exception {
        when(productoService.listarPorCategoria(1L)).thenReturn(List.of(producto()));

        mockMvc.perform(get("/api/productos/categoria/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos[0].categoriaId").value(1));
    }

    @Test
    void listarDisponiblesPorCategoria_debeRetornarOk() throws Exception {
        when(productoService.listarDisponiblesPorCategoria(1L)).thenReturn(List.of(producto()));

        mockMvc.perform(get("/api/productos/categoria/1/disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos[0].nombre").value("Hamburguesa"));
    }

    @Test
    void buscarPorId_debeRetornarOk() throws Exception {
        when(productoService.buscarPorId(1L)).thenReturn(producto());

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.nombre").value("Hamburguesa"));
    }

    @Test
    void buscarPorId_debeRetornarNotFound() throws Exception {
        when(productoService.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("Producto no encontrado"));

        mockMvc.perform(get("/api/productos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Producto no encontrado"));
    }

    @Test
    void buscarProductoConStock_debeRetornarOk() throws Exception {
        ProductoStockResponse response = new ProductoStockResponse(producto(), new InventarioStockResponse(1L, 10, 2, 8, true), "Stock consultado");
        when(productoService.buscarProductoConStock(1L)).thenReturn(response);

        mockMvc.perform(get("/api/productos/1/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.stock.stockReal").value(8));
    }

    @Test
    void crear_debeRetornarCreated() throws Exception {
        when(productoService.crear(any(ProductoRequest.class))).thenReturn(producto());

        mockMvc.perform(post("/api/productos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.datos.nombre").value("Hamburguesa"));
    }

    @Test
    void crear_debeRetornarBadRequestCuandoBodyInvalido() throws Exception {
        ProductoRequest invalido = new ProductoRequest();
        invalido.setNombre("");
        invalido.setDescripcion("");
        invalido.setPrecio(BigDecimal.ZERO);
        invalido.setDisponible(null);
        invalido.setCategoriaId(null);

        mockMvc.perform(post("/api/productos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Error de validación"));
    }

    @Test
    void actualizar_debeRetornarOk() throws Exception {
        when(productoService.actualizar(any(Long.class), any(ProductoRequest.class))).thenReturn(producto());

        mockMvc.perform(put("/api/productos/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Producto actualizado correctamente"));
    }

    @Test
    void cambiarDisponibilidad_debeRetornarOk() throws Exception {
        ProductoResponse response = producto();
        response.setDisponible(false);
        when(productoService.cambiarDisponibilidad(1L, false)).thenReturn(response);

        mockMvc.perform(patch("/api/productos/1/disponibilidad").param("disponible", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.disponible").value(false));
    }

    @Test
    void eliminar_debeRetornarNoContent() throws Exception {
        mockMvc.perform(delete("/api/productos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminar_debeRetornarNotFound() throws Exception {
        doThrow(new RecursoNoEncontradoException("Producto no encontrado")).when(productoService).eliminar(99L);

        mockMvc.perform(delete("/api/productos/99"))
                .andExpect(status().isNotFound());
    }
}
