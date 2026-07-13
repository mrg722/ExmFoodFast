package com.foodfast.reparto_servicio.service;

import com.foodfast.reparto_servicio.dto.RepartidorRequest;
import com.foodfast.reparto_servicio.dto.RepartidorResponse;
import com.foodfast.reparto_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.reparto_servicio.exception.ReglaNegocioException;
import com.foodfast.reparto_servicio.model.Repartidor;
import com.foodfast.reparto_servicio.repository.RepartidorRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepartidorService {

    private final RepartidorRepository repartidorRepository;

    @Transactional(readOnly = true)
    public List<RepartidorResponse> listar() {
        log.info("Listando repartidores");
        return repartidorRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RepartidorResponse> listarDisponibles() {
        log.info("Listando repartidores disponibles");
        return repartidorRepository.findByActivoTrueAndDisponibleTrue().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RepartidorResponse buscarPorId(Long id) {
        validarId(id);
        log.info("Buscando repartidor id={}", id);
        return toResponse(obtenerEntidad(id));
    }

    @Transactional
    public RepartidorResponse crear(RepartidorRequest request) {
        log.info("Creando repartidor nombre={}", request.getNombre());
        Repartidor repartidor = Repartidor.builder()
                .nombre(request.getNombre())
                .telefono(request.getTelefono())
                .vehiculo(request.getVehiculo())
                .activo(Boolean.TRUE.equals(request.getActivo()))
                .disponible(Boolean.TRUE.equals(request.getDisponible()))
                .build();
        return toResponse(repartidorRepository.save(repartidor));
    }

    @Transactional
    public RepartidorResponse actualizar(Long id, RepartidorRequest request) {
        validarId(id);
        log.info("Actualizando repartidor id={}", id);
        Repartidor repartidor = obtenerEntidad(id);
        repartidor.setNombre(request.getNombre());
        repartidor.setTelefono(request.getTelefono());
        repartidor.setVehiculo(request.getVehiculo());
        repartidor.setActivo(Boolean.TRUE.equals(request.getActivo()));
        repartidor.setDisponible(Boolean.TRUE.equals(request.getDisponible()));
        return toResponse(repartidorRepository.save(repartidor));
    }

    @Transactional
    public RepartidorResponse cambiarDisponibilidad(Long id, Boolean disponible) {
        validarId(id);
        if (disponible == null) {
            throw new ReglaNegocioException("La disponibilidad es obligatoria");
        }
        Repartidor repartidor = obtenerEntidad(id);
        if (!Boolean.TRUE.equals(repartidor.getActivo())) {
            throw new ReglaNegocioException("No se puede cambiar disponibilidad de un repartidor inactivo");
        }
        repartidor.setDisponible(disponible);
        log.info("Repartidor id={} cambia disponibilidad a {}", id, disponible);
        return toResponse(repartidorRepository.save(repartidor));
    }

    @Transactional
    public RepartidorResponse desactivar(Long id) {
        validarId(id);
        Repartidor repartidor = obtenerEntidad(id);
        repartidor.setActivo(false);
        repartidor.setDisponible(false);
        log.info("Repartidor id={} desactivado", id);
        return toResponse(repartidorRepository.save(repartidor));
    }

    @Transactional
    public void eliminar(Long id) {
        validarId(id);
        Repartidor repartidor = obtenerEntidad(id);
        if (repartidor.getEntregas() != null && !repartidor.getEntregas().isEmpty()) {
            throw new ReglaNegocioException("No se puede eliminar un repartidor con entregas asociadas. Use desactivar.");
        }
        log.warn("Eliminando repartidor id={}", id);
        repartidorRepository.delete(repartidor);
    }

    public Repartidor obtenerEntidad(Long id) {
        validarId(id);
        return repartidorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Repartidor no encontrado con id: " + id));
    }

    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new ReglaNegocioException("El id debe ser mayor que cero");
        }
    }

    private RepartidorResponse toResponse(Repartidor repartidor) {
        return RepartidorResponse.builder()
                .id(repartidor.getId())
                .nombre(repartidor.getNombre())
                .telefono(repartidor.getTelefono())
                .vehiculo(repartidor.getVehiculo())
                .activo(repartidor.getActivo())
                .disponible(repartidor.getDisponible())
                .build();
    }
}
