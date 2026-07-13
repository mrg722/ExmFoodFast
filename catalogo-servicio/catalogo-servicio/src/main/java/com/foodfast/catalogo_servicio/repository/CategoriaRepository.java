package com.foodfast.catalogo_servicio.repository;

import com.foodfast.catalogo_servicio.model.Categoria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
    List<Categoria> findByActivaTrue();
}
