package com.foodfast.restaurante_servicio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.foodfast.restaurante_servicio.dto.HorarioRestauranteRequest;
import com.foodfast.restaurante_servicio.dto.HorarioRestauranteResponse;
import com.foodfast.restaurante_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.restaurante_servicio.service.HorarioRestauranteService;
import com.foodfast.restaurante_servicio.security.JwtAuthenticationFilter;
import com.foodfast.restaurante_servicio.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HorarioRestauranteController.class)
@AutoConfigureMockMvc(addFilters = false)
class HorarioRestauranteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockBean
    private HorarioRestauranteService horarioService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void listarPorRestauranteDebeResponderOk() throws Exception {
        when(horarioService.listarPorRestaurante(1L)).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/horarios-restaurante/restaurante/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].diaSemana").value("LUNES"));
    }

    @Test
    void buscarPorIdDebeResponderOk() throws Exception {
        when(horarioService.buscarPorId(1L)).thenReturn(response());

        mockMvc.perform(get("/api/horarios-restaurante/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void buscarNoExistenteDebeResponder404() throws Exception {
        when(horarioService.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("No encontrado"));

        mockMvc.perform(get("/api/horarios-restaurante/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearDebeResponderCreated() throws Exception {
        when(horarioService.crear(any(HorarioRestauranteRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/horarios-restaurante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void crearInvalidoDebeResponder400() throws Exception {
        HorarioRestauranteRequest request = request();
        request.setDiaSemana("");

        mockMvc.perform(post("/api/horarios-restaurante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarDebeResponderOk() throws Exception {
        when(horarioService.actualizar(eq(1L), any(HorarioRestauranteRequest.class))).thenReturn(response());

        mockMvc.perform(put("/api/horarios-restaurante/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk());
    }

    @Test
    void eliminarDebeResponderNoContent() throws Exception {
        doNothing().when(horarioService).eliminar(1L);

        mockMvc.perform(delete("/api/horarios-restaurante/1"))
                .andExpect(status().isNoContent());
    }

    private HorarioRestauranteRequest request() {
        HorarioRestauranteRequest request = new HorarioRestauranteRequest();
        request.setRestauranteId(1L);
        request.setDiaSemana("LUNES");
        request.setHoraApertura(LocalTime.of(10, 0));
        request.setHoraCierre(LocalTime.of(22, 0));
        return request;
    }

    private HorarioRestauranteResponse response() {
        return HorarioRestauranteResponse.builder()
                .id(1L)
                .restauranteId(1L)
                .diaSemana("LUNES")
                .horaApertura(LocalTime.of(10, 0))
                .horaCierre(LocalTime.of(22, 0))
                .build();
    }
}
