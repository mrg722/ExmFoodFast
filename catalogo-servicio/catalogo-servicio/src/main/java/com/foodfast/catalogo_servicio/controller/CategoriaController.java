package com.foodfast.catalogo_servicio.controller;

import com.foodfast.catalogo_servicio.dto.ApiResponse;
import com.foodfast.catalogo_servicio.dto.CategoriaRequest;
import com.foodfast.catalogo_servicio.dto.CategoriaResponse;
import com.foodfast.catalogo_servicio.service.CategoriaService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Categorías", description = "Gestión de categorías del catálogo FoodFast")
@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @Operation(summary = "Estado del módulo de categorías", description = "Endpoint rápido para validar que el controlador responde.")
    @GetMapping("/estado")
    public ResponseEntity<ApiResponse<String>> estado() {
        return ResponseEntity.ok(new ApiResponse<>(true, "catalogo-servicio operativo", "OK"));
    }

    @Operation(summary = "Listar categorías", description = "Retorna todas las categorías registradas.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoriaResponse>>> listar() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Categorías listadas correctamente", categoriaService.listar()));
    }

    @Operation(summary = "Listar categorías activas", description = "Retorna solo categorías activas, útiles para crear productos.")
    @GetMapping("/activas")
    public ResponseEntity<ApiResponse<List<CategoriaResponse>>> listarActivas() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Categorías activas listadas correctamente", categoriaService.listarActivas()));
    }

    @Operation(summary = "Buscar categoría por ID", description = "Busca una categoría por su identificador.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoriaResponse>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Categoría encontrada", categoriaService.buscarPorId(id)));
    }

    @Operation(summary = "Crear categoría", description = "Crea una nueva categoría del catálogo.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Categoría creada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CategoriaResponse>> crear(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de la categoría",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CategoriaRequest.class),
                            examples = @ExampleObject(value = "{\"nombre\":\"Hamburguesas\",\"descripcion\":\"Comida rápida\",\"activa\":true}"))
            )
            @Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Categoría creada correctamente", categoriaService.crear(request)));
    }

    @Operation(summary = "Actualizar categoría", description = "Actualiza nombre, descripción y estado de una categoría.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoriaResponse>> actualizar(@PathVariable Long id,
                                                                      @Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Categoría actualizada correctamente", categoriaService.actualizar(id, request)));
    }

    @Operation(summary = "Activar o desactivar categoría", description = "Cambia el estado activo de una categoría sin modificar sus otros datos.")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<CategoriaResponse>> cambiarEstado(@PathVariable Long id,
                                                                         @RequestParam boolean activa) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Estado de categoría actualizado", categoriaService.cambiarEstado(id, activa)));
    }

    @Operation(summary = "Eliminar categoría", description = "Elimina una categoría por ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
