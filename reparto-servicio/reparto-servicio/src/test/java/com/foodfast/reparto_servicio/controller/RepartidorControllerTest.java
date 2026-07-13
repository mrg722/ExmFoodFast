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
import com.foodfast.reparto_servicio.dto.RepartidorRequest;
import com.foodfast.reparto_servicio.dto.RepartidorResponse;
import com.foodfast.reparto_servicio.exception.GlobalExceptionHandler;
import com.foodfast.reparto_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.reparto_servicio.exception.ReglaNegocioException;
import com.foodfast.reparto_servicio.service.RepartidorService;
import com.foodfast.reparto_servicio.security.JwtAuthenticationFilter;
import com.foodfast.reparto_servicio.security.JwtService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = RepartidorController.class)
@Import(GlobalExceptionHandler.class)
class RepartidorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RepartidorService repartidorService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void estadoDebeResponderOk() throws Exception {
        mockMvc.perform(get("/api/repartidores/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("OK"));
    }

    @Test
    void listarDebeResponderOk() throws Exception {
        when(repartidorService.listar()).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/repartidores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].nombre").value("Juan Perez"));
    }

    @Test
    void buscarPorIdDebeResponderOk() throws Exception {
        when(repartidorService.buscarPorId(1L)).thenReturn(response());

        mockMvc.perform(get("/api/repartidores/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void buscarPorIdDebeResponder404SiNoExiste() throws Exception {
        when(repartidorService.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("No existe"));

        mockMvc.perform(get("/api/repartidores/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe"));
    }

    @Test
    void listarDisponiblesDebeResponderOk() throws Exception {
        when(repartidorService.listarDisponibles()).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/repartidores/disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].disponible").value(true));
    }

    @Test
    void crearDebeResponderCreated() throws Exception {
        when(repartidorService.crear(any(RepartidorRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/repartidores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Repartidor creado correctamente"));
    }

    @Test
    void crearDebeResponder400SiBodyInvalido() throws Exception {
        RepartidorRequest request = request();
        request.setNombre("");

        mockMvc.perform(post("/api/repartidores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validations.nombre").exists());
    }

    @Test
    void actualizarDebeResponderOk() throws Exception {
        when(repartidorService.actualizar(eq(1L), any(RepartidorRequest.class))).thenReturn(response());

        mockMvc.perform(put("/api/repartidores/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void cambiarDisponibilidadDebeResponderOk() throws Exception {
        when(repartidorService.cambiarDisponibilidad(1L, false))
                .thenReturn(RepartidorResponse.builder().id(1L).disponible(false).build());

        mockMvc.perform(patch("/api/repartidores/1/disponibilidad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("disponible", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.disponible").value(false));
    }

    @Test
    void cambiarDisponibilidadDebeResponder400PorReglaNegocio() throws Exception {
        when(repartidorService.cambiarDisponibilidad(1L, true))
                .thenThrow(new ReglaNegocioException("No se puede"));

        mockMvc.perform(patch("/api/repartidores/1/disponibilidad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("disponible", true))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No se puede"));
    }

    @Test
    void desactivarDebeResponderOk() throws Exception {
        when(repartidorService.desactivar(1L)).thenReturn(RepartidorResponse.builder().id(1L).activo(false).disponible(false).build());

        mockMvc.perform(patch("/api/repartidores/1/desactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activo").value(false));
    }

    @Test
    void eliminarDebeResponderNoContent() throws Exception {
        mockMvc.perform(delete("/api/repartidores/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarDebeResponder400SiServiceFalla() throws Exception {
        doThrow(new ReglaNegocioException("Tiene entregas")).when(repartidorService).eliminar(1L);

        mockMvc.perform(delete("/api/repartidores/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tiene entregas"));
    }

    private RepartidorRequest request() {
        RepartidorRequest request = new RepartidorRequest();
        request.setNombre("Juan Perez");
        request.setTelefono("+56911112222");
        request.setVehiculo("Moto");
        request.setActivo(true);
        request.setDisponible(true);
        return request;
    }

    private RepartidorResponse response() {
        return RepartidorResponse.builder()
                .id(1L)
                .nombre("Juan Perez")
                .telefono("+56911112222")
                .vehiculo("Moto")
                .activo(true)
                .disponible(true)
                .build();
    }
}
