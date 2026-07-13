package com.foodfast.restaurante_servicio.service;

import com.foodfast.restaurante_servicio.client.NotificacionClient;
import com.foodfast.restaurante_servicio.dto.RestauranteRequest;
import com.foodfast.restaurante_servicio.dto.RestauranteResponse;
import com.foodfast.restaurante_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.restaurante_servicio.exception.ReglaNegocioException;
import com.foodfast.restaurante_servicio.model.HorarioRestaurante;
import com.foodfast.restaurante_servicio.model.Restaurante;
import com.foodfast.restaurante_servicio.repository.RestauranteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestauranteServiceTest {

    @Mock
    private RestauranteRepository restauranteRepository;

    @Mock
    private NotificacionClient notificacionClient;

    @InjectMocks
    private RestauranteService restauranteService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(restauranteService, "notificacionEnabled", false);
    }

    @Test
    void debeCrearRestauranteCuandoNombreEsUnico() {
        RestauranteRequest request = requestValido("FoodFast Centro", true, false);
        when(restauranteRepository.existsByNombreIgnoreCase("FoodFast Centro")).thenReturn(false);
        when(restauranteRepository.save(any(Restaurante.class))).thenAnswer(invocation -> {
            Restaurante r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });

        RestauranteResponse response = restauranteService.crear(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNombre()).isEqualTo("FoodFast Centro");
        assertThat(response.getActivo()).isTrue();
        verify(restauranteRepository).save(any(Restaurante.class));
    }

    @Test
    void noDebeCrearRestauranteConNombreDuplicado() {
        RestauranteRequest request = requestValido("FoodFast Centro", true, false);
        when(restauranteRepository.existsByNombreIgnoreCase("FoodFast Centro")).thenReturn(true);

        assertThatThrownBy(() -> restauranteService.crear(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya existe");
        verify(restauranteRepository, never()).save(any());
    }

    @Test
    void noDebeCrearRestauranteInactivoYAbierto() {
        RestauranteRequest request = requestValido("FoodFast Norte", false, true);
        when(restauranteRepository.existsByNombreIgnoreCase("FoodFast Norte")).thenReturn(false);

        assertThatThrownBy(() -> restauranteService.crear(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("inactivo");
    }

    @Test
    void debeListarRestaurantesConHorarios() {
        Restaurante restaurante = restaurante(1L, "FoodFast Centro", true, true);
        HorarioRestaurante horario = HorarioRestaurante.builder()
                .id(10L)
                .restaurante(restaurante)
                .diaSemana("LUNES")
                .horaApertura(LocalTime.of(10, 0))
                .horaCierre(LocalTime.of(22, 0))
                .build();
        restaurante.setHorarios(List.of(horario));
        when(restauranteRepository.findAll()).thenReturn(List.of(restaurante));

        List<RestauranteResponse> responses = restauranteService.listar();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getHorarios()).hasSize(1);
    }

    @Test
    void debeListarActivosYAbiertos() {
        when(restauranteRepository.findByActivoTrue()).thenReturn(List.of(restaurante(1L, "Activo", true, false)));
        when(restauranteRepository.findByAbiertoTrue()).thenReturn(List.of(restaurante(2L, "Abierto", true, true)));

        assertThat(restauranteService.listarActivos()).hasSize(1);
        assertThat(restauranteService.listarAbiertos()).hasSize(1);
    }

    @Test
    void debeBuscarPorId() {
        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(restaurante(1L, "FoodFast", true, false)));

        RestauranteResponse response = restauranteService.buscarPorId(1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void debeLanzarErrorCuandoRestauranteNoExiste() {
        when(restauranteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restauranteService.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void debeActualizarRestaurante() {
        Restaurante existente = restaurante(1L, "Antiguo", true, false);
        RestauranteRequest request = requestValido("Nuevo", true, true);
        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(restauranteRepository.existsByNombreIgnoreCaseAndIdNot("Nuevo", 1L)).thenReturn(false);
        when(restauranteRepository.save(any(Restaurante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RestauranteResponse response = restauranteService.actualizar(1L, request);

        assertThat(response.getNombre()).isEqualTo("Nuevo");
        assertThat(response.getAbierto()).isTrue();
    }

    @Test
    void noDebeActualizarConNombreDuplicado() {
        Restaurante existente = restaurante(1L, "Antiguo", true, false);
        RestauranteRequest request = requestValido("Nuevo", true, false);
        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(restauranteRepository.existsByNombreIgnoreCaseAndIdNot("Nuevo", 1L)).thenReturn(true);

        assertThatThrownBy(() -> restauranteService.actualizar(1L, request))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void debeCambiarActivoYCerrarSiSeDesactiva() {
        Restaurante existente = restaurante(1L, "FoodFast", true, true);
        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(restauranteRepository.save(any(Restaurante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RestauranteResponse response = restauranteService.cambiarActivo(1L, false);

        assertThat(response.getActivo()).isFalse();
        assertThat(response.getAbierto()).isFalse();
    }

    @Test
    void debeNotificarCuandoCambiarActivoYIntegracionEstaActiva() {
        ReflectionTestUtils.setField(restauranteService, "notificacionEnabled", true);
        Restaurante existente = restaurante(1L, "FoodFast", true, false);
        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(restauranteRepository.save(any(Restaurante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        restauranteService.cambiarActivo(1L, true);

        verify(notificacionClient).crearNotificacionRestaurante(eq(1L), anyString(), anyString());
    }

    @Test
    void noDebeAbrirRestauranteInactivo() {
        Restaurante existente = restaurante(1L, "FoodFast", false, false);
        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> restauranteService.cambiarEstadoAbierto(1L, true))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void debeEliminarRestaurante() {
        Restaurante existente = restaurante(1L, "FoodFast", true, false);
        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(existente));

        restauranteService.eliminar(1L);

        verify(restauranteRepository).delete(existente);
    }

    @Test
    void noDebeAceptarIdInvalido() {
        assertThatThrownBy(() -> restauranteService.buscarPorId(0L))
                .isInstanceOf(ReglaNegocioException.class);
    }

    private RestauranteRequest requestValido(String nombre, boolean activo, boolean abierto) {
        RestauranteRequest request = new RestauranteRequest();
        request.setNombre(nombre);
        request.setDescripcion("Descripción");
        request.setDireccion("Av. FoodFast 123");
        request.setTelefono("+56911112222");
        request.setEmail("restaurante@foodfast.cl");
        request.setActivo(activo);
        request.setAbierto(abierto);
        return request;
    }

    private Restaurante restaurante(Long id, String nombre, boolean activo, boolean abierto) {
        return Restaurante.builder()
                .id(id)
                .nombre(nombre)
                .descripcion("Descripción")
                .direccion("Av. FoodFast 123")
                .telefono("+56911112222")
                .email("restaurante@foodfast.cl")
                .activo(activo)
                .abierto(abierto)
                .build();
    }
}
