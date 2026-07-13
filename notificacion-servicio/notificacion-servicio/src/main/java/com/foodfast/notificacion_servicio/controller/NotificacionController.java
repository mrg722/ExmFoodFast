package com.foodfast.notificacion_servicio.controller;

import com.foodfast.notificacion_servicio.dto.ApiResponse;
import com.foodfast.notificacion_servicio.dto.NotificacionPedidoRequest;
import com.foodfast.notificacion_servicio.dto.NotificacionRequest;
import com.foodfast.notificacion_servicio.dto.NotificacionResponse;
import com.foodfast.notificacion_servicio.model.EstadoNotificacion;
import com.foodfast.notificacion_servicio.service.NotificacionService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notificaciones", description = "Gestión de notificaciones FoodFast")
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @Operation(summary = "Verifica si el microservicio está operativo")
    @GetMapping("/estado")
    public ResponseEntity<ApiResponse<String>> estado() {
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("notificacion-servicio operativo")
                .data("OK")
                .build());
    }

    @Operation(summary = "Lista todas las notificaciones")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificacionResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.<List<NotificacionResponse>>builder()
                .success(true)
                .message("Notificaciones listadas correctamente")
                .data(notificacionService.listar())
                .build());
    }

    @Operation(summary = "Busca una notificación por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificacionResponse>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<NotificacionResponse>builder()
                .success(true)
                .message("Notificación encontrada")
                .data(notificacionService.buscarPorId(id))
                .build());
    }

    @Operation(summary = "Lista notificaciones por cliente")
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<ApiResponse<List<NotificacionResponse>>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(ApiResponse.<List<NotificacionResponse>>builder()
                .success(true)
                .message("Notificaciones por cliente listadas correctamente")
                .data(notificacionService.listarPorCliente(clienteId))
                .build());
    }

    @Operation(summary = "Lista notificaciones por estado")
    @GetMapping("/estado/{estado}")
    public ResponseEntity<ApiResponse<List<NotificacionResponse>>> listarPorEstado(@PathVariable EstadoNotificacion estado) {
        return ResponseEntity.ok(ApiResponse.<List<NotificacionResponse>>builder()
                .success(true)
                .message("Notificaciones por estado listadas correctamente")
                .data(notificacionService.listarPorEstado(estado))
                .build());
    }

    @Operation(summary = "Lista notificaciones por cliente y estado")
    @GetMapping("/cliente/{clienteId}/estado/{estado}")
    public ResponseEntity<ApiResponse<List<NotificacionResponse>>> listarPorClienteYEstado(
            @PathVariable Long clienteId,
            @PathVariable EstadoNotificacion estado) {
        return ResponseEntity.ok(ApiResponse.<List<NotificacionResponse>>builder()
                .success(true)
                .message("Notificaciones por cliente y estado listadas correctamente")
                .data(notificacionService.listarPorClienteYEstado(clienteId, estado))
                .build());
    }

    @Operation(summary = "Crea una notificación manual")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Notificación creada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<NotificacionResponse>> crear(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de la notificación",
                    content = @Content(examples = @ExampleObject(value = """
                            {
                              "clienteId": 1,
                              "tipo": "PEDIDO_CONFIRMADO",
                              "canal": "EMAIL",
                              "titulo": "Pedido confirmado",
                              "mensaje": "Tu pedido fue confirmado correctamente",
                              "referenciaTipo": "PEDIDO",
                              "referenciaId": 1
                            }
                            """)))
            @Valid @RequestBody NotificacionRequest request) {
        NotificacionResponse creada = notificacionService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<NotificacionResponse>builder()
                .success(true)
                .message("Notificación creada correctamente")
                .data(creada)
                .build());
    }

    @Operation(summary = "Crea una notificación asociada a un pedido")
    @PostMapping("/pedido")
    public ResponseEntity<ApiResponse<NotificacionResponse>> crearParaPedido(@Valid @RequestBody NotificacionPedidoRequest request) {
        NotificacionResponse creada = notificacionService.crearParaPedido(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<NotificacionResponse>builder()
                .success(true)
                .message("Notificación de pedido creada correctamente")
                .data(creada)
                .build());
    }

    @Operation(summary = "Actualiza una notificación pendiente")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificacionResponse>> actualizar(@PathVariable Long id,
                                                                         @Valid @RequestBody NotificacionRequest request) {
        return ResponseEntity.ok(ApiResponse.<NotificacionResponse>builder()
                .success(true)
                .message("Notificación actualizada correctamente")
                .data(notificacionService.actualizar(id, request))
                .build());
    }

    @Operation(summary = "Marca una notificación como enviada")
    @PutMapping("/{id}/enviar")
    public ResponseEntity<ApiResponse<NotificacionResponse>> enviar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<NotificacionResponse>builder()
                .success(true)
                .message("Notificación enviada correctamente")
                .data(notificacionService.enviar(id))
                .build());
    }

    @Operation(summary = "Registra error de envío en una notificación")
    @PutMapping("/{id}/error")
    public ResponseEntity<ApiResponse<NotificacionResponse>> registrarError(@PathVariable Long id,
                                                                             @RequestParam(required = false) String motivo) {
        return ResponseEntity.ok(ApiResponse.<NotificacionResponse>builder()
                .success(true)
                .message("Error de envío registrado correctamente")
                .data(notificacionService.registrarErrorEnvio(id, motivo))
                .build());
    }

    @Operation(summary = "Marca una notificación enviada como leída")
    @PutMapping("/{id}/leer")
    public ResponseEntity<ApiResponse<NotificacionResponse>> marcarComoLeida(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<NotificacionResponse>builder()
                .success(true)
                .message("Notificación marcada como leída")
                .data(notificacionService.marcarComoLeida(id))
                .build());
    }

    @Operation(summary = "Elimina una notificación")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        notificacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
