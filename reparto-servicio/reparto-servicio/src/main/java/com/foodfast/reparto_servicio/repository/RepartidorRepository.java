package com.foodfast.reparto_servicio.repository;

import com.foodfast.reparto_servicio.model.Repartidor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepartidorRepository extends JpaRepository<Repartidor, Long> {
    List<Repartidor> findByActivoTrueAndDisponibleTrue();
}