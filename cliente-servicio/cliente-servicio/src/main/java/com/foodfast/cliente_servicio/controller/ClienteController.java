package com.foodfast.cliente_servicio.controller;

import com.foodfast.cliente_servicio.dto.ApiResponse;
import com.foodfast.cliente_servicio.dto.ClienteRequest;
import com.foodfast.cliente_servicio.dto.ClienteResponse;
import com.foodfast.cliente_servicio.dto.PedidoResumenResponse;
import com.foodfast.cliente_servicio.service.ClienteService;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Clientes", description = "Gestión de clientes, direcciones y consulta de pedidos por cliente")
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @Operation(summary = "Estado del microservicio", description = "Endpoint simple para validar que cliente-servicio está operativo.")
    @GetMapping("/estado")
    public ResponseEntity<ApiResponse<String>> estado() {
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("cliente-servicio operativo")
                .data("OK")
                .build());
    }

    @Operation(summary = "Listar clientes", description = "Retorna todos los clientes registrados con sus direcciones.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClienteResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.<List<ClienteResponse>>builder()
                .success(true)
                .message("Clientes listados correctamente")
                .data(clienteService.listar())
                .build());
    }

    @Operation(summary = "Listar clientes activos", description = "Retorna solo clientes activos para operaciones del negocio.")
    @GetMapping("/activos")
    public ResponseEntity<ApiResponse<List<ClienteResponse>>> listarActivos() {
        return ResponseEntity.ok(ApiResponse.<List<ClienteResponse>>builder()
                .success(true)
                .message("Clientes activos listados correctamente")
                .data(clienteService.listarActivos())
                .build());
    }

    @Operation(summary = "Buscar cliente por ID", description = "Busca un cliente por identificador único.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteResponse>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<ClienteResponse>builder()
                .success(true)
                .message("Cliente encontrado")
                .data(clienteService.buscarPorId(id))
                .build());
    }

    @Operation(summary = "Buscar cliente por email", description = "Busca un cliente por correo electrónico.")
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<ClienteResponse>> buscarPorEmail(@PathVariable String email) {
        return ResponseEntity.ok(ApiResponse.<ClienteResponse>builder()
                .success(true)
                .message("Cliente encontrado por email")
                .data(clienteService.buscarPorEmail(email))
                .build());
    }

    @Operation(summary = "Listar pedidos de un cliente", description = "Valida que el cliente exista y consulta pedido-servicio mediante WebClient cuando la integración está activada.")
    @GetMapping("/{id}/pedidos")
    public ResponseEntity<ApiResponse<List<PedidoResumenResponse>>> listarPedidosDelCliente(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return ResponseEntity.ok(ApiResponse.<List<PedidoResumenResponse>>builder()
                .success(true)
                .message("Pedidos del cliente obtenidos correctamente")
                .data(clienteService.listarPedidosDelCliente(id, authorizationHeader))
                .build());
    }

    @Operation(summary = "Crear cliente", description = "Crea un cliente nuevo. Si envía direcciones, exactamente una debe ser principal.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Cliente creado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content(examples = @ExampleObject(value = "{\"message\":\"El cliente debe tener exactamente una direccion principal\"}")))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ClienteResponse>> crear(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse creado = clienteService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<ClienteResponse>builder()
                .success(true)
                .message("Cliente creado correctamente")
                .data(creado)
                .build());
    }

    @Operation(summary = "Actualizar cliente", description = "Actualiza datos y direcciones de un cliente existente.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteResponse>> actualizar(@PathVariable Long id,
                                                                   @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(ApiResponse.<ClienteResponse>builder()
                .success(true)
                .message("Cliente actualizado correctamente")
                .data(clienteService.actualizar(id, request))
                .build());
    }

    @Operation(summary = "Activar cliente", description = "Marca un cliente como activo.")
    @PutMapping("/{id}/activar")
    public ResponseEntity<ApiResponse<ClienteResponse>> activar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<ClienteResponse>builder()
                .success(true)
                .message("Cliente activado correctamente")
                .data(clienteService.activar(id))
                .build());
    }

    @Operation(summary = "Desactivar cliente", description = "Marca un cliente como inactivo sin eliminar sus datos.")
    @PutMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponse<ClienteResponse>> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<ClienteResponse>builder()
                .success(true)
                .message("Cliente desactivado correctamente")
                .data(clienteService.desactivar(id))
                .build());
    }

    @Operation(summary = "Eliminar cliente", description = "Elimina un cliente y sus direcciones asociadas.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
