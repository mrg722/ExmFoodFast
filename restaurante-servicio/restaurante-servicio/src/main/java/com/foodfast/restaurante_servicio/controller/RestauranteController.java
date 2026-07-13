package com.foodfast.restaurante_servicio.controller;

import com.foodfast.restaurante_servicio.dto.ApiResponse;
import com.foodfast.restaurante_servicio.dto.RestauranteRequest;
import com.foodfast.restaurante_servicio.dto.RestauranteResponse;
import com.foodfast.restaurante_servicio.service.RestauranteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Restaurantes", description = "Gestión de restaurantes FoodFast")
@RestController
@RequestMapping("/api/restaurantes")
@RequiredArgsConstructor
public class RestauranteController {

    private final RestauranteService restauranteService;

    @Operation(summary = "Estado del microservicio", description = "Permite verificar rápidamente que restaurante-servicio está operativo.")
    @GetMapping("/estado")
    public ResponseEntity<ApiResponse<String>> estado() {
        return ResponseEntity.ok(ApiResponse.ok("restaurante-servicio operativo", "OK"));
    }

    @Operation(summary = "Listar restaurantes", description = "Obtiene todos los restaurantes registrados.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RestauranteResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok("Restaurantes listados", restauranteService.listar()));
    }

    @Operation(summary = "Listar restaurantes activos", description = "Obtiene solo restaurantes activos.")
    @GetMapping("/activos")
    public ResponseEntity<ApiResponse<List<RestauranteResponse>>> listarActivos() {
        return ResponseEntity.ok(ApiResponse.ok("Restaurantes activos listados", restauranteService.listarActivos()));
    }

    @Operation(summary = "Listar restaurantes abiertos", description = "Obtiene solo restaurantes abiertos.")
    @GetMapping("/abiertos")
    public ResponseEntity<ApiResponse<List<RestauranteResponse>>> listarAbiertos() {
        return ResponseEntity.ok(ApiResponse.ok("Restaurantes abiertos listados", restauranteService.listarAbiertos()));
    }

    @Operation(summary = "Buscar restaurante por ID", description = "Obtiene un restaurante por identificador.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestauranteResponse>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Restaurante encontrado", restauranteService.buscarPorId(id)));
    }

    @Operation(summary = "Crear restaurante", description = "Crea un restaurante validando nombre único y estado coherente.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos necesarios para crear un restaurante",
            required = true,
            content = @Content(schema = @Schema(implementation = RestauranteRequest.class), examples = @ExampleObject(value = """
                    {
                      "nombre": "FoodFast Centro",
                      "descripcion": "Sucursal principal",
                      "direccion": "Av. FoodFast 123",
                      "telefono": "+56911112222",
                      "email": "centro@foodfast.cl",
                      "activo": true,
                      "abierto": false
                    }
                    """)))
    @PostMapping
    public ResponseEntity<ApiResponse<RestauranteResponse>> crear(@Valid @RequestBody RestauranteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Restaurante creado", restauranteService.crear(request)));
    }

    @Operation(summary = "Actualizar restaurante", description = "Actualiza datos generales de un restaurante existente.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RestauranteResponse>> actualizar(@PathVariable Long id,
                                                                       @Valid @RequestBody RestauranteRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Restaurante actualizado", restauranteService.actualizar(id, request)));
    }

    @Operation(summary = "Activar o desactivar restaurante", description = "Cambia el estado activo. Si se desactiva, también queda cerrado.")
    @PatchMapping("/{id}/activo")
    public ResponseEntity<ApiResponse<RestauranteResponse>> cambiarActivo(@PathVariable Long id,
                                                                          @RequestParam Boolean activo) {
        return ResponseEntity.ok(ApiResponse.ok("Estado activo actualizado", restauranteService.cambiarActivo(id, activo)));
    }

    @Operation(summary = "Abrir o cerrar restaurante", description = "Cambia el estado abierto. No permite abrir restaurantes inactivos.")
    @PatchMapping("/{id}/estado-abierto")
    public ResponseEntity<ApiResponse<RestauranteResponse>> cambiarEstadoAbierto(@PathVariable Long id,
                                                                                 @RequestParam Boolean abierto) {
        return ResponseEntity.ok(ApiResponse.ok("Estado abierto actualizado", restauranteService.cambiarEstadoAbierto(id, abierto)));
    }

    @Operation(summary = "Eliminar restaurante", description = "Elimina un restaurante existente.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        restauranteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
