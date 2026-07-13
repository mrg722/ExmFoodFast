package com.foodfast.notificacion_servicio.controller;

import static org.hamcrest.Matchers.hasSize;
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
import com.foodfast.notificacion_servicio.dto.NotificacionPedidoRequest;
import com.foodfast.notificacion_servicio.dto.NotificacionRequest;
import com.foodfast.notificacion_servicio.dto.NotificacionResponse;
import com.foodfast.notificacion_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.notificacion_servicio.exception.ReglaNegocioException;
import com.foodfast.notificacion_servicio.exception.ServicioExternoException;
import com.foodfast.notificacion_servicio.security.JwtAuthenticationFilter;
import com.foodfast.notificacion_servicio.security.JwtService;
import com.foodfast.notificacion_servicio.model.CanalNotificacion;
import com.foodfast.notificacion_servicio.model.EstadoNotificacion;
import com.foodfast.notificacion_servicio.model.TipoNotificacion;
import com.foodfast.notificacion_servicio.service.NotificacionService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificacionController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificacionService service;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void estadoDebeResponderOk() throws Exception {
        mockMvc.perform(get("/api/notificaciones/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("OK"));
    }

    @Test
    void debeListarNotificaciones() throws Exception {
        when(service.listar()).thenReturn(List.of(response(1L), response(2L)));

        mockMvc.perform(get("/api/notificaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void debeBuscarPorId() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(response(1L));

        mockMvc.perform(get("/api/notificaciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void debeResponder404CuandoNoExiste() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("No existe"));

        mockMvc.perform(get("/api/notificaciones/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe"));
    }

    @Test
    void debeListarPorClienteEstadoYClienteEstado() throws Exception {
        when(service.listarPorCliente(10L)).thenReturn(List.of(response(1L)));
        when(service.listarPorEstado(EstadoNotificacion.PENDIENTE)).thenReturn(List.of(response(1L)));
        when(service.listarPorClienteYEstado(10L, EstadoNotificacion.PENDIENTE)).thenReturn(List.of(response(1L)));

        mockMvc.perform(get("/api/notificaciones/cliente/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));

        mockMvc.perform(get("/api/notificaciones/estado/PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));

        mockMvc.perform(get("/api/notificaciones/cliente/10/estado/PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void debeCrearNotificacionManual() throws Exception {
        when(service.crear(any(NotificacionRequest.class))).thenReturn(response(1L));

        mockMvc.perform(post("/api/notificaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBase())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void debeValidarBodyAlCrear() throws Exception {
        NotificacionRequest request = requestBase();
        request.setClienteId(null);
        request.setTitulo("");

        mockMvc.perform(post("/api/notificaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.clienteId").exists())
                .andExpect(jsonPath("$.validationErrors.titulo").exists());
    }

    @Test
    void debeCrearNotificacionParaPedido() throws Exception {
        when(service.crearParaPedido(any(NotificacionPedidoRequest.class))).thenReturn(response(1L));
        NotificacionPedidoRequest request = NotificacionPedidoRequest.builder()
                .pedidoId(50L)
                .clienteId(10L)
                .tipo(TipoNotificacion.PEDIDO_EN_REPARTO)
                .canal(CanalNotificacion.PUSH)
                .build();

        mockMvc.perform(post("/api/notificaciones/pedido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void debeActualizarEnviarLeerRegistrarErrorYEliminar() throws Exception {
        when(service.actualizar(eq(1L), any(NotificacionRequest.class))).thenReturn(response(1L));
        when(service.enviar(1L)).thenReturn(response(1L, EstadoNotificacion.ENVIADA));
        when(service.marcarComoLeida(1L)).thenReturn(response(1L, EstadoNotificacion.LEIDA));
        when(service.registrarErrorEnvio(1L, "SMTP")).thenReturn(response(1L, EstadoNotificacion.ERROR));
        doNothing().when(service).eliminar(1L);

        mockMvc.perform(put("/api/notificaciones/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBase())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));

        mockMvc.perform(put("/api/notificaciones/1/enviar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("ENVIADA"));

        mockMvc.perform(put("/api/notificaciones/1/leer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("LEIDA"));

        mockMvc.perform(put("/api/notificaciones/1/error").param("motivo", "SMTP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("ERROR"));

        mockMvc.perform(delete("/api/notificaciones/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void debeResponder400Y503SegunExcepcionDelService() throws Exception {
        when(service.enviar(1L)).thenThrow(new ReglaNegocioException("Ya enviada"));
        when(service.crearParaPedido(any(NotificacionPedidoRequest.class))).thenThrow(new ServicioExternoException("Pedido no disponible"));

        mockMvc.perform(put("/api/notificaciones/1/enviar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ya enviada"));

        NotificacionPedidoRequest request = NotificacionPedidoRequest.builder()
                .pedidoId(50L)
                .clienteId(10L)
                .tipo(TipoNotificacion.SISTEMA)
                .canal(CanalNotificacion.SISTEMA)
                .build();

        mockMvc.perform(post("/api/notificaciones/pedido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Pedido no disponible"));
    }

    private NotificacionRequest requestBase() {
        return NotificacionRequest.builder()
                .clienteId(10L)
                .tipo(TipoNotificacion.PEDIDO_CONFIRMADO)
                .canal(CanalNotificacion.EMAIL)
                .titulo("Pedido confirmado")
                .mensaje("Tu pedido fue confirmado")
                .referenciaTipo("PEDIDO")
                .referenciaId(100L)
                .build();
    }

    private NotificacionResponse response(Long id) {
        return response(id, EstadoNotificacion.PENDIENTE);
    }

    private NotificacionResponse response(Long id, EstadoNotificacion estado) {
        return NotificacionResponse.builder()
                .id(id)
                .clienteId(10L)
                .tipo(TipoNotificacion.PEDIDO_CONFIRMADO)
                .canal(CanalNotificacion.EMAIL)
                .estado(estado)
                .titulo("Pedido confirmado")
                .mensaje("Tu pedido fue confirmado")
                .referenciaTipo("PEDIDO")
                .referenciaId(100L)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }
}
