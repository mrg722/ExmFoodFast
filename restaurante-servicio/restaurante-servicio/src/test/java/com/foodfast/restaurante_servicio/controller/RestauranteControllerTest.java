package com.foodfast.restaurante_servicio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodfast.restaurante_servicio.dto.RestauranteRequest;
import com.foodfast.restaurante_servicio.dto.RestauranteResponse;
import com.foodfast.restaurante_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.restaurante_servicio.exception.ReglaNegocioException;
import com.foodfast.restaurante_servicio.service.RestauranteService;
import com.foodfast.restaurante_servicio.security.JwtAuthenticationFilter;
import com.foodfast.restaurante_servicio.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestauranteController.class)
@AutoConfigureMockMvc(addFilters = false)
class RestauranteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestauranteService restauranteService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void estadoDebeResponderOk() throws Exception {
        mockMvc.perform(get("/api/restaurantes/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("OK"));
    }

    @Test
    void listarDebeResponderOk() throws Exception {
        when(restauranteService.listar()).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/restaurantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void listarActivosYAbiertosDebeResponderOk() throws Exception {
        when(restauranteService.listarActivos()).thenReturn(List.of(response()));
        when(restauranteService.listarAbiertos()).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/restaurantes/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].activo").value(true));
        mockMvc.perform(get("/api/restaurantes/abiertos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].abierto").value(false));
    }

    @Test
    void buscarPorIdDebeResponderOk() throws Exception {
        when(restauranteService.buscarPorId(1L)).thenReturn(response());

        mockMvc.perform(get("/api/restaurantes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nombre").value("FoodFast Centro"));
    }

    @Test
    void buscarPorIdNoExistenteDebeResponder404() throws Exception {
        when(restauranteService.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("No encontrado"));

        mockMvc.perform(get("/api/restaurantes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearDebeResponderCreated() throws Exception {
        when(restauranteService.crear(any(RestauranteRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/restaurantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void crearInvalidoDebeResponder400() throws Exception {
        RestauranteRequest request = request();
        request.setNombre("");

        mockMvc.perform(post("/api/restaurantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarDebeResponderOk() throws Exception {
        when(restauranteService.actualizar(eq(1L), any(RestauranteRequest.class))).thenReturn(response());

        mockMvc.perform(put("/api/restaurantes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk());
    }

    @Test
    void cambiarActivoYEstadoAbiertoDebenResponderOk() throws Exception {
        when(restauranteService.cambiarActivo(1L, false)).thenReturn(response());
        when(restauranteService.cambiarEstadoAbierto(1L, true)).thenReturn(response());

        mockMvc.perform(patch("/api/restaurantes/1/activo").param("activo", "false"))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/restaurantes/1/estado-abierto").param("abierto", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void cambiarEstadoConReglaNegocioDebeResponder400() throws Exception {
        when(restauranteService.cambiarEstadoAbierto(1L, true)).thenThrow(new ReglaNegocioException("No se puede abrir"));

        mockMvc.perform(patch("/api/restaurantes/1/estado-abierto").param("abierto", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void eliminarDebeResponderNoContent() throws Exception {
        doNothing().when(restauranteService).eliminar(1L);

        mockMvc.perform(delete("/api/restaurantes/1"))
                .andExpect(status().isNoContent());
    }

    private RestauranteRequest request() {
        RestauranteRequest request = new RestauranteRequest();
        request.setNombre("FoodFast Centro");
        request.setDescripcion("Sucursal principal");
        request.setDireccion("Av. FoodFast 123");
        request.setTelefono("+56911112222");
        request.setEmail("centro@foodfast.cl");
        request.setActivo(true);
        request.setAbierto(false);
        return request;
    }

    private RestauranteResponse response() {
        return RestauranteResponse.builder()
                .id(1L)
                .nombre("FoodFast Centro")
                .descripcion("Sucursal principal")
                .direccion("Av. FoodFast 123")
                .telefono("+56911112222")
                .email("centro@foodfast.cl")
                .activo(true)
                .abierto(false)
                .horarios(List.of())
                .build();
    }
}
