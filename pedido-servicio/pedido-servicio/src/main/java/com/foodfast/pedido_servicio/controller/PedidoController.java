package com.foodfast.pedido_servicio.controller;

import com.foodfast.pedido_servicio.dto.ApiResponse;
import com.foodfast.pedido_servicio.dto.ErrorResponse;
import com.foodfast.pedido_servicio.dto.PedidoRequest;
import com.foodfast.pedido_servicio.dto.PedidoResponse;
import com.foodfast.pedido_servicio.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Pedidos", description = "Operaciones del microservicio de pedidos FoodFast")
@RestController
@RequestMapping({"/api/pedidos", "/api/v1/pedidos"})
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PedidoController {

    private final PedidoService pedidoService;

    @Operation(
            summary = "Verifica el estado del microservicio",
            description = "Endpoint público para validar que pedido-servicio está levantado."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Microservicio operativo",
            content = @Content(examples = @ExampleObject(value = "pedido-servicio funcionando"))
    )
    @GetMapping("/estado")
    public ResponseEntity<String> estado() {
        return ResponseEntity.ok("pedido-servicio funcionando");
    }

    @Operation(
            summary = "Lista todos los pedidos",
            description = "Retorna los pedidos registrados en el sistema."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pedidos obtenidos correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<PedidoResponse>>> listar() {
        List<PedidoResponse> pedidos = pedidoService.listar();
        return ResponseEntity.ok(ApiResponse.<List<PedidoResponse>>builder()
                .success(true)
                .message("Pedidos obtenidos correctamente")
                .data(pedidos)
                .build());
    }

    @Operation(
            summary = "Busca un pedido por ID",
            description = "Retorna un pedido específico usando su identificador."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pedido obtenido correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PedidoResponse>> buscarPorId(
            @Parameter(description = "ID del pedido", example = "1") @PathVariable Long id) {

        PedidoResponse pedido = pedidoService.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.<PedidoResponse>builder()
                .success(true)
                .message("Pedido obtenido correctamente")
                .data(pedido)
                .build());
    }

    @Operation(
            summary = "Lista pedidos por cliente",
            description = "Retorna todos los pedidos asociados a un cliente."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pedidos del cliente obtenidos correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "ID de cliente inválido", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<ApiResponse<List<PedidoResponse>>> listarPorCliente(
            @Parameter(description = "ID del cliente", example = "1") @PathVariable Long clienteId) {

        List<PedidoResponse> pedidos = pedidoService.listarPorCliente(clienteId);
        return ResponseEntity.ok(ApiResponse.<List<PedidoResponse>>builder()
                .success(true)
                .message("Pedidos del cliente obtenidos correctamente")
                .data(pedidos)
                .build());
    }

    @Operation(
            summary = "Crea un pedido",
            description = "Crea un pedido. Si se envían productoId y cantidad, consulta inventario-servicio y descuenta stock."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Pedido creado correctamente",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "message": "Pedido creado correctamente",
                              "data": {
                                "id": 1,
                                "clienteId": 1,
                                "productoId": 72,
                                "cantidad": 2,
                                "estado": "CONFIRMADO",
                                "total": 15990,
                                "direccionEntrega": "Av. FoodFast 123",
                                "estadoStock": "STOCK DISPONIBLE",
                                "mensajeInventario": "Stock descontado correctamente en inventario-servicio"
                              }
                            }
                            """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Error de validación o regla de negocio", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Error al comunicarse con inventario-servicio", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<PedidoResponse>> crear(
            @Valid @RequestBody PedidoRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        PedidoResponse pedido = pedidoService.crear(request, authorizationHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<PedidoResponse>builder()
                .success(true)
                .message("Pedido creado correctamente")
                .data(pedido)
                .build());
    }

    @Operation(
            summary = "Actualiza un pedido",
            description = "Modifica los datos de un pedido existente. No permite actualizar pedidos cancelados."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pedido actualizado correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Regla de negocio incumplida", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PedidoResponse>> actualizar(
            @Parameter(description = "ID del pedido", example = "1") @PathVariable Long id,
            @Valid @RequestBody PedidoRequest request) {

        PedidoResponse pedido = pedidoService.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.<PedidoResponse>builder()
                .success(true)
                .message("Pedido actualizado correctamente")
                .data(pedido)
                .build());
    }

    @Operation(
            summary = "Cancela un pedido",
            description = "Cambia el estado del pedido a CANCELADO. No permite cancelar dos veces el mismo pedido."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Pedido cancelado correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "El pedido ya está cancelado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(
            @Parameter(description = "ID del pedido", example = "1") @PathVariable Long id) {

        pedidoService.cancelar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Elimina un pedido",
            description = "Elimina físicamente un pedido existente."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Pedido eliminado correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del pedido", example = "1") @PathVariable Long id) {

        pedidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
