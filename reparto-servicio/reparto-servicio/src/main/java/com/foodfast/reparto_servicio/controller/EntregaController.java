package com.foodfast.reparto_servicio.controller;

import com.foodfast.reparto_servicio.dto.ApiResponse;
import com.foodfast.reparto_servicio.dto.CambiarEstadoEntregaRequest;
import com.foodfast.reparto_servicio.dto.EntregaRequest;
import com.foodfast.reparto_servicio.dto.EntregaResponse;
import com.foodfast.reparto_servicio.service.EntregaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Entregas", description = "Gestión de entregas y estados de reparto")
@RestController
@RequestMapping("/api/entregas")
@RequiredArgsConstructor
public class EntregaController {

    private final EntregaService entregaService;

    @Operation(summary = "Verifica estado del recurso entregas")
    @GetMapping("/estado")
    public ResponseEntity<ApiResponse<String>> estado() {
        return ResponseEntity.ok(ApiResponse.ok("reparto-servicio operativo", "OK"));
    }

    @Operation(summary = "Lista todas las entregas")
    @GetMapping
    public ResponseEntity<ApiResponse<List<EntregaResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok("Entregas listadas correctamente", entregaService.listar()));
    }

    @Operation(summary = "Busca una entrega por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EntregaResponse>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Entrega encontrada", entregaService.buscarPorId(id)));
    }

    @Operation(summary = "Lista entregas por pedido")
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<ApiResponse<List<EntregaResponse>>> listarPorPedido(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(ApiResponse.ok("Entregas del pedido listadas correctamente", entregaService.listarPorPedido(pedidoId)));
    }

    @Operation(summary = "Lista entregas por repartidor")
    @GetMapping("/repartidor/{repartidorId}")
    public ResponseEntity<ApiResponse<List<EntregaResponse>>> listarPorRepartidor(@PathVariable Long repartidorId) {
        return ResponseEntity.ok(ApiResponse.ok("Entregas del repartidor listadas correctamente", entregaService.listarPorRepartidor(repartidorId)));
    }

    @Operation(summary = "Crea una entrega", description = "Crea una entrega en estado CREADA o ASIGNADA si se informa repartidorId.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(schema = @Schema(implementation = EntregaRequest.class), examples = @ExampleObject(value = """
                    {
                      "pedidoId": 1,
                      "repartidorId": 1,
                      "direccionEntrega": "Av. FoodFast 123"
                    }
                    """))
    )
    @PostMapping
    public ResponseEntity<ApiResponse<EntregaResponse>> crear(@Valid @RequestBody EntregaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Entrega creada correctamente", entregaService.crear(request)));
    }

    @Operation(summary = "Actualiza una entrega no finalizada")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EntregaResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EntregaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Entrega actualizada correctamente", entregaService.actualizar(id, request)));
    }

    @Operation(summary = "Asigna repartidor a una entrega creada")
    @PatchMapping("/{entregaId}/asignar/{repartidorId}")
    public ResponseEntity<ApiResponse<EntregaResponse>> asignarRepartidor(
            @PathVariable Long entregaId,
            @PathVariable Long repartidorId) {
        return ResponseEntity.ok(ApiResponse.ok("Repartidor asignado correctamente", entregaService.asignarRepartidor(entregaId, repartidorId)));
    }

    @Operation(summary = "Cambia el estado de una entrega")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<EntregaResponse>> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoEntregaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Estado de entrega actualizado correctamente", entregaService.cambiarEstado(id, request)));
    }

    @Operation(summary = "Elimina una entrega")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        entregaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
