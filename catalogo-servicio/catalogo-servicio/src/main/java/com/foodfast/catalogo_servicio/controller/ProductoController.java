package com.foodfast.catalogo_servicio.controller;

import com.foodfast.catalogo_servicio.dto.ApiResponse;
import com.foodfast.catalogo_servicio.dto.ProductoRequest;
import com.foodfast.catalogo_servicio.dto.ProductoResponse;
import com.foodfast.catalogo_servicio.dto.ProductoStockResponse;
import com.foodfast.catalogo_servicio.service.ProductoService;
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

@Tag(name = "Productos", description = "Gestión de productos del catálogo FoodFast")
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @Operation(summary = "Estado del módulo de productos", description = "Endpoint rápido para validar que el controlador responde.")
    @GetMapping("/estado")
    public ResponseEntity<ApiResponse<String>> estado() {
        return ResponseEntity.ok(new ApiResponse<>(true, "catalogo-servicio operativo", "OK"));
    }

    @Operation(summary = "Listar productos", description = "Retorna todos los productos del catálogo.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> listar() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Productos listados correctamente", productoService.listar()));
    }

    @Operation(summary = "Listar productos disponibles", description = "Retorna solo productos marcados como disponibles.")
    @GetMapping("/disponibles")
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> listarDisponibles() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Productos disponibles listados correctamente", productoService.listarDisponibles()));
    }

    @Operation(summary = "Listar productos por categoría", description = "Retorna productos asociados a una categoría.")
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> listarPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Productos por categoría listados correctamente", productoService.listarPorCategoria(categoriaId)));
    }

    @Operation(summary = "Listar productos disponibles por categoría", description = "Retorna productos disponibles asociados a una categoría.")
    @GetMapping("/categoria/{categoriaId}/disponibles")
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> listarDisponiblesPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Productos disponibles por categoría listados correctamente", productoService.listarDisponiblesPorCategoria(categoriaId)));
    }

    @Operation(summary = "Buscar producto por ID", description = "Busca un producto por su identificador.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoResponse>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Producto encontrado", productoService.buscarPorId(id)));
    }

    @Operation(summary = "Consultar producto con stock", description = "Busca el producto y opcionalmente consulta su stock en inventario-servicio mediante WebClient.")
    @GetMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<ProductoStockResponse>> buscarProductoConStock(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Producto con información de stock", productoService.buscarProductoConStock(id)));
    }

    @Operation(summary = "Crear producto", description = "Crea un producto asociado a una categoría existente y activa.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Producto creado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ProductoResponse>> crear(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del producto",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ProductoRequest.class),
                            examples = @ExampleObject(value = "{\"nombre\":\"Hamburguesa Clásica\",\"descripcion\":\"Hamburguesa con queso\",\"precio\":4990,\"disponible\":true,\"categoriaId\":1}"))
            )
            @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Producto creado correctamente", productoService.crear(request)));
    }

    @Operation(summary = "Actualizar producto", description = "Actualiza datos generales y categoría del producto.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoResponse>> actualizar(@PathVariable Long id,
                                                                     @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Producto actualizado correctamente", productoService.actualizar(id, request)));
    }

    @Operation(summary = "Cambiar disponibilidad", description = "Marca un producto como disponible o no disponible.")
    @PatchMapping("/{id}/disponibilidad")
    public ResponseEntity<ApiResponse<ProductoResponse>> cambiarDisponibilidad(@PathVariable Long id,
                                                                                @RequestParam boolean disponible) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Disponibilidad de producto actualizada", productoService.cambiarDisponibilidad(id, disponible)));
    }

    @Operation(summary = "Eliminar producto", description = "Elimina un producto por ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
