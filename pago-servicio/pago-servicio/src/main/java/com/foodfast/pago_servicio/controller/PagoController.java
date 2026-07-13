package com.foodfast.pago_servicio.controller;

import com.foodfast.pago_servicio.dto.ActualizarPagoRequest;
import com.foodfast.pago_servicio.dto.ApiResponse;
import com.foodfast.pago_servicio.dto.PagoRequest;
import com.foodfast.pago_servicio.dto.PagoResponse;
import com.foodfast.pago_servicio.dto.ProcesarPagoRequest;
import com.foodfast.pago_servicio.service.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Pagos", description = "Gestión de pagos del ecosistema FoodFast")
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @Operation(summary = "Estado del microservicio", description = "Permite verificar rápidamente que pago-servicio está operativo.")
    @GetMapping("/estado")
    public ResponseEntity<ApiResponse<String>> estado() {
        return ResponseEntity.ok(ApiResponse.ok("pago-servicio operativo", "OK"));
    }

    @Operation(summary = "Listar pagos", description = "Retorna todos los pagos registrados.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PagoResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok("Pagos listados", pagoService.listar()));
    }

    @Operation(summary = "Buscar pago por ID", description = "Retorna un pago según su identificador.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PagoResponse>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Pago encontrado", pagoService.buscarPorId(id)));
    }

    @Operation(summary = "Buscar pagos por pedido", description = "Retorna pagos asociados a un pedido.")
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<ApiResponse<List<PagoResponse>>> buscarPorPedidoId(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(ApiResponse.ok("Pagos del pedido", pagoService.buscarPorPedidoId(pedidoId)));
    }

    @Operation(summary = "Crear pago pendiente", description = "Registra un pago en estado PENDIENTE. Opcionalmente valida el pedido contra pedido-servicio.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Pago creado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Error al consumir pedido-servicio")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<PagoResponse>> crear(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(value = "{\"pedidoId\":1,\"monto\":15990,\"metodoPago\":\"WEBPAY_SIMULADO\"}")))
            @Valid @RequestBody PagoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Pago creado en estado pendiente", pagoService.crear(request)));
    }

    @Operation(summary = "Actualizar pago pendiente", description = "Actualiza monto y método de pago solo si el pago sigue pendiente.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PagoResponse>> actualizar(@PathVariable Long id,
                                                                @Valid @RequestBody ActualizarPagoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Pago actualizado", pagoService.actualizar(id, request)));
    }

    @Operation(summary = "Procesar pago", description = "Aprueba o rechaza un pago pendiente y genera código de transacción.")
    @PostMapping("/procesar")
    public ResponseEntity<ApiResponse<PagoResponse>> procesar(@Valid @RequestBody ProcesarPagoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Pago procesado", pagoService.procesar(request)));
    }

    @Operation(summary = "Anular pago", description = "Anula un pago pendiente o rechazado. No permite anular pagos aprobados.")
    @PatchMapping("/{id}/anular")
    public ResponseEntity<ApiResponse<PagoResponse>> anular(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Pago anulado", pagoService.anular(id)));
    }

    @Operation(summary = "Eliminar pago", description = "Elimina físicamente un pago registrado.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
