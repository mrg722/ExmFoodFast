package com.foodfast.restaurante_servicio.service;

import com.foodfast.restaurante_servicio.dto.HorarioRestauranteRequest;
import com.foodfast.restaurante_servicio.dto.HorarioRestauranteResponse;
import com.foodfast.restaurante_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.restaurante_servicio.exception.ReglaNegocioException;
import com.foodfast.restaurante_servicio.model.HorarioRestaurante;
import com.foodfast.restaurante_servicio.model.Restaurante;
import com.foodfast.restaurante_servicio.repository.HorarioRestauranteRepository;
import com.foodfast.restaurante_servicio.repository.RestauranteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HorarioRestauranteServiceTest {

    @Mock
    private HorarioRestauranteRepository horarioRepository;

    @Mock
    private RestauranteRepository restauranteRepository;

    @InjectMocks
    private HorarioRestauranteService horarioService;

    @Test
    void debeCrearHorarioValido() {
        HorarioRestauranteRequest request = requestValido();
        Restaurante restaurante = restaurante();
        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(restaurante));
        when(horarioRepository.existsByRestauranteIdAndDiaSemanaIgnoreCase(1L, "LUNES")).thenReturn(false);
        when(horarioRepository.save(any(HorarioRestaurante.class))).thenAnswer(invocation -> {
            HorarioRestaurante h = invocation.getArgument(0);
            h.setId(1L);
            return h;
        });

        HorarioRestauranteResponse response = horarioService.crear(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getDiaSemana()).isEqualTo("LUNES");
    }

    @Test
    void noDebeCrearHorarioSiCierreEsAntesDeApertura() {
        HorarioRestauranteRequest request = requestValido();
        request.setHoraCierre(LocalTime.of(8, 0));

        assertThatThrownBy(() -> horarioService.crear(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("cierre");
    }

    @Test
    void noDebeCrearHorarioDuplicado() {
        HorarioRestauranteRequest request = requestValido();
        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(restaurante()));
        when(horarioRepository.existsByRestauranteIdAndDiaSemanaIgnoreCase(1L, "LUNES")).thenReturn(true);

        assertThatThrownBy(() -> horarioService.crear(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya existe");
    }

    @Test
    void noDebeCrearHorarioSiRestauranteNoExiste() {
        HorarioRestauranteRequest request = requestValido();
        when(restauranteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> horarioService.crear(request))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void debeListarHorariosPorRestaurante() {
        when(horarioRepository.findByRestauranteId(1L)).thenReturn(List.of(horario()));

        List<HorarioRestauranteResponse> response = horarioService.listarPorRestaurante(1L);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getRestauranteId()).isEqualTo(1L);
    }

    @Test
    void debeBuscarHorarioPorId() {
        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horario()));

        HorarioRestauranteResponse response = horarioService.buscarPorId(1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void debeActualizarHorario() {
        HorarioRestauranteRequest request = requestValido();
        request.setDiaSemana("MARTES");
        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horario()));
        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(restaurante()));
        when(horarioRepository.existsByRestauranteIdAndDiaSemanaIgnoreCaseAndIdNot(1L, "MARTES", 1L)).thenReturn(false);
        when(horarioRepository.save(any(HorarioRestaurante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HorarioRestauranteResponse response = horarioService.actualizar(1L, request);

        assertThat(response.getDiaSemana()).isEqualTo("MARTES");
    }

    @Test
    void noDebeActualizarHorarioDuplicado() {
        HorarioRestauranteRequest request = requestValido();
        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horario()));
        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(restaurante()));
        when(horarioRepository.existsByRestauranteIdAndDiaSemanaIgnoreCaseAndIdNot(1L, "LUNES", 1L)).thenReturn(true);

        assertThatThrownBy(() -> horarioService.actualizar(1L, request))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void debeEliminarHorario() {
        HorarioRestaurante horario = horario();
        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horario));

        horarioService.eliminar(1L);

        verify(horarioRepository).delete(horario);
    }

    @Test
    void debeLanzarErrorSiHorarioNoExiste() {
        when(horarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> horarioService.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void noDebeAceptarIdInvalido() {
        assertThatThrownBy(() -> horarioService.buscarPorId(0L))
                .isInstanceOf(ReglaNegocioException.class);
    }

    private HorarioRestauranteRequest requestValido() {
        HorarioRestauranteRequest request = new HorarioRestauranteRequest();
        request.setRestauranteId(1L);
        request.setDiaSemana("LUNES");
        request.setHoraApertura(LocalTime.of(10, 0));
        request.setHoraCierre(LocalTime.of(22, 0));
        return request;
    }

    private Restaurante restaurante() {
        return Restaurante.builder()
                .id(1L)
                .nombre("FoodFast Centro")
                .direccion("Av. FoodFast 123")
                .telefono("+56911112222")
                .activo(true)
                .abierto(false)
                .build();
    }

    private HorarioRestaurante horario() {
        return HorarioRestaurante.builder()
                .id(1L)
                .restaurante(restaurante())
                .diaSemana("LUNES")
                .horaApertura(LocalTime.of(10, 0))
                .horaCierre(LocalTime.of(22, 0))
                .build();
    }
}
