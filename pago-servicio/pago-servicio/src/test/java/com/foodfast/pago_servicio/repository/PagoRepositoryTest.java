package com.foodfast.pago_servicio.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.foodfast.pago_servicio.model.EstadoPago;
import com.foodfast.pago_servicio.model.MetodoPago;
import com.foodfast.pago_servicio.model.Pago;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class PagoRepositoryTest {

    @Autowired
    private PagoRepository pagoRepository;

    @Test
    void debeGuardarBuscarPorPedidoYVerificarExistencia() {
        Pago pago = Pago.builder()
                .pedidoId(99L)
                .monto(new BigDecimal("25000"))
                .metodoPago(MetodoPago.TRANSFERENCIA)
                .estadoPago(EstadoPago.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .build();

        Pago guardado = pagoRepository.save(pago);

        assertThat(guardado.getId()).isNotNull();
        assertThat(pagoRepository.existsByPedidoId(99L)).isTrue();
        assertThat(pagoRepository.findByPedidoId(99L)).hasSize(1);
        assertThat(pagoRepository.findById(guardado.getId())).isPresent();
    }

    @Test
    void debeEliminarPago() {
        Pago pago = Pago.builder()
                .pedidoId(100L)
                .monto(BigDecimal.TEN)
                .metodoPago(MetodoPago.EFECTIVO)
                .estadoPago(EstadoPago.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .build();

        Pago guardado = pagoRepository.save(pago);
        pagoRepository.delete(guardado);

        assertThat(pagoRepository.findById(guardado.getId())).isEmpty();
    }
}
