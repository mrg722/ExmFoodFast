package com.foodfast.resena_servicio.controller;

import com.foodfast.resena_servicio.dto.ApiResponse;
import com.foodfast.resena_servicio.dto.PromedioResenaResponse;
import com.foodfast.resena_servicio.dto.ResenaRequest;
import com.foodfast.resena_servicio.dto.ResenaResponse;
import com.foodfast.resena_servicio.service.ResenaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reseñas", description = "Gestión de reseñas, calificaciones y promedios de productos")
@RestController
@RequestMapping("/api/resenas")
@RequiredArgsConstructor
public class ResenaController {

    private final ResenaService resenaService;

    @Operation(summary = "Estado del microservicio", description = "Permite validar rápidamente que resena-servicio está operativo.")
    @GetMapping("/estado")
    public ResponseEntity<ApiResponse<String>> estado() {
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("resena-servicio operativo")
                .data("OK")
                .build());
    }

    @Operation(summary = "Listar todas las reseñas", description = "Devuelve todas las reseñas registradas en el sistema.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ResenaResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.<List<ResenaResponse>>builder()
                .success(true)
                .message("Reseñas listadas correctamente")
                .data(resenaService.listar())
                .build());
    }

    @Operation(summary = "Listar reseñas activas", description = "Devuelve solo las reseñas activas y visibles.")
    @GetMapping("/activas")
    public ResponseEntity<ApiResponse<List<ResenaResponse>>> listarActivas() {
        return ResponseEntity.ok(ApiResponse.<List<ResenaResponse>>builder()
                .success(true)
                .message("Reseñas activas listadas correctamente")
                .data(resenaService.listarActivas())
                .build());
    }

    @Operation(summary = "Buscar reseña por ID", description = "Devuelve una reseña específica según su identificador.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResenaResponse>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<ResenaResponse>builder()
                .success(true)
                .message("Reseña encontrada")
                .data(resenaService.buscarPorId(id))
                .build());
    }

    @Operation(summary = "Listar reseñas por producto", description = "Devuelve todas las reseñas asociadas a un producto.")
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<ApiResponse<List<ResenaResponse>>> listarPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(ApiResponse.<List<ResenaResponse>>builder()
                .success(true)
                .message("Reseñas por producto listadas correctamente")
                .data(resenaService.listarPorProducto(productoId))
                .build());
    }

    @Operation(summary = "Listar reseñas por cliente", description = "Devuelve todas las reseñas realizadas por un cliente.")
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<ApiResponse<List<ResenaResponse>>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(ApiResponse.<List<ResenaResponse>>builder()
                .success(true)
                .message("Reseñas por cliente listadas correctamente")
                .data(resenaService.listarPorCliente(clienteId))
                .build());
    }

    @Operation(summary = "Calcular promedio por producto", description = "Calcula promedio y total de reseñas activas para un producto.")
    @GetMapping("/producto/{productoId}/promedio")
    public ResponseEntity<ApiResponse<PromedioResenaResponse>> promedioPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(ApiResponse.<PromedioResenaResponse>builder()
                .success(true)
                .message("Promedio calculado correctamente")
                .data(resenaService.promedioPorProducto(productoId))
                .build());
    }

    @Operation(summary = "Crear reseña", description = "Crea una reseña nueva. Un cliente solo puede reseñar una vez el mismo producto.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Reseña creada correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos o reseña duplicada")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ResenaResponse>> crear(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de la reseña",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ResenaRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "clienteId": 1,
                                      "productoId": 10,
                                      "restauranteId": 2,
                                      "calificacion": 5,
                                      "comentario": "Muy buen producto, llegó rápido y caliente"
                                    }
                                    """)))
            @Valid @RequestBody ResenaRequest request) {
        ResenaResponse creada = resenaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<ResenaResponse>builder()
                .success(true)
                .message("Reseña creada correctamente")
                .data(creada)
                .build());
    }

    @Operation(summary = "Actualizar reseña", description = "Actualiza una reseña activa existente.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ResenaResponse>> actualizar(@PathVariable Long id, @Valid @RequestBody ResenaRequest request) {
        return ResponseEntity.ok(ApiResponse.<ResenaResponse>builder()
                .success(true)
                .message("Reseña actualizada correctamente")
                .data(resenaService.actualizar(id, request))
                .build());
    }

    @Operation(summary = "Desactivar reseña", description = "Marca una reseña como inactiva sin eliminarla físicamente.")
    @PutMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponse<ResenaResponse>> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<ResenaResponse>builder()
                .success(true)
                .message("Reseña desactivada correctamente")
                .data(resenaService.desactivar(id))
                .build());
    }

    @Operation(summary = "Eliminar reseña", description = "Elimina físicamente una reseña por ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        resenaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
