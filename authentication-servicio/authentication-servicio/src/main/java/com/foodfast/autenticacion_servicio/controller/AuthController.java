package com.foodfast.autenticacion_servicio.controller;

import com.foodfast.autenticacion_servicio.dto.ApiResponse;
import com.foodfast.autenticacion_servicio.dto.AuthResponse;
import com.foodfast.autenticacion_servicio.dto.LoginRequest;
import com.foodfast.autenticacion_servicio.dto.RegistroRequest;
import com.foodfast.autenticacion_servicio.dto.UsuarioResponse;
import com.foodfast.autenticacion_servicio.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Tag(name = "Autenticación", description = "Registro, login y emisión de tokens JWT")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Registrar usuario", description = "Crea un usuario nuevo en FoodFast y devuelve token JWT")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuario registrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos o email repetido")
    })
    @PostMapping({"/register", "/registro"})
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(201).body(
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message("Usuario registrado")
                        .data(authService.register(request))
                        .build()
        );
    }

    @Operation(summary = "Iniciar sesión", description = "Valida credenciales y devuelve accessToken")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login correcto"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message("Login correcto")
                        .data(authService.login(request))
                        .build()
        );
    }

    @Operation(summary = "Obtener perfil autenticado", description = "Devuelve el usuario autenticado con enlaces HATEOAS")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Perfil obtenido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
    @GetMapping({"/me", "/perfil"})
    public ResponseEntity<ApiResponse<EntityModel<UsuarioResponse>>> me(Authentication authentication) {
        String email = authentication.getName();
        UsuarioResponse usuario = authService.obtenerPerfil(email);

        EntityModel<UsuarioResponse> recurso = EntityModel.of(usuario);
        recurso.add(linkTo(AuthController.class).slash("me").withSelfRel());
        recurso.add(linkTo(AuthController.class).slash("login").withRel("login"));
        recurso.add(linkTo(AuthController.class).slash("register").withRel("register"));

        return ResponseEntity.ok(
                ApiResponse.<EntityModel<UsuarioResponse>>builder()
                        .success(true)
                        .message("Perfil obtenido")
                        .data(recurso)
                        .build()
        );
    }

    @Operation(summary = "Listar usuarios", description = "Lista usuarios registrados. Requiere rol ADMIN")
    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> listarUsuarios() {
        return ResponseEntity.ok(
                ApiResponse.<List<UsuarioResponse>>builder()
                        .success(true)
                        .message("Usuarios obtenidos")
                        .data(authService.listarUsuarios())
                        .build()
        );
    }

    @Operation(summary = "Buscar usuario por ID", description = "Busca un usuario por ID. Requiere rol ADMIN")
    @GetMapping("/usuarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> buscarUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.<UsuarioResponse>builder()
                        .success(true)
                        .message("Usuario obtenido")
                        .data(authService.buscarUsuarioPorId(id))
                        .build()
        );
    }

    @Operation(summary = "Cambiar estado de usuario", description = "Activa o desactiva un usuario. Requiere rol ADMIN")
    @PutMapping("/usuarios/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo) {
        return ResponseEntity.ok(
                ApiResponse.<UsuarioResponse>builder()
                        .success(true)
                        .message("Estado de usuario actualizado")
                        .data(authService.cambiarEstado(id, activo))
                        .build()
        );
    }
}
