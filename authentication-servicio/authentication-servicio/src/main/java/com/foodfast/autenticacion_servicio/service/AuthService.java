package com.foodfast.autenticacion_servicio.service;

import com.foodfast.autenticacion_servicio.dto.AuthResponse;
import com.foodfast.autenticacion_servicio.dto.LoginRequest;
import com.foodfast.autenticacion_servicio.dto.RegistroRequest;
import com.foodfast.autenticacion_servicio.dto.UsuarioResponse;
import com.foodfast.autenticacion_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.autenticacion_servicio.exception.ReglaNegocioException;
import com.foodfast.autenticacion_servicio.model.Rol;
import com.foodfast.autenticacion_servicio.model.Usuario;
import com.foodfast.autenticacion_servicio.repository.UsuarioRepository;
import com.foodfast.autenticacion_servicio.security.JwtService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Nombre usado por la guía: AuthController llama a authService.register(request).
     */
    @Transactional
    public AuthResponse register(RegistroRequest request) {
        return registrar(request);
    }

    /**
     * Alias en español para mantener compatibilidad con la versión previa del proyecto.
     */
    @Transactional
    public AuthResponse registrar(RegistroRequest request) {
        String emailNormalizado = normalizarEmail(request.getEmail());
        log.info("Registrando usuario email={} rol={}", emailNormalizado, request.getRol());

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new ReglaNegocioException("Ya existe un usuario registrado con el email: " + emailNormalizado);
        }

        Rol rol = request.getRol() != null ? request.getRol() : Rol.CLIENTE;

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre().trim())
                .email(emailNormalizado)
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(rol)
                .activo(true)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        String token = jwtService.generarToken(guardado);

        log.info("Usuario registrado correctamente id={} email={}", guardado.getId(), guardado.getEmail());
        return construirAuthResponse(guardado, token);
    }

    public AuthResponse login(LoginRequest request) {
        String emailNormalizado = normalizarEmail(request.getEmail());
        log.info("Intentando login email={}", emailNormalizado);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(emailNormalizado, request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByEmail(emailNormalizado)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new ReglaNegocioException("El usuario se encuentra desactivado");
        }

        String token = jwtService.generarToken(usuario);
        log.info("Login correcto email={} rol={}", usuario.getEmail(), usuario.getRol());
        return construirAuthResponse(usuario, token);
    }

    public UsuarioResponse obtenerPerfil(String email) {
        Usuario usuario = buscarPorEmail(normalizarEmail(email));
        return toResponse(usuario);
    }

    public List<UsuarioResponse> listarUsuarios() {
        log.info("Listando usuarios registrados");
        return usuarioRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UsuarioResponse buscarUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe usuario con id: " + id));
        return toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse cambiarEstado(Long id, boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe usuario con id: " + id));

        usuario.setActivo(activo);
        Usuario actualizado = usuarioRepository.save(usuario);
        log.info("Usuario id={} actualizado activo={}", id, activo);
        return toResponse(actualizado);
    }

    private Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe usuario con email: " + email));
    }

    private AuthResponse construirAuthResponse(Usuario usuario, String token) {
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .usuario(toResponse(usuario))
                .build();
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .activo(usuario.getActivo())
                .fechaCreacion(usuario.getFechaCreacion())
                .build();
    }

    private String normalizarEmail(String email) {
        return email.toLowerCase().trim();
    }
}
