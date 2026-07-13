package com.foodfast.reparto_servicio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.foodfast.reparto_servicio.client.PedidoClient;
import com.foodfast.reparto_servicio.dto.CambiarEstadoEntregaRequest;
import com.foodfast.reparto_servicio.dto.EntregaRequest;
import com.foodfast.reparto_servicio.dto.EntregaResponse;
import com.foodfast.reparto_servicio.dto.PedidoResponse;
import com.foodfast.reparto_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.reparto_servicio.exception.ReglaNegocioException;
import com.foodfast.reparto_servicio.model.Entrega;
import com.foodfast.reparto_servicio.model.EstadoEntrega;
import com.foodfast.reparto_servicio.model.Repartidor;
import com.foodfast.reparto_servicio.repository.EntregaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EntregaServiceTest {

    @Mock
    private EntregaRepository entregaRepository;

    @Mock
    private RepartidorService repartidorService;

    @Mock
    private PedidoClient pedidoClient;

    @InjectMocks
    private EntregaService entregaService;

    @BeforeEach
    void configurar() {
        ReflectionTestUtils.setField(entregaService, "pedidoIntegrationEnabled", false);
    }

    @Test
    void listarDebeRetornarEntregasMapeadas() {
        // Given
        when(entregaRepository.findAll()).thenReturn(List.of(entrega(1L, EstadoEntrega.CREADA, null)));

        // When
        List<EntregaResponse> respuesta = entregaService.listar();

        // Then
        assertThat(respuesta).hasSize(1);
        assertThat(respuesta.getFirst().getPedidoId()).isEqualTo(100L);
    }

    @Test
    void buscarPorIdDebeRetornarEntregaSiExiste() {
        // Given
        when(entregaRepository.findById(1L)).thenReturn(Optional.of(entrega(1L, EstadoEntrega.CREADA, null)));

        // When
        EntregaResponse respuesta = entregaService.buscarPorId(1L);

        // Then
        assertThat(respuesta.getId()).isEqualTo(1L);
    }

    @Test
    void buscarPorIdDebeFallarSiNoExiste() {
        // Given
        when(entregaRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> entregaService.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void listarPorPedidoDebeConsultarRepositorio() {
        // Given
        when(entregaRepository.findByPedidoId(100L)).thenReturn(List.of(entrega(1L, EstadoEntrega.CREADA, null)));

        // When
        List<EntregaResponse> respuesta = entregaService.listarPorPedido(100L);

        // Then
        assertThat(respuesta).hasSize(1);
    }

    @Test
    void listarPorRepartidorDebeConsultarRepositorio() {
        // Given
        Repartidor repartidor = repartidor(true, true);
        when(entregaRepository.findByRepartidorId(1L)).thenReturn(List.of(entrega(1L, EstadoEntrega.ASIGNADA, repartidor)));

        // When
        List<EntregaResponse> respuesta = entregaService.listarPorRepartidor(1L);

        // Then
        assertThat(respuesta.getFirst().getRepartidorId()).isEqualTo(1L);
    }

    @Test
    void crearDebeCrearEntregaSinRepartidor() {
        // Given
        EntregaRequest request = entregaRequest(null);
        when(entregaRepository.existsByPedidoId(100L)).thenReturn(false);
        when(entregaRepository.save(any(Entrega.class))).thenAnswer(invocation -> {
            Entrega entrega = invocation.getArgument(0);
            entrega.setId(1L);
            return entrega;
        });

        // When
        EntregaResponse respuesta = entregaService.crear(request);

        // Then
        assertThat(respuesta.getEstadoEntrega()).isEqualTo(EstadoEntrega.CREADA);
        assertThat(respuesta.getRepartidorId()).isNull();
    }

    @Test
    void crearDebeValidarPedidoRemotoCuandoIntegracionEstaActiva() {
        // Given
        ReflectionTestUtils.setField(entregaService, "pedidoIntegrationEnabled", true);
        EntregaRequest request = entregaRequest(null);
        when(pedidoClient.obtenerPedido(100L)).thenReturn(PedidoResponse.builder().id(100L).build());
        when(entregaRepository.existsByPedidoId(100L)).thenReturn(false);
        when(entregaRepository.save(any(Entrega.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        EntregaResponse respuesta = entregaService.crear(request);

        // Then
        assertThat(respuesta.getPedidoId()).isEqualTo(100L);
        verify(pedidoClient).obtenerPedido(100L);
    }

    @Test
    void crearDebeFallarSiPedidoRemotoEsInconsistente() {
        // Given
        ReflectionTestUtils.setField(entregaService, "pedidoIntegrationEnabled", true);
        when(pedidoClient.obtenerPedido(100L)).thenReturn(PedidoResponse.builder().id(999L).build());

        // When / Then
        assertThatThrownBy(() -> entregaService.crear(entregaRequest(null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("inconsistente");
    }

    @Test
    void crearDebeFallarSiYaExisteEntregaParaPedido() {
        // Given
        when(entregaRepository.existsByPedidoId(100L)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> entregaService.crear(entregaRequest(null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya existe");
    }

    @Test
    void crearConRepartidorDebeAsignarYDejarNoDisponible() {
        // Given
        Repartidor repartidor = repartidor(true, true);
        when(entregaRepository.existsByPedidoId(100L)).thenReturn(false);
        when(repartidorService.obtenerEntidad(1L)).thenReturn(repartidor);
        when(entregaRepository.save(any(Entrega.class))).thenAnswer(invocation -> {
            Entrega entrega = invocation.getArgument(0);
            entrega.setId(1L);
            return entrega;
        });

        // When
        EntregaResponse respuesta = entregaService.crear(entregaRequest(1L));

        // Then
        assertThat(respuesta.getEstadoEntrega()).isEqualTo(EstadoEntrega.ASIGNADA);
        assertThat(repartidor.getDisponible()).isFalse();
    }

    @Test
    void crearConRepartidorNoDisponibleDebeFallar() {
        // Given
        when(entregaRepository.existsByPedidoId(100L)).thenReturn(false);
        when(repartidorService.obtenerEntidad(1L)).thenReturn(repartidor(true, false));

        // When / Then
        assertThatThrownBy(() -> entregaService.crear(entregaRequest(1L)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("disponible");
    }

    @Test
    void actualizarDebeModificarDireccion() {
        // Given
        Entrega entrega = entrega(1L, EstadoEntrega.CREADA, null);
        EntregaRequest request = entregaRequest(null);
        request.setDireccionEntrega("Nueva direccion");
        when(entregaRepository.findById(1L)).thenReturn(Optional.of(entrega));
        when(entregaRepository.save(any(Entrega.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        EntregaResponse respuesta = entregaService.actualizar(1L, request);

        // Then
        assertThat(respuesta.getDireccionEntrega()).isEqualTo("Nueva direccion");
        assertThat(respuesta.getFechaActualizacion()).isNotNull();
    }

    @Test
    void actualizarDebeFallarSiEntregaFinalizada() {
        // Given
        when(entregaRepository.findById(1L)).thenReturn(Optional.of(entrega(1L, EstadoEntrega.ENTREGADA, repartidor(true, false))));

        // When / Then
        assertThatThrownBy(() -> entregaService.actualizar(1L, entregaRequest(null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("finalizada");
    }

    @Test
    void actualizarDebeFallarSiNuevoPedidoYaTieneEntrega() {
        // Given
        Entrega entrega = entrega(1L, EstadoEntrega.CREADA, null);
        entrega.setPedidoId(100L);
        EntregaRequest request = entregaRequest(null);
        request.setPedidoId(200L);
        when(entregaRepository.findById(1L)).thenReturn(Optional.of(entrega));
        when(entregaRepository.existsByPedidoId(200L)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> entregaService.actualizar(1L, request))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void asignarRepartidorDebeCambiarEstadoAAsignada() {
        // Given
        Repartidor repartidor = repartidor(true, true);
        when(entregaRepository.findById(1L)).thenReturn(Optional.of(entrega(1L, EstadoEntrega.CREADA, null)));
        when(repartidorService.obtenerEntidad(1L)).thenReturn(repartidor);
        when(entregaRepository.save(any(Entrega.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        EntregaResponse respuesta = entregaService.asignarRepartidor(1L, 1L);

        // Then
        assertThat(respuesta.getEstadoEntrega()).isEqualTo(EstadoEntrega.ASIGNADA);
        assertThat(repartidor.getDisponible()).isFalse();
    }

    @Test
    void asignarRepartidorDebeFallarSiEntregaNoEstaCreada() {
        // Given
        when(entregaRepository.findById(1L)).thenReturn(Optional.of(entrega(1L, EstadoEntrega.ASIGNADA, repartidor(true, false))));

        // When / Then
        assertThatThrownBy(() -> entregaService.asignarRepartidor(1L, 1L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("creadas");
    }

    @Test
    void cambiarEstadoDeAsignadaAEnCaminoDebeFuncionar() {
        // Given
        when(entregaRepository.findById(1L)).thenReturn(Optional.of(entrega(1L, EstadoEntrega.ASIGNADA, repartidor(true, false))));
        when(entregaRepository.save(any(Entrega.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        EntregaResponse respuesta = entregaService.cambiarEstado(1L, cambio(EstadoEntrega.EN_CAMINO));

        // Then
        assertThat(respuesta.getEstadoEntrega()).isEqualTo(EstadoEntrega.EN_CAMINO);
    }

    @Test
    void cambiarEstadoAEntregadaDebeLiberarRepartidor() {
        // Given
        Repartidor repartidor = repartidor(true, false);
        when(entregaRepository.findById(1L)).thenReturn(Optional.of(entrega(1L, EstadoEntrega.EN_CAMINO, repartidor)));
        when(entregaRepository.save(any(Entrega.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        EntregaResponse respuesta = entregaService.cambiarEstado(1L, cambio(EstadoEntrega.ENTREGADA));

        // Then
        assertThat(respuesta.getEstadoEntrega()).isEqualTo(EstadoEntrega.ENTREGADA);
        assertThat(repartidor.getDisponible()).isTrue();
    }

    @Test
    void cambiarEstadoDebeFallarSiNuevoEstadoEsNulo() {
        // Given
        when(entregaRepository.findById(1L)).thenReturn(Optional.of(entrega(1L, EstadoEntrega.CREADA, null)));

        // When / Then
        assertThatThrownBy(() -> entregaService.cambiarEstado(1L, cambio(null)))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void cambiarEstadoDebeFallarSiQuiereAvanzarSinRepartidor() {
        // Given
        when(entregaRepository.findById(1L)).thenReturn(Optional.of(entrega(1L, EstadoEntrega.CREADA, null)));

        // When / Then
        assertThatThrownBy(() -> entregaService.cambiarEstado(1L, cambio(EstadoEntrega.EN_CAMINO)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("repartidor");
    }

    @Test
    void cambiarEstadoDebeFallarSiTransicionNoPermitida() {
        // Given
        when(entregaRepository.findById(1L)).thenReturn(Optional.of(entrega(1L, EstadoEntrega.ASIGNADA, repartidor(true, false))));

        // When / Then
        assertThatThrownBy(() -> entregaService.cambiarEstado(1L, cambio(EstadoEntrega.CREADA)))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void cambiarEstadoDebeFallarSiEntregaYaEstaCancelada() {
        // Given
        when(entregaRepository.findById(1L)).thenReturn(Optional.of(entrega(1L, EstadoEntrega.CANCELADA, repartidor(true, true))));

        // When / Then
        assertThatThrownBy(() -> entregaService.cambiarEstado(1L, cambio(EstadoEntrega.ASIGNADA)))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void eliminarDebeBorrarEntregaSiNoEstaEnCamino() {
        // Given
        Entrega entrega = entrega(1L, EstadoEntrega.CREADA, null);
        when(entregaRepository.findById(1L)).thenReturn(Optional.of(entrega));

        // When
        entregaService.eliminar(1L);

        // Then
        verify(entregaRepository).delete(entrega);
    }

    @Test
    void eliminarDebeFallarSiEntregaEstaEnCamino() {
        // Given
        when(entregaRepository.findById(1L)).thenReturn(Optional.of(entrega(1L, EstadoEntrega.EN_CAMINO, repartidor(true, false))));

        // When / Then
        assertThatThrownBy(() -> entregaService.eliminar(1L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("en camino");
        verify(entregaRepository, never()).delete(any());
    }

    @Test
    void operacionesDebenValidarIdMayorACero() {
        assertThatThrownBy(() -> entregaService.buscarPorId(0L))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> entregaService.listarPorPedido(-1L))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> entregaService.listarPorRepartidor(null))
                .isInstanceOf(ReglaNegocioException.class);
    }

    private EntregaRequest entregaRequest(Long repartidorId) {
        EntregaRequest request = new EntregaRequest();
        request.setPedidoId(100L);
        request.setRepartidorId(repartidorId);
        request.setDireccionEntrega("Av. FoodFast 123");
        return request;
    }

    private CambiarEstadoEntregaRequest cambio(EstadoEntrega estado) {
        CambiarEstadoEntregaRequest request = new CambiarEstadoEntregaRequest();
        request.setEstadoEntrega(estado);
        return request;
    }

    private Repartidor repartidor(boolean activo, boolean disponible) {
        return Repartidor.builder()
                .id(1L)
                .nombre("Juan Perez")
                .telefono("+56911112222")
                .vehiculo("Moto")
                .activo(activo)
                .disponible(disponible)
                .build();
    }

    private Entrega entrega(Long id, EstadoEntrega estado, Repartidor repartidor) {
        return Entrega.builder()
                .id(id)
                .pedidoId(100L)
                .repartidor(repartidor)
                .direccionEntrega("Av. FoodFast 123")
                .estadoEntrega(estado)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }
}
