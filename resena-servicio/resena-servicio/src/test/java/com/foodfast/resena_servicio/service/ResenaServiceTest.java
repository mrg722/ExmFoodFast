package com.foodfast.resena_servicio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.foodfast.resena_servicio.client.CatalogoClient;
import com.foodfast.resena_servicio.dto.ProductoResponse;
import com.foodfast.resena_servicio.dto.PromedioResenaResponse;
import com.foodfast.resena_servicio.dto.ResenaRequest;
import com.foodfast.resena_servicio.dto.ResenaResponse;
import com.foodfast.resena_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.resena_servicio.exception.ReglaNegocioException;
import com.foodfast.resena_servicio.model.Resena;
import com.foodfast.resena_servicio.repository.ResenaRepository;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResenaServiceTest {

    @Mock
    private ResenaRepository resenaRepository;

    @Mock
    private CatalogoClient catalogoClient;

    @InjectMocks
    private ResenaService resenaService;

    @BeforeEach
    void setUp() throws Exception {
        setCatalogoIntegration(false);
    }

    @Test
    void debeCrearResenaCuandoNoExisteDuplicado() {
        ResenaRequest request = request();
        when(resenaRepository.existsByClienteIdAndProductoId(1L, 10L)).thenReturn(false);
        when(resenaRepository.save(any(Resena.class))).thenAnswer(invocation -> {
            Resena resena = invocation.getArgument(0);
            resena.setId(1L);
            return resena;
        });

        ResenaResponse response = resenaService.crear(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getActiva()).isTrue();
        assertThat(response.getComentario()).isEqualTo("Muy bueno");
        ArgumentCaptor<Resena> captor = ArgumentCaptor.forClass(Resena.class);
        verify(resenaRepository).save(captor.capture());
        assertThat(captor.getValue().getFechaCreacion()).isNotNull();
    }

    @Test
    void debeRechazarResenaDuplicada() {
        when(resenaRepository.existsByClienteIdAndProductoId(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> resenaService.crear(request()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya registró");

        verify(resenaRepository, never()).save(any());
    }

    @Test
    void debeValidarProductoActivoCuandoIntegracionCatalogoEstaActiva() throws Exception {
        setCatalogoIntegration(true);
        when(catalogoClient.buscarProducto(10L)).thenReturn(ProductoResponse.builder().id(10L).activo(true).build());
        when(resenaRepository.existsByClienteIdAndProductoId(1L, 10L)).thenReturn(false);
        when(resenaRepository.save(any(Resena.class))).thenAnswer(invocation -> {
            Resena resena = invocation.getArgument(0);
            resena.setId(3L);
            return resena;
        });

        ResenaResponse response = resenaService.crear(request());

        assertThat(response.getId()).isEqualTo(3L);
        verify(catalogoClient).buscarProducto(10L);
    }

    @Test
    void debeRechazarProductoInactivoCuandoIntegracionCatalogoEstaActiva() throws Exception {
        setCatalogoIntegration(true);
        when(catalogoClient.buscarProducto(10L)).thenReturn(ProductoResponse.builder().id(10L).activo(false).build());

        assertThatThrownBy(() -> resenaService.crear(request()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("producto inactivo");
    }

    @Test
    void debeListarYBuscarResenas() {
        Resena resena = entidad(1L, true);
        when(resenaRepository.findAll()).thenReturn(List.of(resena));
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(resena));

        assertThat(resenaService.listar()).hasSize(1);
        assertThat(resenaService.buscarPorId(1L).getClienteId()).isEqualTo(1L);
    }

    @Test
    void debeListarActivasPorProductoYPorCliente() {
        Resena resena = entidad(1L, true);
        when(resenaRepository.findByActivaTrue()).thenReturn(List.of(resena));
        when(resenaRepository.findByProductoId(10L)).thenReturn(List.of(resena));
        when(resenaRepository.findByClienteId(1L)).thenReturn(List.of(resena));

        assertThat(resenaService.listarActivas()).hasSize(1);
        assertThat(resenaService.listarPorProducto(10L)).hasSize(1);
        assertThat(resenaService.listarPorCliente(1L)).hasSize(1);
    }

    @Test
    void debeCalcularPromedioDeProducto() {
        when(resenaRepository.promedioPorProducto(10L)).thenReturn(4.5);
        when(resenaRepository.countByProductoIdAndActivaTrue(10L)).thenReturn(2L);

        PromedioResenaResponse response = resenaService.promedioPorProducto(10L);

        assertThat(response.getPromedio()).isEqualTo(4.5);
        assertThat(response.getTotalResenas()).isEqualTo(2L);
    }

    @Test
    void debeRetornarPromedioCeroCuandoNoHayResenas() {
        when(resenaRepository.promedioPorProducto(10L)).thenReturn(null);
        when(resenaRepository.countByProductoIdAndActivaTrue(10L)).thenReturn(0L);

        PromedioResenaResponse response = resenaService.promedioPorProducto(10L);

        assertThat(response.getPromedio()).isEqualTo(0.0);
        assertThat(response.getTotalResenas()).isZero();
    }

    @Test
    void debeActualizarResenaActiva() {
        Resena existente = entidad(1L, true);
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(resenaRepository.findByClienteIdAndProductoId(1L, 10L)).thenReturn(Optional.of(existente));
        when(resenaRepository.save(any(Resena.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResenaResponse response = resenaService.actualizar(1L, request());

        assertThat(response.getCalificacion()).isEqualTo(5);
        assertThat(response.getFechaActualizacion()).isNotNull();
    }

    @Test
    void debeRechazarActualizarSiOtroRegistroTieneMismoClienteYProducto() {
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(entidad(1L, true)));
        when(resenaRepository.findByClienteIdAndProductoId(1L, 10L)).thenReturn(Optional.of(entidad(2L, true)));

        assertThatThrownBy(() -> resenaService.actualizar(1L, request()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Otro registro");
    }

    @Test
    void debeRechazarActualizarResenaDesactivada() {
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(entidad(1L, false)));

        assertThatThrownBy(() -> resenaService.actualizar(1L, request()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("desactivada");
    }

    @Test
    void debeDesactivarResena() {
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(entidad(1L, true)));
        when(resenaRepository.save(any(Resena.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResenaResponse response = resenaService.desactivar(1L);

        assertThat(response.getActiva()).isFalse();
        assertThat(response.getFechaActualizacion()).isNotNull();
    }

    @Test
    void debeRechazarDesactivarDosVeces() {
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(entidad(1L, false)));

        assertThatThrownBy(() -> resenaService.desactivar(1L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya se encuentra");
    }

    @Test
    void debeEliminarResenaExistente() {
        Resena resena = entidad(1L, true);
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(resena));

        resenaService.eliminar(1L);

        verify(resenaRepository).delete(resena);
    }

    @Test
    void debeLanzarNotFoundSiNoExiste() {
        when(resenaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resenaService.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void debeValidarIdsInvalidosYComentarioCorto() {
        assertThatThrownBy(() -> resenaService.buscarPorId(0L)).isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> resenaService.listarPorCliente(-1L)).isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> resenaService.listarPorProducto(null)).isInstanceOf(ReglaNegocioException.class);
        ResenaRequest request = request();
        request.setComentario("  ");
        assertThatThrownBy(() -> resenaService.crear(request)).isInstanceOf(ReglaNegocioException.class);
    }

    private ResenaRequest request() {
        return ResenaRequest.builder()
                .clienteId(1L)
                .productoId(10L)
                .restauranteId(2L)
                .calificacion(5)
                .comentario("  Muy bueno  ")
                .build();
    }

    private Resena entidad(Long id, boolean activa) {
        return Resena.builder()
                .id(id)
                .clienteId(1L)
                .productoId(10L)
                .restauranteId(2L)
                .calificacion(4)
                .comentario("Comentario")
                .activa(activa)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    private void setCatalogoIntegration(boolean enabled) throws Exception {
        Field field = ResenaService.class.getDeclaredField("catalogoIntegrationEnabled");
        field.setAccessible(true);
        field.set(resenaService, enabled);
    }
}
