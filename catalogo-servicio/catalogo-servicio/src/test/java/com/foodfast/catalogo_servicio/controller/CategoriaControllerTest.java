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
import com.foodfast.catalogo_servicio.dto.CategoriaRequest;
import com.foodfast.catalogo_servicio.dto.CategoriaResponse;
import com.foodfast.catalogo_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.catalogo_servicio.security.JwtAuthenticationFilter;
import com.foodfast.catalogo_servicio.security.JwtService;
import com.foodfast.catalogo_servicio.service.CategoriaService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CategoriaController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoriaService categoriaService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void estado_debeRetornarOk() throws Exception {
        mockMvc.perform(get("/api/categorias/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exito").value(true))
                .andExpect(jsonPath("$.datos").value("OK"));
    }

    @Test
    void listar_debeRetornarOk() throws Exception {
        when(categoriaService.listar()).thenReturn(List.of(new CategoriaResponse(1L, "Bebidas", "Bebidas frías", true)));

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exito").value(true))
                .andExpect(jsonPath("$.datos[0].nombre").value("Bebidas"));
    }

    @Test
    void listarActivas_debeRetornarOk() throws Exception {
        when(categoriaService.listarActivas()).thenReturn(List.of(new CategoriaResponse(1L, "Bebidas", "Bebidas frías", true)));

        mockMvc.perform(get("/api/categorias/activas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos[0].activa").value(true));
    }

    @Test
    void buscarPorId_debeRetornarOk() throws Exception {
        when(categoriaService.buscarPorId(1L)).thenReturn(new CategoriaResponse(1L, "Bebidas", "Bebidas frías", true));

        mockMvc.perform(get("/api/categorias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.nombre").value("Bebidas"));
    }

    @Test
    void buscarPorId_debeRetornarNotFound() throws Exception {
        when(categoriaService.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("Categoría no encontrada"));

        mockMvc.perform(get("/api/categorias/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Categoría no encontrada"));
    }

    @Test
    void crear_debeRetornarCreated() throws Exception {
        CategoriaRequest request = new CategoriaRequest();
        request.setNombre("Postres");
        request.setDescripcion("Postres dulces");
        request.setActiva(true);

        when(categoriaService.crear(any(CategoriaRequest.class))).thenReturn(new CategoriaResponse(1L, "Postres", "Postres dulces", true));

        mockMvc.perform(post("/api/categorias")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.datos.nombre").value("Postres"));
    }

    @Test
    void crear_debeRetornarBadRequestCuandoBodyInvalido() throws Exception {
        CategoriaRequest request = new CategoriaRequest();
        request.setNombre("");
        request.setDescripcion("");
        request.setActiva(null);

        mockMvc.perform(post("/api/categorias")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Error de validación"));
    }

    @Test
    void actualizar_debeRetornarOk() throws Exception {
        CategoriaRequest request = new CategoriaRequest();
        request.setNombre("Postres");
        request.setDescripcion("Postres dulces");
        request.setActiva(true);

        when(categoriaService.actualizar(any(Long.class), any(CategoriaRequest.class))).thenReturn(new CategoriaResponse(1L, "Postres", "Postres dulces", true));

        mockMvc.perform(put("/api/categorias/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Categoría actualizada correctamente"));
    }

    @Test
    void cambiarEstado_debeRetornarOk() throws Exception {
        when(categoriaService.cambiarEstado(1L, false)).thenReturn(new CategoriaResponse(1L, "Postres", "Postres dulces", false));

        mockMvc.perform(patch("/api/categorias/1/estado").param("activa", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.activa").value(false));
    }

    @Test
    void eliminar_debeRetornarNoContent() throws Exception {
        mockMvc.perform(delete("/api/categorias/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminar_debeRetornarNotFound() throws Exception {
        doThrow(new RecursoNoEncontradoException("Categoría no encontrada")).when(categoriaService).eliminar(99L);

        mockMvc.perform(delete("/api/categorias/99"))
                .andExpect(status().isNotFound());
    }
}
