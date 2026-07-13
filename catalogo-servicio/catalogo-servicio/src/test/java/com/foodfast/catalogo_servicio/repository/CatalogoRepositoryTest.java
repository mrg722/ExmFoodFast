package com.foodfast.catalogo_servicio.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.foodfast.catalogo_servicio.model.Categoria;
import com.foodfast.catalogo_servicio.model.Producto;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class CatalogoRepositoryTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Test
    void categoriaRepository_debeGuardarBuscarListarYEliminar() {
        Categoria categoria = new Categoria(null, "Bebidas", "Bebidas frías", true);

        Categoria guardada = categoriaRepository.save(categoria);

        assertNotNull(guardada.getId());
        assertTrue(categoriaRepository.findById(guardada.getId()).isPresent());
        assertEquals(1, categoriaRepository.findByActivaTrue().size());
        assertTrue(categoriaRepository.existsByNombreIgnoreCase("bebidas"));

        categoriaRepository.delete(guardada);

        assertFalse(categoriaRepository.findById(guardada.getId()).isPresent());
    }

    @Test
    void productoRepository_debeGuardarBuscarPorCategoriaYDisponibles() {
        Categoria categoria = categoriaRepository.save(new Categoria(null, "Comida rápida", "Categoría", true));
        Producto producto = new Producto(null, "Completo", "Completo italiano", new BigDecimal("2990"), true, categoria);

        Producto guardado = productoRepository.save(producto);

        assertNotNull(guardado.getId());
        assertTrue(productoRepository.findById(guardado.getId()).isPresent());
        assertTrue(productoRepository.existsByNombreIgnoreCase("completo"));
        assertEquals(1, productoRepository.findByDisponibleTrue().size());
        assertEquals(1, productoRepository.findByCategoriaId(categoria.getId()).size());
        assertEquals(1, productoRepository.findByCategoriaIdAndDisponibleTrue(categoria.getId()).size());
    }

    @Test
    void productoRepository_debeNoListarNoDisponiblesEnDisponibles() {
        Categoria categoria = categoriaRepository.save(new Categoria(null, "Promos", "Promociones", true));
        productoRepository.save(new Producto(null, "Promo antigua", "No disponible", new BigDecimal("1990"), false, categoria));

        assertEquals(0, productoRepository.findByDisponibleTrue().size());
        assertEquals(0, productoRepository.findByCategoriaIdAndDisponibleTrue(categoria.getId()).size());
    }
}
