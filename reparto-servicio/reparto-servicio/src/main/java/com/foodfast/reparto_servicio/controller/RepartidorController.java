package com.foodfast.reparto_servicio.controller;

import com.foodfast.reparto_servicio.dto.ApiResponse;
import com.foodfast.reparto_servicio.dto.RepartidorRequest;
import com.foodfast.reparto_servicio.dto.RepartidorResponse;
import com.foodfast.reparto_servicio.service.RepartidorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Repartidores", description = "Gestión de repartidores del ecosistema FoodFast")
@RestController
@RequestMapping("/api/repartidores")
@RequiredArgsConstructor
public class RepartidorController {

    private final RepartidorService repartidorService;

    @Operation(summary = "Verifica estado del recurso repartidores")
    @GetMapping("/estado")
    public ResponseEntity<ApiResponse<String>> estado() {
        return ResponseEntity.ok(ApiResponse.ok("reparto-servicio operativo", "OK"));
    }

    @Operation(summary = "Lista todos los repartidores")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Repartidores listados correctamente")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<RepartidorResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok("Repartidores listados correctamente", repartidorService.listar()));
    }

    @Operation(summary = "Busca un repartidor por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RepartidorResponse>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Repartidor encontrado", repartidorService.buscarPorId(id)));
    }

    @Operation(summary = "Lista repartidores activos y disponibles")
    @GetMapping("/disponibles")
    public ResponseEntity<ApiResponse<List<RepartidorResponse>>> listarDisponibles() {
        return ResponseEntity.ok(ApiResponse.ok("Repartidores disponibles listados correctamente", repartidorService.listarDisponibles()));
    }

    @Operation(summary = "Crea un repartidor", description = "Registra un repartidor con estado activo y disponibilidad inicial.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(schema = @Schema(implementation = RepartidorRequest.class), examples = @ExampleObject(value = """
                    {
                      "nombre": "Juan Perez",
                      "telefono": "+56911112222",
                      "vehiculo": "Moto",
                      "activo": true,
                      "disponible": true
                    }
                    """))
    )
    @PostMapping
    public ResponseEntity<ApiResponse<RepartidorResponse>> crear(@Valid @RequestBody RepartidorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Repartidor creado correctamente", repartidorService.crear(request)));
    }

    @Operation(summary = "Actualiza un repartidor")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RepartidorResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody RepartidorRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Repartidor actualizado correctamente", repartidorService.actualizar(id, request)));
    }

    @Operation(summary = "Cambia disponibilidad de un repartidor")
    @PatchMapping("/{id}/disponibilidad")
    public ResponseEntity<ApiResponse<RepartidorResponse>> cambiarDisponibilidad(
            @PathVariable Long id,
            @RequestBody Map<String, @NotNull Boolean> body) {
        Boolean disponible = body.get("disponible");
        return ResponseEntity.ok(ApiResponse.ok("Disponibilidad actualizada correctamente", repartidorService.cambiarDisponibilidad(id, disponible)));
    }

    @Operation(summary = "Desactiva un repartidor")
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponse<RepartidorResponse>> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Repartidor desactivado correctamente", repartidorService.desactivar(id)));
    }

    @Operation(summary = "Elimina un repartidor sin entregas asociadas")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        repartidorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
