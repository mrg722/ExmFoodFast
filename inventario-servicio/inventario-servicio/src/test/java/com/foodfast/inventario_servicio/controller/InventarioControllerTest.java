package com.foodfast.inventario_servicio.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodfast.inventario_servicio.dto.DescontarStockRequest;
import com.foodfast.inventario_servicio.dto.InventarioRequest;
import com.foodfast.inventario_servicio.dto.InventarioResponse;
import com.foodfast.inventario_servicio.dto.StockResponse;
import com.foodfast.inventario_servicio.exception.GlobalExceptionHandler;
import com.foodfast.inventario_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.inventario_servicio.exception.StockInsuficienteException;
import com.foodfast.inventario_servicio.service.InventarioService;
import com.foodfast.inventario_servicio.security.JwtAuthenticationFilter;
import com.foodfast.inventario_servicio.security.JwtService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InventarioController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class InventarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventarioService inventarioService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void estadoDebeResponderOk() throws Exception {
        mockMvc.perform(get("/api/inventarios/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("OK"));
    }

    @Test
    void debeListarInventarios() throws Exception {
        when(inventarioService.listar()).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/inventarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].productoId").value(10));
    }

    @Test
    void debeBuscarPorUbicacion() throws Exception {
        when(inventarioService.listarPorUbicacion("central")).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/inventarios/buscar").param("ubicacion", "central"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].ubicacion").value("Bodega Central A1"));
    }

    @Test
    void debeBuscarPorId() throws Exception {
        when(inventarioService.buscarPorId(1L)).thenReturn(response());

        mockMvc.perform(get("/api/inventarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void debeResponder404SiNoExiste() throws Exception {
        when(inventarioService.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("No encontrado"));

        mockMvc.perform(get("/api/inventarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No encontrado"));
    }

    @Test
    void debeBuscarPorProductoId() throws Exception {
        when(inventarioService.buscarPorProductoId(10L)).thenReturn(response());

        mockMvc.perform(get("/api/inventarios/producto/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productoId").value(10));
    }

    @Test
    void debeConsultarStock() throws Exception {
        when(inventarioService.consultarStock(10L)).thenReturn(StockResponse.builder().productoId(10L).stockDisponible(45).hayStock(true).build());

        mockMvc.perform(get("/api/inventarios/producto/10/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stockDisponible").value(45))
                .andExpect(jsonPath("$.data.hayStock").value(true));
    }

    @Test
    void debeConsultarStockAlias() throws Exception {
        when(inventarioService.consultarStock(10L)).thenReturn(StockResponse.builder().productoId(10L).stockDisponible(45).hayStock(true).build());

        mockMvc.perform(get("/api/inventarios/stock/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stockDisponible").value(45));
    }

    @Test
    void debeCrearInventario() throws Exception {
        when(inventarioService.crear(any(InventarioRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/inventarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.productoId").value(10));
    }

    @Test
    void debeResponder400ConBodyInvalido() throws Exception {
        InventarioRequest invalido = InventarioRequest.builder().productoId(null).cantidadDisponible(-1).cantidadReservada(5).ubicacion("").build();

        mockMvc.perform(post("/api/inventarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validaciones.productoId").exists());
    }

    @Test
    void debeActualizarInventario() throws Exception {
        when(inventarioService.actualizar(any(Long.class), any(InventarioRequest.class))).thenReturn(response());

        mockMvc.perform(put("/api/inventarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Inventario actualizado correctamente"));
    }

    @Test
    void debeDescontarStock() throws Exception {
        when(inventarioService.descontarStock(any(DescontarStockRequest.class))).thenReturn(response());

        mockMvc.perform(put("/api/inventarios/descontar-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(descuento())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Stock descontado correctamente"));
    }

    @Test
    void debeDescontarStockAliasPost() throws Exception {
        when(inventarioService.descontarStock(any(DescontarStockRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/inventarios/descontar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(descuento())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stockReal").value(45));
    }

    @Test
    void debeResponder400CuandoStockEsInsuficiente() throws Exception {
        when(inventarioService.descontarStock(any())).thenThrow(new StockInsuficienteException("Stock insuficiente"));

        mockMvc.perform(post("/api/inventarios/descontar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(descuento())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Stock insuficiente"));
    }

    @Test
    void debeReservarStock() throws Exception {
        when(inventarioService.reservarStock(any(DescontarStockRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/inventarios/reservar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(descuento())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Stock reservado correctamente"));
    }

    @Test
    void debeLiberarReserva() throws Exception {
        when(inventarioService.liberarReserva(any(DescontarStockRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/inventarios/liberar-reserva")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(descuento())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reserva liberada correctamente"));
    }

    @Test
    void debeEliminarInventario() throws Exception {
        doNothing().when(inventarioService).eliminar(1L);

        mockMvc.perform(delete("/api/inventarios/1"))
                .andExpect(status().isNoContent());
    }

    private InventarioRequest request() {
        return InventarioRequest.builder()
                .productoId(10L)
                .cantidadDisponible(50)
                .cantidadReservada(5)
                .ubicacion("Bodega Central A1")
                .build();
    }

    private DescontarStockRequest descuento() {
        return DescontarStockRequest.builder()
                .productoId(10L)
                .cantidad(2)
                .build();
    }

    private InventarioResponse response() {
        return InventarioResponse.builder()
                .id(1L)
                .productoId(10L)
                .cantidadDisponible(50)
                .cantidadReservada(5)
                .stockReal(45)
                .ubicacion("Bodega Central A1")
                .build();
    }
}
