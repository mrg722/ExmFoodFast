package com.foodfast.cliente_servicio.repository;

import com.foodfast.cliente_servicio.model.Cliente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    @Override
    @EntityGraph(attributePaths = "direcciones")
    List<Cliente> findAll();

    @Override
    @EntityGraph(attributePaths = "direcciones")
    Optional<Cliente> findById(Long id);

    @EntityGraph(attributePaths = "direcciones")
    Optional<Cliente> findByEmail(String email);

    @EntityGraph(attributePaths = "direcciones")
    List<Cliente> findByActivoTrue();

    boolean existsByEmail(String email);
}
