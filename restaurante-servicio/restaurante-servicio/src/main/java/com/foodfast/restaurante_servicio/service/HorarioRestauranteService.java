package com.foodfast.restaurante_servicio.service;

import com.foodfast.restaurante_servicio.dto.HorarioRestauranteRequest;
import com.foodfast.restaurante_servicio.dto.HorarioRestauranteResponse;
import com.foodfast.restaurante_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.restaurante_servicio.exception.ReglaNegocioException;
import com.foodfast.restaurante_servicio.model.HorarioRestaurante;
import com.foodfast.restaurante_servicio.model.Restaurante;
import com.foodfast.restaurante_servicio.repository.HorarioRestauranteRepository;
import com.foodfast.restaurante_servicio.repository.RestauranteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HorarioRestauranteService {

    private final HorarioRestauranteRepository horarioRepository;
    private final RestauranteRepository restauranteRepository;

    @Transactional(readOnly = true)
    public List<HorarioRestauranteResponse> listarPorRestaurante(Long restauranteId) {
        validarId(restauranteId, "restauranteId");
        return horarioRepository.findByRestauranteId(restauranteId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public HorarioRestauranteResponse buscarPorId(Long id) {
        return toResponse(obtenerHorario(id));
    }

    @Transactional
    public HorarioRestauranteResponse crear(HorarioRestauranteRequest request) {
        validarHoras(request);
        Restaurante restaurante = restauranteRepository.findById(request.getRestauranteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Restaurante no encontrado con id: " + request.getRestauranteId()));
        if (horarioRepository.existsByRestauranteIdAndDiaSemanaIgnoreCase(request.getRestauranteId(), request.getDiaSemana())) {
            throw new ReglaNegocioException("Ya existe un horario para ese día en el restaurante");
        }

        HorarioRestaurante horario = HorarioRestaurante.builder()
                .restaurante(restaurante)
                .diaSemana(request.getDiaSemana().trim().toUpperCase())
                .horaApertura(request.getHoraApertura())
                .horaCierre(request.getHoraCierre())
                .build();
        return toResponse(horarioRepository.save(horario));
    }

    @Transactional
    public HorarioRestauranteResponse actualizar(Long id, HorarioRestauranteRequest request) {
        validarHoras(request);
        HorarioRestaurante horario = obtenerHorario(id);
        Restaurante restaurante = restauranteRepository.findById(request.getRestauranteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Restaurante no encontrado con id: " + request.getRestauranteId()));
        if (horarioRepository.existsByRestauranteIdAndDiaSemanaIgnoreCaseAndIdNot(request.getRestauranteId(), request.getDiaSemana(), id)) {
            throw new ReglaNegocioException("Ya existe otro horario para ese día en el restaurante");
        }

        horario.setRestaurante(restaurante);
        horario.setDiaSemana(request.getDiaSemana().trim().toUpperCase());
        horario.setHoraApertura(request.getHoraApertura());
        horario.setHoraCierre(request.getHoraCierre());
        return toResponse(horarioRepository.save(horario));
    }

    @Transactional
    public void eliminar(Long id) {
        HorarioRestaurante horario = obtenerHorario(id);
        horarioRepository.delete(horario);
        log.warn("Horario eliminado id={}", id);
    }

    private HorarioRestaurante obtenerHorario(Long id) {
        validarId(id, "id");
        return horarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Horario no encontrado con id: " + id));
    }

    private void validarId(Long id, String campo) {
        if (id == null || id <= 0) {
            throw new ReglaNegocioException("El campo " + campo + " debe ser mayor a cero");
        }
    }

    private void validarHoras(HorarioRestauranteRequest request) {
        if (request.getHoraApertura() == null || request.getHoraCierre() == null) {
            throw new ReglaNegocioException("Las horas de apertura y cierre son obligatorias");
        }
        if (!request.getHoraCierre().isAfter(request.getHoraApertura())) {
            throw new ReglaNegocioException("La hora de cierre debe ser posterior a la apertura");
        }
    }

    private HorarioRestauranteResponse toResponse(HorarioRestaurante horario) {
        return HorarioRestauranteResponse.builder()
                .id(horario.getId())
                .restauranteId(horario.getRestaurante().getId())
                .diaSemana(horario.getDiaSemana())
                .horaApertura(horario.getHoraApertura())
                .horaCierre(horario.getHoraCierre())
                .build();
    }
}
