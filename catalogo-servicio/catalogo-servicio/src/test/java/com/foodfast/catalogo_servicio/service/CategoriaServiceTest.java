package com.foodfast.catalogo_servicio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.foodfast.catalogo_servicio.dto.CategoriaRequest;
import com.foodfast.catalogo_servicio.dto.CategoriaResponse;
import com.foodfast.catalogo_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.catalogo_servicio.exception.ReglaNegocioException;
import com.foodfast.catalogo_servicio.model.Categoria;
import com.foodfast.catalogo_servicio.repository.CategoriaRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoria;
    private CategoriaRequest request;

    @BeforeEach
    void setUp() {
        categoria = new Categoria(1L, "Hamburguesas", "Comida rápida", true);
        request = new CategoriaRequest();
        request.setNombre("Hamburguesas");
        request.setDescripcion("Comida rápida");
        request.setActiva(true);
    }

    @Test
    void listar_debeRetornarCategorias() {
        when(categoriaRepository.findAll()).thenReturn(List.of(categoria));

        List<CategoriaResponse> resultado = categoriaService.listar();

        assertEquals(1, resultado.size());
        assertEquals("Hamburguesas", resultado.get(0).getNombre());
        verify(categoriaRepository).findAll();
    }

    @Test
    void listarActivas_debeRetornarSoloActivas() {
        when(categoriaRepository.findByActivaTrue()).thenReturn(List.of(categoria));

        List<CategoriaResponse> resultado = categoriaService.listarActivas();

        assertEquals(1, resultado.size());
        assertEquals(true, resultado.get(0).getActiva());
    }

    @Test
    void buscarPorId_debeRetornarCategoria() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        CategoriaResponse resultado = categoriaService.buscarPorId(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("Hamburguesas", resultado.getNombre());
    }

    @Test
    void obtenerEntidad_debeLanzarErrorCuandoIdInvalido() {
        assertThrows(ReglaNegocioException.class, () -> categoriaService.obtenerEntidad(0L));
        assertThrows(ReglaNegocioException.class, () -> categoriaService.obtenerEntidad(null));
    }

    @Test
    void buscarPorId_debeLanzarErrorCuandoNoExiste() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> categoriaService.buscarPorId(99L));
    }

    @Test
    void crear_debeGuardarCategoriaCuandoNombreNoExiste() {
        when(categoriaRepository.existsByNombreIgnoreCase("Hamburguesas")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        CategoriaResponse resultado = categoriaService.crear(request);

        assertNotNull(resultado);
        assertEquals("Hamburguesas", resultado.getNombre());
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    void crear_debeLanzarErrorCuandoNombreDuplicado() {
        when(categoriaRepository.existsByNombreIgnoreCase("Hamburguesas")).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> categoriaService.crear(request));

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    void crear_debeLanzarErrorCuandoRequestInvalido() {
        CategoriaRequest invalido = new CategoriaRequest();
        invalido.setNombre(" ");
        invalido.setDescripcion("Texto");
        invalido.setActiva(true);

        assertThrows(ReglaNegocioException.class, () -> categoriaService.crear(invalido));
        assertThrows(ReglaNegocioException.class, () -> categoriaService.crear(null));
    }

    @Test
    void actualizar_debeActualizarCategoriaSinDuplicarMismoNombre() {
        CategoriaRequest actualizado = new CategoriaRequest();
        actualizado.setNombre("Hamburguesas Premium");
        actualizado.setDescripcion("Nueva descripción");
        actualizado.setActiva(true);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombreIgnoreCase("Hamburguesas Premium")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoriaResponse resultado = categoriaService.actualizar(1L, actualizado);

        assertEquals("Hamburguesas Premium", resultado.getNombre());
        assertEquals("Nueva descripción", resultado.getDescripcion());
    }

    @Test
    void actualizar_debePermitirMismoNombre() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoriaResponse resultado = categoriaService.actualizar(1L, request);

        assertEquals("Hamburguesas", resultado.getNombre());
    }

    @Test
    void cambiarEstado_debeDesactivarCategoria() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoriaResponse resultado = categoriaService.cambiarEstado(1L, false);

        assertFalse(resultado.getActiva());
    }

    @Test
    void eliminar_debeEliminarCategoria() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        categoriaService.eliminar(1L);

        verify(categoriaRepository).delete(categoria);
    }
}
