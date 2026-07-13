package com.foodfast.reparto_servicio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.foodfast.reparto_servicio.dto.RepartidorRequest;
import com.foodfast.reparto_servicio.dto.RepartidorResponse;
import com.foodfast.reparto_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.reparto_servicio.exception.ReglaNegocioException;
import com.foodfast.reparto_servicio.model.Entrega;
import com.foodfast.reparto_servicio.model.Repartidor;
import com.foodfast.reparto_servicio.repository.RepartidorRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RepartidorServiceTest {

    @Mock
    private RepartidorRepository repartidorRepository;

    @InjectMocks
    private RepartidorService repartidorService;

    @Test
    void listarDebeRetornarRepartidoresMapeados() {
        // Given
        when(repartidorRepository.findAll()).thenReturn(List.of(repartidor(1L, true, true)));

        // When
        List<RepartidorResponse> respuesta = repartidorService.listar();

        // Then
        assertThat(respuesta).hasSize(1);
        assertThat(respuesta.getFirst().getNombre()).isEqualTo("Juan Perez");
    }

    @Test
    void listarDisponiblesDebeUsarConsultaDeActivosYDisponibles() {
        // Given
        when(repartidorRepository.findByActivoTrueAndDisponibleTrue()).thenReturn(List.of(repartidor(1L, true, true)));

        // When
        List<RepartidorResponse> respuesta = repartidorService.listarDisponibles();

        // Then
        assertThat(respuesta).hasSize(1);
        assertThat(respuesta.getFirst().getDisponible()).isTrue();
    }

    @Test
    void buscarPorIdDebeRetornarRepartidorSiExiste() {
        // Given
        when(repartidorRepository.findById(1L)).thenReturn(Optional.of(repartidor(1L, true, true)));

        // When
        RepartidorResponse respuesta = repartidorService.buscarPorId(1L);

        // Then
        assertThat(respuesta.getId()).isEqualTo(1L);
    }

    @Test
    void buscarPorIdDebeLanzarErrorSiNoExiste() {
        // Given
        when(repartidorRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> repartidorService.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void crearDebeGuardarRepartidor() {
        // Given
        RepartidorRequest request = request(true, true);
        when(repartidorRepository.save(any(Repartidor.class))).thenReturn(repartidor(1L, true, true));

        // When
        RepartidorResponse respuesta = repartidorService.crear(request);

        // Then
        assertThat(respuesta.getId()).isEqualTo(1L);
        assertThat(respuesta.getActivo()).isTrue();
    }

    @Test
    void actualizarDebeModificarDatos() {
        // Given
        when(repartidorRepository.findById(1L)).thenReturn(Optional.of(repartidor(1L, true, true)));
        when(repartidorRepository.save(any(Repartidor.class))).thenAnswer(invocation -> invocation.getArgument(0));
        RepartidorRequest request = request(true, false);
        request.setNombre("Pedro Actualizado");

        // When
        RepartidorResponse respuesta = repartidorService.actualizar(1L, request);

        // Then
        assertThat(respuesta.getNombre()).isEqualTo("Pedro Actualizado");
        assertThat(respuesta.getDisponible()).isFalse();
    }

    @Test
    void cambiarDisponibilidadDebeValidarBooleanNoNulo() {
        // When / Then
        assertThatThrownBy(() -> repartidorService.cambiarDisponibilidad(1L, null))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("disponibilidad");
    }

    @Test
    void cambiarDisponibilidadDebeFallarSiRepartidorEstaInactivo() {
        // Given
        when(repartidorRepository.findById(1L)).thenReturn(Optional.of(repartidor(1L, false, false)));

        // When / Then
        assertThatThrownBy(() -> repartidorService.cambiarDisponibilidad(1L, true))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("inactivo");
    }

    @Test
    void cambiarDisponibilidadDebeActualizarRepartidorActivo() {
        // Given
        when(repartidorRepository.findById(1L)).thenReturn(Optional.of(repartidor(1L, true, true)));
        when(repartidorRepository.save(any(Repartidor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RepartidorResponse respuesta = repartidorService.cambiarDisponibilidad(1L, false);

        // Then
        assertThat(respuesta.getDisponible()).isFalse();
    }

    @Test
    void desactivarDebeDejarActivoYDisponibleEnFalse() {
        // Given
        when(repartidorRepository.findById(1L)).thenReturn(Optional.of(repartidor(1L, true, true)));
        when(repartidorRepository.save(any(Repartidor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RepartidorResponse respuesta = repartidorService.desactivar(1L);

        // Then
        assertThat(respuesta.getActivo()).isFalse();
        assertThat(respuesta.getDisponible()).isFalse();
    }

    @Test
    void eliminarDebeBorrarSiNoTieneEntregas() {
        // Given
        Repartidor repartidor = repartidor(1L, true, true);
        repartidor.setEntregas(new ArrayList<>());
        when(repartidorRepository.findById(1L)).thenReturn(Optional.of(repartidor));

        // When
        repartidorService.eliminar(1L);

        // Then
        verify(repartidorRepository).delete(repartidor);
    }

    @Test
    void eliminarDebeFallarSiTieneEntregas() {
        // Given
        Repartidor repartidor = repartidor(1L, true, true);
        repartidor.setEntregas(List.of(Entrega.builder().id(10L).build()));
        when(repartidorRepository.findById(1L)).thenReturn(Optional.of(repartidor));

        // When / Then
        assertThatThrownBy(() -> repartidorService.eliminar(1L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("entregas asociadas");
        verify(repartidorRepository, never()).delete(any());
    }

    @Test
    void obtenerEntidadDebeValidarIdMayorACero() {
        assertThatThrownBy(() -> repartidorService.obtenerEntidad(0L))
                .isInstanceOf(ReglaNegocioException.class);
    }

    private Repartidor repartidor(Long id, boolean activo, boolean disponible) {
        return Repartidor.builder()
                .id(id)
                .nombre("Juan Perez")
                .telefono("+56911112222")
                .vehiculo("Moto")
                .activo(activo)
                .disponible(disponible)
                .entregas(new ArrayList<>())
                .build();
    }

    private RepartidorRequest request(boolean activo, boolean disponible) {
        RepartidorRequest request = new RepartidorRequest();
        request.setNombre("Juan Perez");
        request.setTelefono("+56911112222");
        request.setVehiculo("Moto");
        request.setActivo(activo);
        request.setDisponible(disponible);
        return request;
    }
}
