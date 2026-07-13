package com.foodfast.pedido_servicio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.foodfast.pedido_servicio.client.InventarioClient;
import com.foodfast.pedido_servicio.dto.InventarioStockResponse;
import com.foodfast.pedido_servicio.dto.PedidoRequest;
import com.foodfast.pedido_servicio.dto.PedidoResponse;
import com.foodfast.pedido_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.pedido_servicio.exception.ReglaNegocioException;
import com.foodfast.pedido_servicio.model.EstadoPedido;
import com.foodfast.pedido_servicio.model.Pedido;
import com.foodfast.pedido_servicio.repository.PedidoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private InventarioClient inventarioClient;

    @InjectMocks
    private PedidoService pedidoService;

    private Pedido pedidoGuardado;

    @BeforeEach
    void setUp() {
        pedidoGuardado = Pedido.builder()
                .id(1L)
                .clienteId(1L)
                .productoId(72L)
                .cantidad(2)
                .fechaCreacion(LocalDateTime.now())
                .estado(EstadoPedido.CONFIRMADO)
                .total(new BigDecimal("15990"))
                .direccionEntrega("Av. FoodFast 123")
                .observacion("Sin cebolla")
                .build();
    }

    @Test
    void listarDebeRetornarPedidosMapeados() {
        // Given
        when(pedidoRepository.findAll()).thenReturn(List.of(pedidoGuardado));

        // When
        List<PedidoResponse> responses = pedidoService.listar();

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(0).getEstado()).isEqualTo("CONFIRMADO");
    }

    @Test
    void buscarPorIdExistenteDebeRetornarPedido() {
        // Given
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoGuardado));

        // When
        PedidoResponse response = pedidoService.buscarPorId(1L);

        // Then
        assertThat(response.getClienteId()).isEqualTo(1L);
        assertThat(response.getDireccionEntrega()).isEqualTo("Av. FoodFast 123");
    }

    @Test
    void buscarPorIdInexistenteDebeLanzarNoEncontrado() {
        // Given
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> pedidoService.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Pedido no encontrado");
    }

    @Test
    void buscarConIdInvalidoDebeLanzarReglaNegocio() {
        assertThatThrownBy(() -> pedidoService.buscarPorId(0L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("id debe ser mayor a cero");
    }

    @Test
    void listarPorClienteDebeMapearPedidos() {
        // Given
        when(pedidoRepository.findByClienteId(1L)).thenReturn(List.of(pedidoGuardado));

        // When
        List<PedidoResponse> responses = pedidoService.listarPorCliente(1L);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getClienteId()).isEqualTo(1L);
    }

    @Test
    void listarPorClienteConIdInvalidoDebeLanzarReglaNegocio() {
        assertThatThrownBy(() -> pedidoService.listarPorCliente(-1L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("clienteId debe ser mayor a cero");
    }

    @Test
    void crearPedidoSinInventarioDebeGuardarConfirmado() {
        // Given
        PedidoRequest request = requestBase();
        request.setProductoId(null);
        request.setCantidad(null);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);

        // When
        PedidoResponse response = pedidoService.crear(request);

        // Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEstado()).isEqualTo("CONFIRMADO");
        assertThat(response.getMensajeInventario()).isEqualTo("Pedido creado sin validación de inventario");
        verify(inventarioClient, never()).consultarStock(any());
        verify(inventarioClient, never()).descontarStock(any(), any());
    }

    @Test
    void crearPedidoConStockDisponibleDebeConsultarYDescontarInventario() {
        // Given
        PedidoRequest request = requestBase();
        InventarioStockResponse stock = InventarioStockResponse.builder()
                .productoId(72L)
                .stockDisponible(10)
                .hayStock(true)
                .build();
        when(inventarioClient.consultarStock(72L)).thenReturn(stock);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);

        // When
        PedidoResponse response = pedidoService.crear(request);

        // Then
        assertThat(response.getEstadoStock()).isEqualTo("STOCK DISPONIBLE");
        assertThat(response.getMensajeInventario()).isEqualTo("Stock descontado correctamente en inventario-servicio");
        verify(inventarioClient).consultarStock(72L);
        verify(inventarioClient).descontarStock(72L, 2);
    }

    @Test
    void crearPedidoDebeAceptarStockRealComoFallback() {
        // Given
        PedidoRequest request = requestBase();
        InventarioStockResponse stock = InventarioStockResponse.builder()
                .productoId(72L)
                .stockReal(9)
                .disponible(true)
                .build();
        when(inventarioClient.consultarStock(72L)).thenReturn(stock);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);

        // When
        PedidoResponse response = pedidoService.crear(request);

        // Then
        assertThat(response.getEstadoStock()).isEqualTo("STOCK DISPONIBLE");
        verify(inventarioClient).descontarStock(72L, 2);
    }

    @Test
    void crearPedidoConStockInsuficienteDebeLanzarReglaNegocio() {
        // Given
        PedidoRequest request = requestBase();
        InventarioStockResponse stock = InventarioStockResponse.builder()
                .productoId(72L)
                .stockDisponible(1)
                .hayStock(true)
                .build();
        when(inventarioClient.consultarStock(72L)).thenReturn(stock);

        // When / Then
        assertThatThrownBy(() -> pedidoService.crear(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("No hay stock suficiente");
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verify(inventarioClient, never()).descontarStock(any(), any());
    }

    @Test
    void crearPedidoConInventarioSinDatosDebeLanzarReglaNegocio() {
        // Given
        PedidoRequest request = requestBase();
        InventarioStockResponse stock = InventarioStockResponse.builder()
                .productoId(72L)
                .hayStock(true)
                .build();
        when(inventarioClient.consultarStock(72L)).thenReturn(stock);

        // When / Then
        assertThatThrownBy(() -> pedidoService.crear(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("cantidad de stock válida");
    }

    @Test
    void crearPedidoConInventarioNuloDebeLanzarReglaNegocio() {
        // Given
        PedidoRequest request = requestBase();
        when(inventarioClient.consultarStock(72L)).thenReturn(null);

        // When / Then
        assertThatThrownBy(() -> pedidoService.crear(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Inventario no devolvió información");
    }

    @Test
    void crearPedidoConHayStockFalsoDebeLanzarReglaNegocio() {
        // Given
        PedidoRequest request = requestBase();
        InventarioStockResponse stock = InventarioStockResponse.builder()
                .productoId(72L)
                .stockDisponible(10)
                .hayStock(false)
                .build();
        when(inventarioClient.consultarStock(72L)).thenReturn(stock);

        // When / Then
        assertThatThrownBy(() -> pedidoService.crear(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("No hay stock suficiente");
    }

    @Test
    void crearPedidoConProductoSinCantidadDebeLanzarReglaNegocio() {
        // Given
        PedidoRequest request = requestBase();
        request.setCantidad(null);

        // When / Then
        assertThatThrownBy(() -> pedidoService.crear(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("productoId y cantidad juntos");
    }

    @Test
    void actualizarPedidoConfirmadoDebeGuardarCambios() {
        // Given
        PedidoRequest request = requestBase();
        request.setDireccionEntrega("Nueva dirección 456");
        InventarioStockResponse stock = InventarioStockResponse.builder()
                .productoId(72L)
                .stockDisponible(10)
                .hayStock(true)
                .build();
        when(inventarioClient.consultarStock(72L)).thenReturn(stock);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoGuardado));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);

        // When
        PedidoResponse response = pedidoService.actualizar(1L, request);

        // Then
        assertThat(response.getDireccionEntrega()).isEqualTo("Nueva dirección 456");
        verify(pedidoRepository).save(pedidoGuardado);
    }

    @Test
    void actualizarPedidoCanceladoDebeLanzarReglaNegocio() {
        // Given
        pedidoGuardado.setEstado(EstadoPedido.CANCELADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoGuardado));

        // When / Then
        assertThatThrownBy(() -> pedidoService.actualizar(1L, requestBase()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("No se puede actualizar");
    }

    @Test
    void cancelarPedidoConfirmadoDebeCambiarEstadoACancelado() {
        // Given
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoGuardado));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);

        // When
        pedidoService.cancelar(1L);

        // Then
        assertThat(pedidoGuardado.getEstado()).isEqualTo(EstadoPedido.CANCELADO);
        verify(pedidoRepository).save(pedidoGuardado);
    }

    @Test
    void cancelarPedidoCanceladoDebeLanzarReglaNegocio() {
        // Given
        pedidoGuardado.setEstado(EstadoPedido.CANCELADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoGuardado));

        // When / Then
        assertThatThrownBy(() -> pedidoService.cancelar(1L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya se encuentra cancelado");
    }

    @Test
    void eliminarPedidoExistenteDebeEliminar() {
        // Given
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoGuardado));

        // When
        pedidoService.eliminar(1L);

        // Then
        verify(pedidoRepository).delete(pedidoGuardado);
    }

    private PedidoRequest requestBase() {
        PedidoRequest request = new PedidoRequest();
        request.setClienteId(1L);
        request.setProductoId(72L);
        request.setCantidad(2);
        request.setTotal(new BigDecimal("15990"));
        request.setDireccionEntrega("Av. FoodFast 123");
        request.setObservacion("Sin cebolla");
        return request;
    }
}
