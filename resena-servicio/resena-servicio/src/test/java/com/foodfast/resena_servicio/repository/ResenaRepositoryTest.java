package com.foodfast.resena_servicio.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.foodfast.resena_servicio.model.Resena;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ResenaRepositoryTest {

    @Autowired
    private ResenaRepository resenaRepository;

    @Test
    void debeGuardarBuscarPorProductoClienteYPromedio() {
        Resena guardada = resenaRepository.save(entidad(1L, 10L, true, 5));
        resenaRepository.save(entidad(2L, 10L, true, 3));
        resenaRepository.save(entidad(3L, 11L, false, 1));

        assertThat(guardada.getId()).isNotNull();
        assertThat(resenaRepository.findByProductoId(10L)).hasSize(2);
        assertThat(resenaRepository.findByClienteId(1L)).hasSize(1);
        assertThat(resenaRepository.findByActivaTrue()).hasSize(2);
        assertThat(resenaRepository.findByClienteIdAndProductoId(1L, 10L)).isPresent();
        assertThat(resenaRepository.existsByClienteIdAndProductoId(1L, 10L)).isTrue();
        assertThat(resenaRepository.countByProductoIdAndActivaTrue(10L)).isEqualTo(2L);
        assertThat(resenaRepository.promedioPorProducto(10L)).isEqualTo(4.0);
    }

    @Test
    void debeEliminarResena() {
        Resena guardada = resenaRepository.save(entidad(4L, 20L, true, 4));

        resenaRepository.delete(guardada);

        assertThat(resenaRepository.findById(guardada.getId())).isEmpty();
    }

    private Resena entidad(Long clienteId, Long productoId, boolean activa, int calificacion) {
        return Resena.builder()
                .clienteId(clienteId)
                .productoId(productoId)
                .restauranteId(2L)
                .calificacion(calificacion)
                .comentario("Comentario de prueba")
                .activa(activa)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }
}
