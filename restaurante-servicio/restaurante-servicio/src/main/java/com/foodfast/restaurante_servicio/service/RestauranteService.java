package com.foodfast.restaurante_servicio.service;

import com.foodfast.restaurante_servicio.client.NotificacionClient;
import com.foodfast.restaurante_servicio.dto.HorarioRestauranteResponse;
import com.foodfast.restaurante_servicio.dto.RestauranteRequest;
import com.foodfast.restaurante_servicio.dto.RestauranteResponse;
import com.foodfast.restaurante_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.restaurante_servicio.exception.ReglaNegocioException;
import com.foodfast.restaurante_servicio.model.Restaurante;
import com.foodfast.restaurante_servicio.repository.RestauranteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestauranteService {

    private final RestauranteRepository restauranteRepository;
    private final NotificacionClient notificacionClient;

    @Value("${services.notificacion.enabled:false}")
    private boolean notificacionEnabled;

    @Transactional(readOnly = true)
    public List<RestauranteResponse> listar() {
        return restauranteRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RestauranteResponse> listarActivos() {
        return restauranteRepository.findByActivoTrue().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RestauranteResponse> listarAbiertos() {
        return restauranteRepository.findByAbiertoTrue().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RestauranteResponse buscarPorId(Long id) {
        return toResponse(obtenerEntidad(id));
    }

    @Transactional
    public RestauranteResponse crear(RestauranteRequest request) {
        validarNombreDisponible(request.getNombre(), null);
        validarEstado(request.getActivo(), request.getAbierto());

        Restaurante restaurante = Restaurante.builder()
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion())
                .direccion(request.getDireccion())
                .telefono(request.getTelefono())
                .email(request.getEmail())
                .activo(request.getActivo())
                .abierto(request.getAbierto())
                .build();

        Restaurante guardado = restauranteRepository.save(restaurante);
        log.info("Restaurante creado id={} nombre={}", guardado.getId(), guardado.getNombre());
        return toResponse(guardado);
    }

    @Transactional
    public RestauranteResponse actualizar(Long id, RestauranteRequest request) {
        Restaurante restaurante = obtenerEntidad(id);
        validarNombreDisponible(request.getNombre(), id);
        validarEstado(request.getActivo(), request.getAbierto());

        restaurante.setNombre(request.getNombre().trim());
        restaurante.setDescripcion(request.getDescripcion());
        restaurante.setDireccion(request.getDireccion());
        restaurante.setTelefono(request.getTelefono());
        restaurante.setEmail(request.getEmail());
        restaurante.setActivo(request.getActivo());
        restaurante.setAbierto(request.getAbierto());

        Restaurante actualizado = restauranteRepository.save(restaurante);
        log.info("Restaurante actualizado id={}", actualizado.getId());
        return toResponse(actualizado);
    }

    @Transactional
    public RestauranteResponse cambiarActivo(Long id, Boolean activo) {
        if (activo == null) {
            throw new ReglaNegocioException("El estado activo es obligatorio");
        }
        Restaurante restaurante = obtenerEntidad(id);
        restaurante.setActivo(activo);
        if (Boolean.FALSE.equals(activo)) {
            restaurante.setAbierto(false);
        }
        Restaurante actualizado = restauranteRepository.save(restaurante);
        notificarSiCorresponde(actualizado, "Estado de restaurante actualizado", "Restaurante activo=" + activo);
        return toResponse(actualizado);
    }

    @Transactional
    public RestauranteResponse cambiarEstadoAbierto(Long id, Boolean abierto) {
        if (abierto == null) {
            throw new ReglaNegocioException("El estado abierto es obligatorio");
        }
        Restaurante restaurante = obtenerEntidad(id);
        if (!Boolean.TRUE.equals(restaurante.getActivo()) && Boolean.TRUE.equals(abierto)) {
            throw new ReglaNegocioException("No se puede abrir un restaurante inactivo");
        }
        restaurante.setAbierto(abierto);
        Restaurante actualizado = restauranteRepository.save(restaurante);
        notificarSiCorresponde(actualizado, "Estado de apertura actualizado", "Restaurante abierto=" + abierto);
        return toResponse(actualizado);
    }

    @Transactional
    public void eliminar(Long id) {
        Restaurante restaurante = obtenerEntidad(id);
        restauranteRepository.delete(restaurante);
        log.warn("Restaurante eliminado id={}", id);
    }

    private void validarNombreDisponible(String nombre, Long idActual) {
        if (nombre == null || nombre.trim().isBlank()) {
            throw new ReglaNegocioException("El nombre del restaurante es obligatorio");
        }
        boolean existe = idActual == null
                ? restauranteRepository.existsByNombreIgnoreCase(nombre.trim())
                : restauranteRepository.existsByNombreIgnoreCaseAndIdNot(nombre.trim(), idActual);
        if (existe) {
            throw new ReglaNegocioException("Ya existe un restaurante con ese nombre");
        }
    }

    private void validarEstado(Boolean activo, Boolean abierto) {
        if (Boolean.FALSE.equals(activo) && Boolean.TRUE.equals(abierto)) {
            throw new ReglaNegocioException("Un restaurante inactivo no puede quedar abierto");
        }
    }

    private Restaurante obtenerEntidad(Long id) {
        if (id == null || id <= 0) {
            throw new ReglaNegocioException("El id del restaurante debe ser mayor a cero");
        }
        return restauranteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Restaurante no encontrado con id: " + id));
    }

    private void notificarSiCorresponde(Restaurante restaurante, String titulo, String mensaje) {
        if (notificacionEnabled) {
            notificacionClient.crearNotificacionRestaurante(restaurante.getId(), titulo, mensaje);
        }
    }

    private RestauranteResponse toResponse(Restaurante restaurante) {
        List<HorarioRestauranteResponse> horarios = restaurante.getHorarios() == null ? List.of() :
                restaurante.getHorarios().stream()
                        .map(h -> HorarioRestauranteResponse.builder()
                                .id(h.getId())
                                .restauranteId(restaurante.getId())
                                .diaSemana(h.getDiaSemana())
                                .horaApertura(h.getHoraApertura())
                                .horaCierre(h.getHoraCierre())
                                .build())
                        .toList();
        return RestauranteResponse.builder()
                .id(restaurante.getId())
                .nombre(restaurante.getNombre())
                .descripcion(restaurante.getDescripcion())
                .direccion(restaurante.getDireccion())
                .telefono(restaurante.getTelefono())
                .email(restaurante.getEmail())
                .activo(restaurante.getActivo())
                .abierto(restaurante.getAbierto())
                .horarios(horarios)
                .build();
    }
}
