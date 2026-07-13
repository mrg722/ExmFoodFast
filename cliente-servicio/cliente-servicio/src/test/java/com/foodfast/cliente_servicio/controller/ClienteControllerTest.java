package com.foodfast.cliente_servicio.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodfast.cliente_servicio.dto.ClienteRequest;
import com.foodfast.cliente_servicio.dto.ClienteResponse;
import com.foodfast.cliente_servicio.dto.DireccionRequest;
import com.foodfast.cliente_servicio.dto.DireccionResponse;
import com.foodfast.cliente_servicio.dto.PedidoResumenResponse;
import com.foodfast.cliente_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.cliente_servicio.exception.ReglaNegocioException;
import com.foodfast.cliente_servicio.exception.ServicioExternoException;
import com.foodfast.cliente_servicio.service.ClienteService;
import com.foodfast.cliente_servicio.security.JwtAuthenticationFilter;
import com.foodfast.cliente_servicio.security.JwtService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ClienteController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteService clienteService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void estado_debeResponderOk() throws Exception {
        mockMvc.perform(get("/api/clientes/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("OK"));
    }

    @Test
    void listar_debeRetornarClientes() throws Exception {
        when(clienteService.listar()).thenReturn(List.of(responseValida()));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].email").value("damian@foodfast.cl"));
    }

    @Test
    void listarActivos_debeRetornarClientesActivos() throws Exception {
        when(clienteService.listarActivos()).thenReturn(List.of(responseValida()));

        mockMvc.perform(get("/api/clientes/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].activo").value(true));
    }

    @Test
    void buscarPorId_debeRetornarCliente() throws Exception {
        when(clienteService.buscarPorId(1L)).thenReturn(responseValida());

        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void buscarPorId_debeResponder404CuandoNoExiste() throws Exception {
        when(clienteService.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("Cliente no encontrado"));

        mockMvc.perform(get("/api/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente no encontrado"));
    }

    @Test
    void buscarPorEmail_debeRetornarCliente() throws Exception {
        when(clienteService.buscarPorEmail("damian@foodfast.cl")).thenReturn(responseValida());

        mockMvc.perform(get("/api/clientes/email/damian@foodfast.cl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("damian@foodfast.cl"));
    }

    @Test
    void listarPedidosDelCliente_debeRetornarPedidos() throws Exception {
        PedidoResumenResponse pedido = PedidoResumenResponse.builder()
                .id(10L)
                .clienteId(1L)
                .estado("CONFIRMADO")
                .total(BigDecimal.valueOf(15990))
                .build();
        when(clienteService.listarPedidosDelCliente(eq(1L), eq("Bearer token"))).thenReturn(List.of(pedido));

        mockMvc.perform(get("/api/clientes/1/pedidos").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(10));
    }

    @Test
    void listarPedidosDelCliente_debeResponder502CuandoPedidoServicioFalla() throws Exception {
        when(clienteService.listarPedidosDelCliente(eq(1L), any()))
                .thenThrow(new ServicioExternoException("No se pudo comunicar con pedido-servicio"));

        mockMvc.perform(get("/api/clientes/1/pedidos"))
                .andExpect(status().isBadGateway());
    }

    @Test
    void crear_debeResponder201() throws Exception {
        when(clienteService.crear(any(ClienteRequest.class))).thenReturn(responseValida());

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValida())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("damian@foodfast.cl"));
    }

    @Test
    void crear_debeResponder400CuandoRequestEsInvalido() throws Exception {
        ClienteRequest request = requestValida();
        request.setEmail("correo-malo");

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error de validacion"));
    }

    @Test
    void crear_debeResponder400CuandoReglaNegocioFalla() throws Exception {
        when(clienteService.crear(any(ClienteRequest.class)))
                .thenThrow(new ReglaNegocioException("Email duplicado"));

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValida())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email duplicado"));
    }

    @Test
    void actualizar_debeResponder200() throws Exception {
        when(clienteService.actualizar(eq(1L), any(ClienteRequest.class))).thenReturn(responseValida());

        mockMvc.perform(put("/api/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValida())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void activar_debeResponder200() throws Exception {
        when(clienteService.activar(1L)).thenReturn(responseValida());

        mockMvc.perform(put("/api/clientes/1/activar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cliente activado correctamente"));
    }

    @Test
    void desactivar_debeResponder200() throws Exception {
        ClienteResponse response = responseValida();
        response.setActivo(false);
        when(clienteService.desactivar(1L)).thenReturn(response);

        mockMvc.perform(put("/api/clientes/1/desactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activo").value(false));
    }

    @Test
    void eliminar_debeResponder204() throws Exception {
        doNothing().when(clienteService).eliminar(1L);

        mockMvc.perform(delete("/api/clientes/1"))
                .andExpect(status().isNoContent());
    }

    private ClienteRequest requestValida() {
        DireccionRequest direccion = DireccionRequest.builder()
                .calle("Av. Providencia")
                .numero("1234")
                .comuna("Providencia")
                .ciudad("Santiago")
                .referencia("Depto 501")
                .principal(true)
                .build();
        return ClienteRequest.builder()
                .nombre("Damian")
                .apellido("Galaz")
                .email("damian@foodfast.cl")
                .telefono("+56911112222")
                .direcciones(List.of(direccion))
                .build();
    }

    private ClienteResponse responseValida() {
        DireccionResponse direccion = DireccionResponse.builder()
                .id(5L)
                .calle("Av. Providencia")
                .numero("1234")
                .comuna("Providencia")
                .ciudad("Santiago")
                .referencia("Depto 501")
                .principal(true)
                .build();
        return ClienteResponse.builder()
                .id(1L)
                .nombre("Damian")
                .apellido("Galaz")
                .email("damian@foodfast.cl")
                .telefono("+56911112222")
                .activo(true)
                .direcciones(List.of(direccion))
                .build();
    }
}
