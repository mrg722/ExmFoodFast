package com.foodfast.reparto_servicio.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.foodfast.reparto_servicio.dto.CambiarEstadoEntregaRequest;
import com.foodfast.reparto_servicio.dto.EntregaRequest;
import com.foodfast.reparto_servicio.dto.EntregaResponse;
import com.foodfast.reparto_servicio.exception.GlobalExceptionHandler;
import com.foodfast.reparto_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.reparto_servicio.exception.ReglaNegocioException;
import com.foodfast.reparto_servicio.exception.ServicioExternoException;
import com.foodfast.reparto_servicio.model.EstadoEntrega;
import com.foodfast.reparto_servicio.security.JwtAuthenticationFilter;
import com.foodfast.reparto_servicio.security.JwtService;
import com.foodfast.reparto_servicio.service.EntregaService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = EntregaController.class)
@Import(GlobalExceptionHandler.class)
class EntregaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EntregaService entregaService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void estadoDebeResponderOk() throws Exception {
        mockMvc.perform(get("/api/entregas/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("OK"));
    }

    @Test
    void listarDebeResponderOk() throws Exception {
        when(entregaService.listar()).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/entregas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].pedidoId").value(100));
    }

    @Test
    void buscarPorIdDebeResponderOk() throws Exception {
        when(entregaService.buscarPorId(1L)).thenReturn(response());

        mockMvc.perform(get("/api/entregas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void buscarPorIdDebeResponder404SiNoExiste() throws Exception {
        when(entregaService.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("No existe"));

        mockMvc.perform(get("/api/entregas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe"));
    }

    @Test
    void listarPorPedidoDebeResponderOk() throws Exception {
        when(entregaService.listarPorPedido(100L)).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/entregas/pedido/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].pedidoId").value(100));
    }

    @Test
    void listarPorRepartidorDebeResponderOk() throws Exception {
        when(entregaService.listarPorRepartidor(1L)).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/entregas/repartidor/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].repartidorId").value(1));
    }

    @Test
    void crearDebeResponderCreated() throws Exception {
        when(entregaService.crear(any(EntregaRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/entregas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Entrega creada correctamente"));
    }

    @Test
    void crearDebeResponder400SiBodyInvalido() throws Exception {
        EntregaRequest request = request();
        request.setDireccionEntrega("");

        mockMvc.perform(post("/api/entregas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validations.direccionEntrega").exists());
    }

    @Test
    void crearDebeResponder503SiPedidoServicioFalla() throws Exception {
        when(entregaService.crear(any(EntregaRequest.class)))
                .thenThrow(new ServicioExternoException("pedido no disponible"));

        mockMvc.perform(post("/api/entregas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("pedido no disponible"));
    }

    @Test
    void actualizarDebeResponderOk() throws Exception {
        when(entregaService.actualizar(eq(1L), any(EntregaRequest.class))).thenReturn(response());

        mockMvc.perform(put("/api/entregas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void asignarRepartidorDebeResponderOk() throws Exception {
        when(entregaService.asignarRepartidor(1L, 1L)).thenReturn(response());

        mockMvc.perform(patch("/api/entregas/1/asignar/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Repartidor asignado correctamente"));
    }

    @Test
    void cambiarEstadoDebeResponderOk() throws Exception {
        EntregaResponse response = response();
        response.setEstadoEntrega(EstadoEntrega.EN_CAMINO);
        when(entregaService.cambiarEstado(eq(1L), any(CambiarEstadoEntregaRequest.class))).thenReturn(response);

        CambiarEstadoEntregaRequest request = new CambiarEstadoEntregaRequest();
        request.setEstadoEntrega(EstadoEntrega.EN_CAMINO);

        mockMvc.perform(patch("/api/entregas/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoEntrega").value("EN_CAMINO"));
    }

    @Test
    void cambiarEstadoDebeResponder400SiEstadoEsNulo() throws Exception {
        mockMvc.perform(patch("/api/entregas/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validations.estadoEntrega").exists());
    }

    @Test
    void eliminarDebeResponderNoContent() throws Exception {
        mockMvc.perform(delete("/api/entregas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarDebeResponder400SiReglaNegocioFalla() throws Exception {
        doThrow(new ReglaNegocioException("No se puede eliminar")).when(entregaService).eliminar(1L);

        mockMvc.perform(delete("/api/entregas/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No se puede eliminar"));
    }

    private EntregaRequest request() {
        EntregaRequest request = new EntregaRequest();
        request.setPedidoId(100L);
        request.setRepartidorId(1L);
        request.setDireccionEntrega("Av. FoodFast 123");
        return request;
    }

    private EntregaResponse response() {
        return EntregaResponse.builder()
                .id(1L)
                .pedidoId(100L)
                .repartidorId(1L)
                .repartidorNombre("Juan Perez")
                .direccionEntrega("Av. FoodFast 123")
                .estadoEntrega(EstadoEntrega.ASIGNADA)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }
}
