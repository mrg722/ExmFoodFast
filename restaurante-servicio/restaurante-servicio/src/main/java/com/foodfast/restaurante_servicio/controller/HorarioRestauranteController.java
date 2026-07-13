package com.foodfast.restaurante_servicio.controller;

import com.foodfast.restaurante_servicio.dto.ApiResponse;
import com.foodfast.restaurante_servicio.dto.HorarioRestauranteRequest;
import com.foodfast.restaurante_servicio.dto.HorarioRestauranteResponse;
import com.foodfast.restaurante_servicio.service.HorarioRestauranteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Horarios Restaurante", description = "Gestión de horarios de atención de restaurantes")
@RestController
@RequestMapping("/api/horarios-restaurante")
@RequiredArgsConstructor
public class HorarioRestauranteController {

    private final HorarioRestauranteService horarioService;

    @Operation(summary = "Listar horarios por restaurante", description = "Obtiene los horarios asociados a un restaurante.")
    @GetMapping("/restaurante/{restauranteId}")
    public ResponseEntity<ApiResponse<List<HorarioRestauranteResponse>>> listarPorRestaurante(@PathVariable Long restauranteId) {
        return ResponseEntity.ok(ApiResponse.ok("Horarios listados", horarioService.listarPorRestaurante(restauranteId)));
    }

    @Operation(summary = "Buscar horario por ID", description = "Obtiene un horario por identificador.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HorarioRestauranteResponse>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Horario encontrado", horarioService.buscarPorId(id)));
    }

    @Operation(summary = "Crear horario", description = "Crea un horario validando que cierre sea posterior a apertura y que no se duplique el día.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del horario",
            required = true,
            content = @Content(schema = @Schema(implementation = HorarioRestauranteRequest.class), examples = @ExampleObject(value = """
                    {
                      "restauranteId": 1,
                      "diaSemana": "LUNES",
                      "horaApertura": "10:00:00",
                      "horaCierre": "22:00:00"
                    }
                    """)))
    @PostMapping
    public ResponseEntity<ApiResponse<HorarioRestauranteResponse>> crear(@Valid @RequestBody HorarioRestauranteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Horario creado", horarioService.crear(request)));
    }

    @Operation(summary = "Actualizar horario", description = "Actualiza un horario existente.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HorarioRestauranteResponse>> actualizar(@PathVariable Long id,
                                                                              @Valid @RequestBody HorarioRestauranteRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Horario actualizado", horarioService.actualizar(id, request)));
    }

    @Operation(summary = "Eliminar horario", description = "Elimina un horario existente.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        horarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
