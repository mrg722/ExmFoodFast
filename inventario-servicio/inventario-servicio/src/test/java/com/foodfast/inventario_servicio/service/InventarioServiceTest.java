package com.foodfast.inventario_servicio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.foodfast.inventario_servicio.dto.DescontarStockRequest;
import com.foodfast.inventario_servicio.dto.InventarioRequest;
import com.foodfast.inventario_servicio.dto.InventarioResponse;
import com.foodfast.inventario_servicio.dto.StockResponse;
import com.foodfast.inventario_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.inventario_servicio.exception.ReglaNegocioException;
import com.foodfast.inventario_servicio.exception.StockInsuficienteException;
import com.foodfast.inventario_servicio.model.Inventario;
import com.foodfast.inventario_servicio.repository.InventarioRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @InjectMocks
    private InventarioService inventarioService;

    private Inventario inventario;
    private InventarioRequest request;

    @BeforeEach
    void setUp() {
        inventario = Inventario.builder()
                .id(1L)
                .productoId(10L)
                .cantidadDisponible(50)
                .cantidadReservada(5)
                .ubicacion("Bodega Central A1")
                .build();

        request = InventarioRequest.builder()
                .productoId(10L)
                .cantidadDisponible(50)
                .cantidadReservada(5)
                .ubicacion("Bodega Central A1")
                .build();
    }

    @Test
    void debeListarInventarios() {
        // Given
        when(inventarioRepository.findAll()).thenReturn(List.of(inventario));

        // When
        List<InventarioResponse> resultado = inventarioService.listar();

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getStockReal()).isEqualTo(45);
    }

    @Test
    void debeListarPorUbicacion() {
        when(inventarioRepository.findByUbicacionContainingIgnoreCase("central")).thenReturn(List.of(inventario));

        List<InventarioResponse> resultado = inventarioService.listarPorUbicacion("central");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getUbicacion()).contains("Central");
    }

    @Test
    void debeFallarAlListarPorUbicacionVacia() {
        assertThatThrownBy(() -> inventarioService.listarPorUbicacion(" "))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ubicación");
    }

    @Test
    void debeBuscarPorId() {
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));

        InventarioResponse resultado = inventarioService.buscarPorId(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getProductoId()).isEqualTo(10L);
    }

    @Test
    void debeFallarSiIdNoExiste() {
        when(inventarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventarioService.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void debeFallarSiIdEsInvalido() {
        assertThatThrownBy(() -> inventarioService.buscarPorId(0L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("mayor que cero");
    }

    @Test
    void debeBuscarPorProductoId() {
        when(inventarioRepository.findByProductoId(10L)).thenReturn(Optional.of(inventario));

        InventarioResponse resultado = inventarioService.buscarPorProductoId(10L);

        assertThat(resultado.getProductoId()).isEqualTo(10L);
        assertThat(resultado.getStockReal()).isEqualTo(45);
    }

    @Test
    void debeConsultarStock() {
        when(inventarioRepository.findByProductoId(10L)).thenReturn(Optional.of(inventario));

        StockResponse resultado = inventarioService.consultarStock(10L);

        assertThat(resultado.getProductoId()).isEqualTo(10L);
        assertThat(resultado.getStockDisponible()).isEqualTo(45);
        assertThat(resultado.getHayStock()).isTrue();
    }

    @Test
    void debeCrearInventario() {
        when(inventarioRepository.existsByProductoId(10L)).thenReturn(false);
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);

        InventarioResponse resultado = inventarioService.crear(request);

        assertThat(resultado.getProductoId()).isEqualTo(10L);
        assertThat(resultado.getStockReal()).isEqualTo(45);
        verify(inventarioRepository).save(any(Inventario.class));
    }

    @Test
    void debeFallarAlCrearDuplicado() {
        when(inventarioRepository.existsByProductoId(10L)).thenReturn(true);

        assertThatThrownBy(() -> inventarioService.crear(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya existe");

        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void debeFallarAlCrearConStockReservadoMayorQueDisponible() {
        request.setCantidadDisponible(3);
        request.setCantidadReservada(5);

        assertThatThrownBy(() -> inventarioService.crear(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("reservada");
    }

    @Test
    void debeFallarAlCrearConRequestNulo() {
        assertThatThrownBy(() -> inventarioService.crear(null))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("obligatorios");
    }

    @Test
    void debeActualizarInventario() {
        InventarioRequest actualizado = InventarioRequest.builder()
                .productoId(11L)
                .cantidadDisponible(70)
                .cantidadReservada(10)
                .ubicacion("Bodega B2")
                .build();
        Inventario guardado = Inventario.builder()
                .id(1L)
                .productoId(11L)
                .cantidadDisponible(70)
                .cantidadReservada(10)
                .ubicacion("Bodega B2")
                .build();

        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.findByProductoId(11L)).thenReturn(Optional.empty());
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(guardado);

        InventarioResponse resultado = inventarioService.actualizar(1L, actualizado);

        assertThat(resultado.getProductoId()).isEqualTo(11L);
        assertThat(resultado.getStockReal()).isEqualTo(60);
    }

    @Test
    void debeFallarAlActualizarConProductoDuplicadoDeOtroRegistro() {
        Inventario otro = Inventario.builder().id(2L).productoId(10L).cantidadDisponible(20).cantidadReservada(0).ubicacion("Otra").build();
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.findByProductoId(10L)).thenReturn(Optional.of(otro));

        assertThatThrownBy(() -> inventarioService.actualizar(1L, request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Otro registro");
    }

    @Test
    void debeDescontarStock() {
        DescontarStockRequest descuento = DescontarStockRequest.builder().productoId(10L).cantidad(5).build();
        Inventario actualizado = Inventario.builder().id(1L).productoId(10L).cantidadDisponible(45).cantidadReservada(5).ubicacion("Bodega Central A1").build();
        when(inventarioRepository.findByProductoId(10L)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(actualizado);

        InventarioResponse resultado = inventarioService.descontarStock(descuento);

        assertThat(resultado.getCantidadDisponible()).isEqualTo(45);
        assertThat(resultado.getStockReal()).isEqualTo(40);
    }

    @Test
    void debeFallarAlDescontarStockInsuficiente() {
        DescontarStockRequest descuento = DescontarStockRequest.builder().productoId(10L).cantidad(100).build();
        when(inventarioRepository.findByProductoId(10L)).thenReturn(Optional.of(inventario));

        assertThatThrownBy(() -> inventarioService.descontarStock(descuento))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    @Test
    void debeReservarStock() {
        DescontarStockRequest reserva = DescontarStockRequest.builder().productoId(10L).cantidad(10).build();
        Inventario actualizado = Inventario.builder().id(1L).productoId(10L).cantidadDisponible(50).cantidadReservada(15).ubicacion("Bodega Central A1").build();
        when(inventarioRepository.findByProductoId(10L)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(actualizado);

        InventarioResponse resultado = inventarioService.reservarStock(reserva);

        assertThat(resultado.getCantidadReservada()).isEqualTo(15);
        assertThat(resultado.getStockReal()).isEqualTo(35);
    }

    @Test
    void debeFallarAlReservarSinStock() {
        DescontarStockRequest reserva = DescontarStockRequest.builder().productoId(10L).cantidad(60).build();
        when(inventarioRepository.findByProductoId(10L)).thenReturn(Optional.of(inventario));

        assertThatThrownBy(() -> inventarioService.reservarStock(reserva))
                .isInstanceOf(StockInsuficienteException.class);
    }

    @Test
    void debeLiberarReserva() {
        DescontarStockRequest liberar = DescontarStockRequest.builder().productoId(10L).cantidad(3).build();
        Inventario actualizado = Inventario.builder().id(1L).productoId(10L).cantidadDisponible(50).cantidadReservada(2).ubicacion("Bodega Central A1").build();
        when(inventarioRepository.findByProductoId(10L)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(actualizado);

        InventarioResponse resultado = inventarioService.liberarReserva(liberar);

        assertThat(resultado.getCantidadReservada()).isEqualTo(2);
    }

    @Test
    void debeFallarAlLiberarMasDeLoReservado() {
        DescontarStockRequest liberar = DescontarStockRequest.builder().productoId(10L).cantidad(20).build();
        when(inventarioRepository.findByProductoId(10L)).thenReturn(Optional.of(inventario));

        assertThatThrownBy(() -> inventarioService.liberarReserva(liberar))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("liberar más stock");
    }

    @Test
    void debeEliminarInventario() {
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));

        inventarioService.eliminar(1L);

        verify(inventarioRepository).delete(inventario);
    }
}
