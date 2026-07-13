package com.foodfast.reparto_servicio.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.foodfast.reparto_servicio.model.Entrega;
import com.foodfast.reparto_servicio.model.EstadoEntrega;
import com.foodfast.reparto_servicio.model.Repartidor;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class RepartoRepositoryTest {

    @Autowired
    private RepartidorRepository repartidorRepository;

    @Autowired
    private EntregaRepository entregaRepository;

    @Test
    void repartidorRepositoryDebeGuardarYBuscarDisponibles() {
        // Given
        Repartidor disponible = repartidor("Juan", true, true);
        Repartidor noDisponible = repartidor("Pedro", true, false);
        repartidorRepository.save(disponible);
        repartidorRepository.save(noDisponible);

        // When
        List<Repartidor> resultado = repartidorRepository.findByActivoTrueAndDisponibleTrue();

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getNombre()).isEqualTo("Juan");
    }

    @Test
    void entregaRepositoryDebeGuardarYBuscarPorPedidoYRepartidor() {
        // Given
        Repartidor repartidor = repartidorRepository.save(repartidor("Juan", true, true));
        Entrega entrega = entregaRepository.save(entrega(500L, repartidor));

        // When
        List<Entrega> porPedido = entregaRepository.findByPedidoId(500L);
        List<Entrega> porRepartidor = entregaRepository.findByRepartidorId(repartidor.getId());
        boolean existe = entregaRepository.existsByPedidoId(500L);

        // Then
        assertThat(entrega.getId()).isNotNull();
        assertThat(porPedido).hasSize(1);
        assertThat(porRepartidor).hasSize(1);
        assertThat(existe).isTrue();
    }

    @Test
    void entregaRepositoryDebeEliminarEntrega() {
        // Given
        Entrega guardada = entregaRepository.save(entrega(700L, null));

        // When
        entregaRepository.delete(guardada);

        // Then
        assertThat(entregaRepository.findById(guardada.getId())).isEmpty();
    }

    private Repartidor repartidor(String nombre, boolean activo, boolean disponible) {
        return Repartidor.builder()
                .nombre(nombre)
                .telefono("+56911112222")
                .vehiculo("Moto")
                .activo(activo)
                .disponible(disponible)
                .build();
    }

    private Entrega entrega(Long pedidoId, Repartidor repartidor) {
        return Entrega.builder()
                .pedidoId(pedidoId)
                .repartidor(repartidor)
                .direccionEntrega("Av. FoodFast 123")
                .estadoEntrega(repartidor == null ? EstadoEntrega.CREADA : EstadoEntrega.ASIGNADA)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }
}
