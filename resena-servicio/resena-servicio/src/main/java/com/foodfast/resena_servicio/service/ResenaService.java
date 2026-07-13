package com.foodfast.resena_servicio.service;

import com.foodfast.resena_servicio.client.CatalogoClient;
import com.foodfast.resena_servicio.dto.ProductoResponse;
import com.foodfast.resena_servicio.dto.PromedioResenaResponse;
import com.foodfast.resena_servicio.dto.ResenaRequest;
import com.foodfast.resena_servicio.dto.ResenaResponse;
import com.foodfast.resena_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.resena_servicio.exception.ReglaNegocioException;
import com.foodfast.resena_servicio.model.Resena;
import com.foodfast.resena_servicio.repository.ResenaRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final CatalogoClient catalogoClient;

    @Value("${services.catalogo.enabled:false}")
    private boolean catalogoIntegrationEnabled;

    @Transactional(readOnly = true)
    public List<ResenaResponse> listar() {
        return resenaRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ResenaResponse> listarActivas() {
        return resenaRepository.findByActivaTrue().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ResenaResponse buscarPorId(Long id) {
        return toResponse(obtenerResena(id));
    }

    @Transactional(readOnly = true)
    public List<ResenaResponse> listarPorProducto(Long productoId) {
        validarId(productoId, "productoId");
        return resenaRepository.findByProductoId(productoId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ResenaResponse> listarPorCliente(Long clienteId) {
        validarId(clienteId, "clienteId");
        return resenaRepository.findByClienteId(clienteId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PromedioResenaResponse promedioPorProducto(Long productoId) {
        validarId(productoId, "productoId");
        Double promedio = resenaRepository.promedioPorProducto(productoId);
        long total = resenaRepository.countByProductoIdAndActivaTrue(productoId);
        return PromedioResenaResponse.builder()
                .productoId(productoId)
                .promedio(promedio == null ? 0.0 : promedio)
                .totalResenas(total)
                .build();
    }

    @Transactional
    public ResenaResponse crear(ResenaRequest request) {
        validarProductoSiCorresponde(request.getProductoId());
        validarComentario(request.getComentario());

        if (resenaRepository.existsByClienteIdAndProductoId(request.getClienteId(), request.getProductoId())) {
            throw new ReglaNegocioException("El cliente ya registró una reseña para este producto");
        }

        Resena resena = Resena.builder()
                .clienteId(request.getClienteId())
                .productoId(request.getProductoId())
                .restauranteId(request.getRestauranteId())
                .calificacion(request.getCalificacion())
                .comentario(request.getComentario().trim())
                .activa(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        Resena guardada = resenaRepository.save(resena);
        log.info("Reseña creada id={} clienteId={} productoId={}", guardada.getId(), guardada.getClienteId(), guardada.getProductoId());
        return toResponse(guardada);
    }

    @Transactional
    public ResenaResponse actualizar(Long id, ResenaRequest request) {
        validarProductoSiCorresponde(request.getProductoId());
        validarComentario(request.getComentario());
        Resena resena = obtenerResena(id);

        if (Boolean.FALSE.equals(resena.getActiva())) {
            throw new ReglaNegocioException("No se puede actualizar una reseña desactivada");
        }

        resenaRepository.findByClienteIdAndProductoId(request.getClienteId(), request.getProductoId())
                .ifPresent(encontrada -> {
                    if (!encontrada.getId().equals(id)) {
                        throw new ReglaNegocioException("Otro registro ya usa el mismo clienteId y productoId");
                    }
                });

        resena.setClienteId(request.getClienteId());
        resena.setProductoId(request.getProductoId());
        resena.setRestauranteId(request.getRestauranteId());
        resena.setCalificacion(request.getCalificacion());
        resena.setComentario(request.getComentario().trim());
        resena.setFechaActualizacion(LocalDateTime.now());

        return toResponse(resenaRepository.save(resena));
    }

    @Transactional
    public ResenaResponse desactivar(Long id) {
        Resena resena = obtenerResena(id);
        if (Boolean.FALSE.equals(resena.getActiva())) {
            throw new ReglaNegocioException("La reseña ya se encuentra desactivada");
        }
        resena.setActiva(false);
        resena.setFechaActualizacion(LocalDateTime.now());
        return toResponse(resenaRepository.save(resena));
    }

    @Transactional
    public void eliminar(Long id) {
        Resena resena = obtenerResena(id);
        resenaRepository.delete(resena);
    }

    private void validarProductoSiCorresponde(Long productoId) {
        if (!catalogoIntegrationEnabled) {
            return;
        }
        ProductoResponse producto = catalogoClient.buscarProducto(productoId);
        if (Boolean.FALSE.equals(producto.getActivo())) {
            throw new ReglaNegocioException("No se puede reseñar un producto inactivo");
        }
    }

    private Resena obtenerResena(Long id) {
        validarId(id, "id");
        return resenaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reseña no encontrada con id: " + id));
    }

    private void validarId(Long id, String campo) {
        if (id == null || id <= 0) {
            throw new ReglaNegocioException("El campo " + campo + " debe ser mayor a cero");
        }
    }

    private void validarComentario(String comentario) {
        if (comentario == null || comentario.trim().length() < 3) {
            throw new ReglaNegocioException("El comentario debe tener al menos 3 caracteres reales");
        }
    }

    private ResenaResponse toResponse(Resena resena) {
        return ResenaResponse.builder()
                .id(resena.getId())
                .clienteId(resena.getClienteId())
                .productoId(resena.getProductoId())
                .restauranteId(resena.getRestauranteId())
                .calificacion(resena.getCalificacion())
                .comentario(resena.getComentario())
                .activa(resena.getActiva())
                .fechaCreacion(resena.getFechaCreacion())
                .fechaActualizacion(resena.getFechaActualizacion())
                .build();
    }
}
