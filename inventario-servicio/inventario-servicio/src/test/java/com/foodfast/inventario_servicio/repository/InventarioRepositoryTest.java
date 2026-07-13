package com.foodfast.inventario_servicio.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.foodfast.inventario_servicio.model.Inventario;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class InventarioRepositoryTest {

    @Autowired
    private InventarioRepository inventarioRepository;

    @Test
    void debeGuardarYBuscarPorProductoId() {
        // Given
        Inventario inventario = Inventario.builder()
                .productoId(100L)
                .cantidadDisponible(30)
                .cantidadReservada(5)
                .ubicacion("Bodega Central A1")
                .build();

        // When
        inventarioRepository.save(inventario);
        Optional<Inventario> resultado = inventarioRepository.findByProductoId(100L);

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getCantidadDisponible()).isEqualTo(30);
        assertThat(resultado.get().getCantidadReservada()).isEqualTo(5);
    }

    @Test
    void debeValidarExistenciaPorProductoId() {
        // Given
        inventarioRepository.save(Inventario.builder()
                .productoId(200L)
                .cantidadDisponible(12)
                .cantidadReservada(0)
                .ubicacion("Bodega Norte")
                .build());

        // When / Then
        assertThat(inventarioRepository.existsByProductoId(200L)).isTrue();
        assertThat(inventarioRepository.existsByProductoId(201L)).isFalse();
    }

    @Test
    void debeBuscarPorUbicacionIgnorandoMayusculas() {
        // Given
        inventarioRepository.save(Inventario.builder()
                .productoId(300L)
                .cantidadDisponible(40)
                .cantidadReservada(10)
                .ubicacion("Bodega Sur")
                .build());

        // When
        List<Inventario> resultado = inventarioRepository.findByUbicacionContainingIgnoreCase("sur");

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getProductoId()).isEqualTo(300L);
    }

    @Test
    void debeEliminarInventario() {
        // Given
        Inventario guardado = inventarioRepository.save(Inventario.builder()
                .productoId(400L)
                .cantidadDisponible(8)
                .cantidadReservada(1)
                .ubicacion("Bodega Temporal")
                .build());

        // When
        inventarioRepository.delete(guardado);

        // Then
        assertThat(inventarioRepository.findById(guardado.getId())).isEmpty();
    }
}
