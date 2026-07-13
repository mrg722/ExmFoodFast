package com.foodfast.restaurante_servicio.repository;

import com.foodfast.restaurante_servicio.model.HorarioRestaurante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HorarioRestauranteRepository extends JpaRepository<HorarioRestaurante, Long> {
    List<HorarioRestaurante> findByRestauranteId(Long restauranteId);
    boolean existsByRestauranteIdAndDiaSemanaIgnoreCase(Long restauranteId, String diaSemana);
    boolean existsByRestauranteIdAndDiaSemanaIgnoreCaseAndIdNot(Long restauranteId, String diaSemana, Long id);
}
