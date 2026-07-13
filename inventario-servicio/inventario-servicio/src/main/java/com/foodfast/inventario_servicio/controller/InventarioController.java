package com.foodfast.inventario_servicio.controller;

import com.foodfast.inventario_servicio.dto.ApiResponse;
import com.foodfast.inventario_servicio.dto.DescontarStockRequest;
import com.foodfast.inventario_servicio.dto.InventarioRequest;
import com.foodfast.inventario_servicio.dto.InventarioResponse;
import com.foodfast.inventario_servicio.dto.StockResponse;
import com.foodfast.inventario_servicio.service.InventarioService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Inventario", description = "Gestión de inventario y stock de productos FoodFast")
@RestController
@RequestMapping("/api/inventarios")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @Operation(summary = "Estado del microservicio", description = "Permite verificar que inventario-servicio está operativo.")
    @GetMapping("/estado")
    public ResponseEntity<ApiResponse<String>> estado() {
        return ResponseEntity.ok(new ApiResponse<>(true, "inventario-servicio operativo", "OK"));
    }

    @Operation(summary = "Listar inventarios", description = "Retorna todos los registros de inventario disponibles.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<InventarioResponse>>> listar() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventarios listados correctamente", inventarioService.listar()));
    }

    @Operation(summary = "Buscar inventarios por ubicación", description = "Filtra inventarios por texto de ubicación.")
    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<InventarioResponse>>> listarPorUbicacion(@RequestParam String ubicacion) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventarios filtrados correctamente", inventarioService.listarPorUbicacion(ubicacion)));
    }

    @Operation(summary = "Buscar inventario por ID", description = "Retorna un inventario específico por su ID interno.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventarioResponse>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventario encontrado", inventarioService.buscarPorId(id)));
    }

    @Operation(summary = "Buscar inventario por producto", description = "Retorna el inventario asociado a un producto específico.")
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<ApiResponse<InventarioResponse>> buscarPorProductoId(@PathVariable Long productoId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventario encontrado por productoId", inventarioService.buscarPorProductoId(productoId)));
    }

    @Operation(summary = "Consultar stock por producto", description = "Retorna el stock real: cantidad disponible menos cantidad reservada.")
    @GetMapping("/producto/{productoId}/stock")
    public ResponseEntity<ApiResponse<StockResponse>> consultarStock(@PathVariable Long productoId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Stock consultado correctamente", inventarioService.consultarStock(productoId)));
    }

    @Operation(summary = "Consultar stock por producto para pedido-servicio", description = "Alias usado por pedido-servicio para validar stock antes de crear un pedido.")
    @GetMapping("/stock/{productoId}")
    public ResponseEntity<ApiResponse<StockResponse>> consultarStockAlias(@PathVariable Long productoId) {
        return consultarStock(productoId);
    }

    @Operation(
            summary = "Crear inventario",
            description = "Crea un registro de inventario. Regla: productoId único y cantidadReservada no puede superar cantidadDisponible.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            schema = @Schema(implementation = InventarioRequest.class),
                            examples = @ExampleObject(value = "{\"productoId\":1,\"cantidadDisponible\":50,\"cantidadReservada\":5,\"ubicacion\":\"Bodega Central A1\"}")
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Inventario creado correctamente")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio incumplida")
    @PostMapping
    public ResponseEntity<ApiResponse<InventarioResponse>> crear(@Valid @RequestBody InventarioRequest request) {
        InventarioResponse inventario = inventarioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Inventario creado correctamente", inventario));
    }

    @Operation(summary = "Actualizar inventario", description = "Actualiza cantidades y ubicación de un inventario existente.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventarioResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody InventarioRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventario actualizado correctamente", inventarioService.actualizar(id, request)));
    }

    @Operation(summary = "Descontar stock", description = "Descuenta stock real cuando un pedido queda confirmado.")
    @PutMapping("/descontar-stock")
    public ResponseEntity<ApiResponse<InventarioResponse>> descontarStock(
            @Valid @RequestBody DescontarStockRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Stock descontado correctamente", inventarioService.descontarStock(request)));
    }

    @Operation(summary = "Descontar stock alias", description = "Alias POST usado por pedido-servicio: /api/inventarios/descontar.")
    @PostMapping("/descontar")
    public ResponseEntity<ApiResponse<InventarioResponse>> descontarStockAlias(
            @Valid @RequestBody DescontarStockRequest request
    ) {
        return descontarStock(request);
    }

    @Operation(summary = "Reservar stock", description = "Aumenta cantidadReservada cuando un pedido queda pendiente o en preparación.")
    @PostMapping("/reservar")
    public ResponseEntity<ApiResponse<InventarioResponse>> reservarStock(@Valid @RequestBody DescontarStockRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Stock reservado correctamente", inventarioService.reservarStock(request)));
    }

    @Operation(summary = "Liberar reserva", description = "Disminuye cantidadReservada cuando un pedido se cancela o libera stock.")
    @PostMapping("/liberar-reserva")
    public ResponseEntity<ApiResponse<InventarioResponse>> liberarReserva(@Valid @RequestBody DescontarStockRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Reserva liberada correctamente", inventarioService.liberarReserva(request)));
    }

    @Operation(summary = "Eliminar inventario", description = "Elimina un inventario por ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        inventarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
