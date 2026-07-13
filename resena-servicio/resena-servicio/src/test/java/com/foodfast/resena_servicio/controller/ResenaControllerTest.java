package com.foodfast.resena_servicio.controller;

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
import com.foodfast.resena_servicio.dto.PromedioResenaResponse;
import com.foodfast.resena_servicio.dto.ResenaRequest;
import com.foodfast.resena_servicio.dto.ResenaResponse;
import com.foodfast.resena_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.resena_servicio.exception.ReglaNegocioException;
import com.foodfast.resena_servicio.exception.ServicioExternoException;
import com.foodfast.resena_servicio.service.ResenaService;
import java.time.LocalDateTime;
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
import com.foodfast.resena_servicio.exception.GlobalExceptionHandler;
import com.foodfast.resena_servicio.security.JwtAuthenticationFilter;
import com.foodfast.resena_servicio.security.JwtService;

@WebMvcTest(ResenaController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class ResenaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResenaService resenaService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void estadoDebeResponderOk() throws Exception {
        mockMvc.perform(get("/api/resenas/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("OK"));
    }

    @Test
    void debeListarTodasYActivas() throws Exception {
        when(resenaService.listar()).thenReturn(List.of(response()));
        when(resenaService.listarActivas()).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/resenas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));

        mockMvc.perform(get("/api/resenas/activas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].activa").value(true));
    }

    @Test
    void debeBuscarPorIdProductoClienteYPromedio() throws Exception {
        when(resenaService.buscarPorId(1L)).thenReturn(response());
        when(resenaService.listarPorProducto(10L)).thenReturn(List.of(response()));
        when(resenaService.listarPorCliente(1L)).thenReturn(List.of(response()));
        when(resenaService.promedioPorProducto(10L)).thenReturn(PromedioResenaResponse.builder()
                .productoId(10L).promedio(4.5).totalResenas(2L).build());

        mockMvc.perform(get("/api/resenas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));

        mockMvc.perform(get("/api/resenas/producto/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].productoId").value(10));

        mockMvc.perform(get("/api/resenas/cliente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].clienteId").value(1));

        mockMvc.perform(get("/api/resenas/producto/10/promedio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.promedio").value(4.5));
    }

    @Test
    void debeCrearActualizarDesactivarYEliminar() throws Exception {
        when(resenaService.crear(any(ResenaRequest.class))).thenReturn(response());
        when(resenaService.actualizar(eq(1L), any(ResenaRequest.class))).thenReturn(response());
        when(resenaService.desactivar(1L)).thenReturn(ResenaResponse.builder()
                .id(1L).clienteId(1L).productoId(10L).activa(false).calificacion(4).comentario("OK").build());
        doNothing().when(resenaService).eliminar(1L);

        mockMvc.perform(post("/api/resenas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1));

        mockMvc.perform(put("/api/resenas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(put("/api/resenas/1/desactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activa").value(false));

        mockMvc.perform(delete("/api/resenas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void debeResponderBadRequestCuandoBodyEsInvalido() throws Exception {
        ResenaRequest request = ResenaRequest.builder()
                .clienteId(0L)
                .productoId(null)
                .calificacion(6)
                .comentario("x")
                .build();

        mockMvc.perform(post("/api/resenas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error de validacion"));
    }

    @Test
    void debeResponderErroresControlados() throws Exception {
        when(resenaService.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("No existe"));
        when(resenaService.listarPorProducto(0L)).thenThrow(new ReglaNegocioException("ID inválido"));
        when(resenaService.crear(any(ResenaRequest.class))).thenThrow(new ServicioExternoException("Catalogo no responde"));

        mockMvc.perform(get("/api/resenas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe"));

        mockMvc.perform(get("/api/resenas/producto/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ID inválido"));

        mockMvc.perform(post("/api/resenas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Catalogo no responde"));
    }

    private ResenaRequest request() {
        return ResenaRequest.builder()
                .clienteId(1L)
                .productoId(10L)
                .restauranteId(2L)
                .calificacion(5)
                .comentario("Muy bueno")
                .build();
    }

    private ResenaResponse response() {
        return ResenaResponse.builder()
                .id(1L)
                .clienteId(1L)
                .productoId(10L)
                .restauranteId(2L)
                .calificacion(5)
                .comentario("Muy bueno")
                .activa(true)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }
}
