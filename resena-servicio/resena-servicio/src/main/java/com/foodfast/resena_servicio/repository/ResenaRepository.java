package com.foodfast.resena_servicio.repository;

import com.foodfast.resena_servicio.model.Resena;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findByProductoId(Long productoId);
    List<Resena> findByClienteId(Long clienteId);
    List<Resena> findByActivaTrue();
    Optional<Resena> findByClienteIdAndProductoId(Long clienteId, Long productoId);
    boolean existsByClienteIdAndProductoId(Long clienteId, Long productoId);
    long countByProductoIdAndActivaTrue(Long productoId);

    @Query("SELECT AVG(r.calificacion) FROM Resena r WHERE r.productoId = :productoId AND r.activa = true")
    Double promedioPorProducto(@Param("productoId") Long productoId);
}
