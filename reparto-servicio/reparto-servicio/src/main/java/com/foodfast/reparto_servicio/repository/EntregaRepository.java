package com.foodfast.reparto_servicio.repository;

import com.foodfast.reparto_servicio.model.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntregaRepository extends JpaRepository<Entrega, Long> {
    List<Entrega> findByPedidoId(Long pedidoId);
    List<Entrega> findByRepartidorId(Long repartidorId);
    boolean existsByPedidoId(Long pedidoId);
}
