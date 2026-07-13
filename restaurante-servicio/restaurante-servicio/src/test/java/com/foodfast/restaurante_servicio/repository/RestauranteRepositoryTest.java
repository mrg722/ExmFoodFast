package com.foodfast.restaurante_servicio.repository;

import com.foodfast.restaurante_servicio.model.HorarioRestaurante;
import com.foodfast.restaurante_servicio.model.Restaurante;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RestauranteRepositoryTest {

    @Autowired
    private RestauranteRepository restauranteRepository;

    @Autowired
    private HorarioRestauranteRepository horarioRepository;

    @Test
    void debeGuardarYBuscarRestaurante() {
        Restaurante guardado = restauranteRepository.save(restaurante("FoodFast Centro", true, false));

        assertThat(restauranteRepository.findById(guardado.getId())).isPresent();
        assertThat(restauranteRepository.existsByNombreIgnoreCase("foodfast centro")).isTrue();
    }

    @Test
    void debeBuscarActivosYAbiertos() {
        restauranteRepository.save(restaurante("Activo", true, false));
        restauranteRepository.save(restaurante("Abierto", true, true));
        restauranteRepository.save(restaurante("Inactivo", false, false));

        assertThat(restauranteRepository.findByActivoTrue()).hasSize(2);
        assertThat(restauranteRepository.findByAbiertoTrue()).hasSize(1);
    }

    @Test
    void debeValidarNombreDuplicadoExcluyendoIdActual() {
        Restaurante guardado = restauranteRepository.save(restaurante("FoodFast Centro", true, false));
        restauranteRepository.save(restaurante("FoodFast Norte", true, false));

        assertThat(restauranteRepository.existsByNombreIgnoreCaseAndIdNot("FoodFast Norte", guardado.getId())).isTrue();
        assertThat(restauranteRepository.existsByNombreIgnoreCaseAndIdNot("FoodFast Centro", guardado.getId())).isFalse();
    }

    @Test
    void debeGuardarYBuscarHorarioPorRestaurante() {
        Restaurante restaurante = restauranteRepository.save(restaurante("FoodFast Centro", true, false));
        HorarioRestaurante horario = HorarioRestaurante.builder()
                .restaurante(restaurante)
                .diaSemana("LUNES")
                .horaApertura(LocalTime.of(10, 0))
                .horaCierre(LocalTime.of(22, 0))
                .build();
        horarioRepository.save(horario);

        assertThat(horarioRepository.findByRestauranteId(restaurante.getId())).hasSize(1);
        assertThat(horarioRepository.existsByRestauranteIdAndDiaSemanaIgnoreCase(restaurante.getId(), "lunes")).isTrue();
    }

    @Test
    void debeDetectarHorarioDuplicadoExcluyendoId() {
        Restaurante restaurante = restauranteRepository.save(restaurante("FoodFast Centro", true, false));
        HorarioRestaurante horario = horarioRepository.save(HorarioRestaurante.builder()
                .restaurante(restaurante)
                .diaSemana("LUNES")
                .horaApertura(LocalTime.of(10, 0))
                .horaCierre(LocalTime.of(22, 0))
                .build());

        assertThat(horarioRepository.existsByRestauranteIdAndDiaSemanaIgnoreCaseAndIdNot(restaurante.getId(), "LUNES", horario.getId())).isFalse();
    }

    @Test
    void debeEliminarRestaurante() {
        Restaurante guardado = restauranteRepository.save(restaurante("Eliminar", true, false));

        restauranteRepository.delete(guardado);

        assertThat(restauranteRepository.findById(guardado.getId())).isEmpty();
    }

    private Restaurante restaurante(String nombre, boolean activo, boolean abierto) {
        return Restaurante.builder()
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
