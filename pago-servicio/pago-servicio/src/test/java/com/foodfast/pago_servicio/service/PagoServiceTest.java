package com.foodfast.pago_servicio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.foodfast.pago_servicio.client.PedidoClient;
import com.foodfast.pago_servicio.dto.ActualizarPagoRequest;
import com.foodfast.pago_servicio.dto.PagoRequest;
import com.foodfast.pago_servicio.dto.PagoResponse;
import com.foodfast.pago_servicio.dto.PedidoResponse;
import com.foodfast.pago_servicio.dto.ProcesarPagoRequest;
import com.foodfast.pago_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.pago_servicio.exception.ReglaNegocioException;
import com.foodfast.pago_servicio.model.EstadoPago;
import com.foodfast.pago_servicio.model.MetodoPago;
import com.foodfast.pago_servicio.model.Pago;
import com.foodfast.pago_servicio.repository.PagoRepository;
import java.math.BigDecimal;
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
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private PedidoClient pedidoClient;

    @InjectMocks
    private PagoService pagoService;

    private Pago pagoPendiente;

    @BeforeEach
    void setUp() {
        pagoPendiente = Pago.builder()
                .id(1L)
                .pedidoId(10L)
                .monto(new BigDecimal("15990"))
                .metodoPago(MetodoPago.WEBPAY_SIMULADO)
                .estadoPago(EstadoPago.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    @Test
    void crearDebeGuardarPagoPendienteCuandoPedidoEsValido() {
        PagoRequest request = new PagoRequest();
        request.setPedidoId(10L);
        request.setMonto(new BigDecimal("15990"));
        request.setMetodoPago(MetodoPago.WEBPAY_SIMULADO);

        when(pagoRepository.existsByPedidoId(10L)).thenReturn(false);
        when(pedidoClient.obtenerPedido(10L)).thenReturn(PedidoResponse.builder().id(10L).estado("CONFIRMADO").build());
        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoPendiente);

        PagoResponse response = pagoService.crear(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEstadoPago()).isEqualTo(EstadoPago.PENDIENTE);
        assertThat(response.getMensajePedido()).contains("Pedido validado");

        ArgumentCaptor<Pago> captor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(captor.capture());
        assertThat(captor.getValue().getEstadoPago()).isEqualTo(EstadoPago.PENDIENTE);
    }

    @Test
    void crearDebeRechazarPagoDuplicado() {
        PagoRequest request = new PagoRequest();
        request.setPedidoId(10L);
        request.setMonto(BigDecimal.TEN);
        request.setMetodoPago(MetodoPago.TARJETA);
        when(pagoRepository.existsByPedidoId(10L)).thenReturn(true);

        assertThatThrownBy(() -> pagoService.crear(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya existe");
    }

    @Test
    void crearDebeRechazarMontoInvalido() {
        PagoRequest request = new PagoRequest();
        request.setPedidoId(10L);
        request.setMonto(BigDecimal.ZERO);
        request.setMetodoPago(MetodoPago.TARJETA);

        assertThatThrownBy(() -> pagoService.crear(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("monto");
    }

    @Test
    void crearDebeRechazarPedidoCancelado() {
        PagoRequest request = new PagoRequest();
        request.setPedidoId(10L);
        request.setMonto(BigDecimal.TEN);
        request.setMetodoPago(MetodoPago.EFECTIVO);
        when(pagoRepository.existsByPedidoId(10L)).thenReturn(false);
        when(pedidoClient.obtenerPedido(10L)).thenReturn(PedidoResponse.builder().id(10L).estado("CANCELADO").build());

        assertThatThrownBy(() -> pagoService.crear(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("estado pagable");
    }

    @Test
    void listarYBuscarDebenRetornarResponses() {
        when(pagoRepository.findAll()).thenReturn(List.of(pagoPendiente));
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoPendiente));
        when(pagoRepository.findByPedidoId(10L)).thenReturn(List.of(pagoPendiente));

        assertThat(pagoService.listar()).hasSize(1);
        assertThat(pagoService.buscarPorId(1L).getPedidoId()).isEqualTo(10L);
        assertThat(pagoService.buscarPorPedidoId(10L)).hasSize(1);
    }

    @Test
    void buscarDebeLanzarCuandoNoExiste() {
        when(pagoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pagoService.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Pago no encontrado");
    }

    @Test
    void actualizarDebeModificarPagoPendiente() {
        ActualizarPagoRequest request = new ActualizarPagoRequest();
        request.setMonto(new BigDecimal("20000"));
        request.setMetodoPago(MetodoPago.TRANSFERENCIA);
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoPendiente));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PagoResponse response = pagoService.actualizar(1L, request);

        assertThat(response.getMonto()).isEqualByComparingTo("20000");
        assertThat(response.getMetodoPago()).isEqualTo(MetodoPago.TRANSFERENCIA);
    }

    @Test
    void actualizarDebeRechazarPagoNoPendiente() {
        pagoPendiente.setEstadoPago(EstadoPago.APROBADO);
        ActualizarPagoRequest request = new ActualizarPagoRequest();
        request.setMonto(BigDecimal.TEN);
        request.setMetodoPago(MetodoPago.EFECTIVO);
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoPendiente));

        assertThatThrownBy(() -> pagoService.actualizar(1L, request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("pendientes");
    }

    @Test
    void procesarDebeAprobarYGenerarCodigo() {
        ProcesarPagoRequest request = new ProcesarPagoRequest();
        request.setPagoId(1L);
        request.setAprobado(true);
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoPendiente));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PagoResponse response = pagoService.procesar(request);

        assertThat(response.getEstadoPago()).isEqualTo(EstadoPago.APROBADO);
        assertThat(response.getCodigoTransaccion()).startsWith("FF-");
        assertThat(response.getFechaProcesamiento()).isNotNull();
    }

    @Test
    void procesarDebeRechazarCuandoNoEstaPendiente() {
        pagoPendiente.setEstadoPago(EstadoPago.ANULADO);
        ProcesarPagoRequest request = new ProcesarPagoRequest();
        request.setPagoId(1L);
        request.setAprobado(false);
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoPendiente));

        assertThatThrownBy(() -> pagoService.procesar(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("pendientes");
    }

    @Test
    void anularDebeAnularPagoPendienteYEliminarDebeBorrar() {
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoPendiente));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PagoResponse response = pagoService.anular(1L);
        pagoService.eliminar(1L);

        assertThat(response.getEstadoPago()).isEqualTo(EstadoPago.ANULADO);
        verify(pagoRepository).delete(pagoPendiente);
    }

    @Test
    void anularDebeRechazarPagoAprobadoOYaAnulado() {
        pagoPendiente.setEstadoPago(EstadoPago.APROBADO);
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoPendiente));
        assertThatThrownBy(() -> pagoService.anular(1L)).isInstanceOf(ReglaNegocioException.class);

        pagoPendiente.setEstadoPago(EstadoPago.ANULADO);
        assertThatThrownBy(() -> pagoService.anular(1L)).isInstanceOf(ReglaNegocioException.class);
    }
}
