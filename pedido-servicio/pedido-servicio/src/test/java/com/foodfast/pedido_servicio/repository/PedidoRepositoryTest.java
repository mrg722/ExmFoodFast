package com.foodfast.pedido_servicio.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.foodfast.pedido_servicio.model.EstadoPedido;
import com.foodfast.pedido_servicio.model.Pedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class PedidoRepositoryTest {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Test
    void debeGuardarBuscarListarYEliminarPedidoConH2() {
        // Given
        Pedido pedido = Pedido.builder()
                .clienteId(1L)
                .productoId(72L)
                .cantidad(2)
                .fechaCreacion(LocalDateTime.now())
                .estado(EstadoPedido.CONFIRMADO)
                .total(new BigDecimal("15990"))
                .direccionEntrega("Av. FoodFast 123")
                .observacion("Sin cebolla")
                .build();

        // When
        Pedido guardado = pedidoRepository.save(pedido);
        List<Pedido> pedidosCliente = pedidoRepository.findByClienteId(1L);

        // Then
        assertThat(guardado.getId()).isNotNull();
        assertThat(pedidosCliente).hasSize(1);
        assertThat(pedidoRepository.findById(guardado.getId())).isPresent();

        pedidoRepository.delete(guardado);
        assertThat(pedidoRepository.findById(guardado.getId())).isEmpty();
    }
}
