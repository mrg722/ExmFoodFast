package com.foodfast.inventario_servicio.repository;

import com.foodfast.inventario_servicio.model.Inventario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    Optional<Inventario> findByProductoId(Long productoId);
    boolean existsByProductoId(Long productoId);
    List<Inventario> findByUbicacionContainingIgnoreCase(String ubicacion);
}
