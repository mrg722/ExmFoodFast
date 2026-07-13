package com.foodfast.pedido_servicio.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodfast.pedido_servicio.dto.PedidoRequest;
import com.foodfast.pedido_servicio.dto.PedidoResponse;
import com.foodfast.pedido_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.pedido_servicio.exception.ReglaNegocioException;
import com.foodfast.pedido_servicio.exception.ServicioExternoException;
import com.foodfast.pedido_servicio.service.PedidoService;
import com.foodfast.pedido_servicio.security.JwtAuthenticationFilter;
import com.foodfast.pedido_servicio.security.JwtService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PedidoController.class)
@AutoConfigureMockMvc(addFilters = false)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PedidoService pedidoService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void estadoDebeResponderOk() throws Exception {
        mockMvc.perform(get("/api/pedidos/estado"))
                .andExpect(status().isOk());
    }

    @Test
    void listarDebeResponderApiResponse() throws Exception {
        when(pedidoService.listar()).thenReturn(List.of(responseBase()));

        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pedidos obtenidos correctamente"))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void buscarPorIdDebeResponderOk() throws Exception {
        when(pedidoService.buscarPorId(1L)).thenReturn(responseBase());

        mockMvc.perform(get("/api/pedidos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.clienteId").value(1));
    }

    @Test
    void buscarPorIdInexistenteDebeResponderNotFound() throws Exception {
        when(pedidoService.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("Pedido no encontrado con id: 99"));

        mockMvc.perform(get("/api/pedidos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Pedido no encontrado con id: 99"));
    }

    @Test
    void listarPorClienteDebeResponderOk() throws Exception {
        when(pedidoService.listarPorCliente(1L)).thenReturn(List.of(responseBase()));

        mockMvc.perform(get("/api/pedidos/cliente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Pedidos del cliente obtenidos correctamente"))
                .andExpect(jsonPath("$.data[0].clienteId").value(1));
    }

    @Test
    void crearDebeResponderCreated() throws Exception {
        when(pedidoService.crear(any(PedidoRequest.class), any())).thenReturn(responseBase());

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBase())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pedido creado correctamente"));
    }

    @Test
    void crearConBodyInvalidoDebeResponderBadRequest() throws Exception {
        PedidoRequest request = requestBase();
        request.setClienteId(null);

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validaciones.clienteId").exists());
    }

    @Test
    void crearConErrorInventarioDebeResponderServiceUnavailable() throws Exception {
        when(pedidoService.crear(any(PedidoRequest.class), any()))
                .thenThrow(new ServicioExternoException("No se pudo comunicar con inventario-servicio"));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBase())))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("No se pudo comunicar con inventario-servicio"));
    }

    @Test
    void actualizarDebeResponderOk() throws Exception {
        when(pedidoService.actualizar(eq(1L), any(PedidoRequest.class))).thenReturn(responseBase());

        mockMvc.perform(put("/api/pedidos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBase())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Pedido actualizado correctamente"));
    }

    @Test
    void actualizarConReglaNegocioDebeResponderBadRequest() throws Exception {
        when(pedidoService.actualizar(eq(1L), any(PedidoRequest.class)))
                .thenThrow(new ReglaNegocioException("No se puede actualizar un pedido cancelado"));

        mockMvc.perform(put("/api/pedidos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBase())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No se puede actualizar un pedido cancelado"));
    }

    @Test
    void cancelarDebeResponderNoContent() throws Exception {
        mockMvc.perform(put("/api/pedidos/1/cancelar"))
                .andExpect(status().isNoContent());

        verify(pedidoService).cancelar(1L);
    }

    @Test
    void cancelarConReglaNegocioDebeResponderBadRequest() throws Exception {
        doThrow(new ReglaNegocioException("El pedido ya se encuentra cancelado"))
                .when(pedidoService).cancelar(1L);

        mockMvc.perform(put("/api/pedidos/1/cancelar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El pedido ya se encuentra cancelado"));
    }

    @Test
    void eliminarDebeResponderNoContent() throws Exception {
        mockMvc.perform(delete("/api/pedidos/1"))
                .andExpect(status().isNoContent());

        verify(pedidoService).eliminar(1L);
    }

    private PedidoRequest requestBase() {
        PedidoRequest request = new PedidoRequest();
        request.setClienteId(1L);
        request.setProductoId(72L);
        request.setCantidad(2);
        request.setTotal(new BigDecimal("15990"));
        request.setDireccionEntrega("Av. FoodFast 123");
        request.setObservacion("Sin cebolla");
        return request;
    }

    private PedidoResponse responseBase() {
        return PedidoResponse.builder()
                .id(1L)
                .clienteId(1L)
                .productoId(72L)
                .cantidad(2)
                .fechaCreacion(LocalDateTime.now())
                .estado("CONFIRMADO")
                .total(new BigDecimal("15990"))
                .direccionEntrega("Av. FoodFast 123")
                .observacion("Sin cebolla")
                .estadoStock("STOCK DISPONIBLE")
                .mensajeInventario("Stock descontado correctamente en inventario-servicio")
                .build();
    }
}
