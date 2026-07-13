package com.foodfast.restaurante_servicio.repository;

import com.foodfast.restaurante_servicio.model.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestauranteRepository extends JpaRepository<Restaurante, Long> {
    List<Restaurante> findByActivoTrue();
    List<Restaurante> findByAbiertoTrue();
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
