package com.foodfast.catalogo_servicio.service;

import com.foodfast.catalogo_servicio.dto.CategoriaRequest;
import com.foodfast.catalogo_servicio.dto.CategoriaResponse;
import com.foodfast.catalogo_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.catalogo_servicio.exception.ReglaNegocioException;
import com.foodfast.catalogo_servicio.model.Categoria;
import com.foodfast.catalogo_servicio.repository.CategoriaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarActivas() {
        return categoriaRepository.findByActivaTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscarPorId(Long id) {
        return toResponse(obtenerEntidad(id));
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        validarRequest(request);
        validarNombreDuplicado(request.getNombre());

        Categoria categoria = new Categoria();
        copiarDatos(request, categoria);

        Categoria guardada = categoriaRepository.save(categoria);
        log.info("Categoría creada id={} nombre={}", guardada.getId(), guardada.getNombre());
        return toResponse(guardada);
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        validarRequest(request);
        Categoria categoria = obtenerEntidad(id);

        if (!categoria.getNombre().equalsIgnoreCase(request.getNombre().trim())) {
            validarNombreDuplicado(request.getNombre());
        }

        copiarDatos(request, categoria);
        Categoria actualizada = categoriaRepository.save(categoria);
        log.info("Categoría actualizada id={}", actualizada.getId());
        return toResponse(actualizada);
    }

    @Transactional
    public CategoriaResponse cambiarEstado(Long id, boolean activa) {
        Categoria categoria = obtenerEntidad(id);
        categoria.setActiva(activa);
        Categoria actualizada = categoriaRepository.save(categoria);
        log.info("Categoría id={} activa={}", id, activa);
        return toResponse(actualizada);
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = obtenerEntidad(id);
        categoriaRepository.delete(categoria);
        log.info("Categoría eliminada id={}", id);
    }

    @Transactional(readOnly = true)
    public Categoria obtenerEntidad(Long id) {
        if (id == null || id <= 0) {
            throw new ReglaNegocioException("El id de categoría debe ser mayor que cero");
        }
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada con id: " + id));
    }

    private void validarRequest(CategoriaRequest request) {
        if (request == null) {
            throw new ReglaNegocioException("La solicitud de categoría es obligatoria");
        }
        if (request.getNombre() == null || request.getNombre().trim().isBlank()) {
            throw new ReglaNegocioException("El nombre de la categoría es obligatorio");
        }
        if (request.getDescripcion() == null || request.getDescripcion().trim().isBlank()) {
            throw new ReglaNegocioException("La descripción de la categoría es obligatoria");
        }
        if (request.getActiva() == null) {
            throw new ReglaNegocioException("El estado activa es obligatorio");
        }
    }

    private void validarNombreDuplicado(String nombre) {
        if (categoriaRepository.existsByNombreIgnoreCase(nombre.trim())) {
            throw new ReglaNegocioException("Ya existe una categoría con el nombre: " + nombre);
        }
    }

    private void copiarDatos(CategoriaRequest request, Categoria categoria) {
        categoria.setNombre(request.getNombre().trim());
        categoria.setDescripcion(request.getDescripcion().trim());
        categoria.setActiva(request.getActiva());
    }

    private CategoriaResponse toResponse(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.getActiva()
        );
    }
}
