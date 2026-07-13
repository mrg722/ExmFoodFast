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
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void deberiaRegistrarUsuarioCorrectamente() {
        RegistroRequest request = registroRequest(Rol.ADMIN);

        when(usuarioRepository.existsByEmail("martin@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("password-encriptada");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(1L);
            return usuario;
        });
        when(jwtService.generarToken(any(Usuario.class))).thenReturn("token-prueba");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("token-prueba", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("martin@mail.com", response.getUsuario().getEmail());
        assertEquals(Rol.ADMIN, response.getUsuario().getRol());
        verify(usuarioRepository).existsByEmail("martin@mail.com");
        verify(passwordEncoder).encode("123456");
        verify(usuarioRepository).save(any(Usuario.class));
        verify(jwtService).generarToken(any(Usuario.class));
    }

    @Test
    void deberiaRegistrarUsuarioConRolClientePorDefecto() {
        RegistroRequest request = registroRequest(null);

        when(usuarioRepository.existsByEmail("martin@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("password-encriptada");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(2L);
            return usuario;
        });
        when(jwtService.generarToken(any(Usuario.class))).thenReturn("token-cliente");

        AuthResponse response = authService.registrar(request);

        assertEquals("token-cliente", response.getAccessToken());
        assertEquals(Rol.CLIENTE, response.getUsuario().getRol());
    }

    @Test
    void deberiaLanzarErrorSiEmailYaExiste() {
        RegistroRequest request = registroRequest(Rol.CLIENTE);

        when(usuarioRepository.existsByEmail("martin@mail.com")).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> authService.register(request));
        verify(usuarioRepository).existsByEmail("martin@mail.com");
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(jwtService, never()).generarToken(any(Usuario.class));
    }

    @Test
    void deberiaHacerLoginCorrectamente() {
        LoginRequest request = loginRequest("MARTIN@MAIL.COM", "123456");
        Usuario usuario = usuarioActivo();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("martin@mail.com", null));
        when(usuarioRepository.findByEmail("martin@mail.com")).thenReturn(Optional.of(usuario));
        when(jwtService.generarToken(usuario)).thenReturn("token-prueba");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("token-prueba", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usuarioRepository).findByEmail("martin@mail.com");
        verify(jwtService).generarToken(usuario);
    }

    @Test
    void deberiaLanzarErrorSiCredencialesSonInvalidas() {
        LoginRequest request = loginRequest("martin@mail.com", "incorrecta");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(usuarioRepository, never()).findByEmail("martin@mail.com");
        verify(jwtService, never()).generarToken(any(Usuario.class));
    }

    @Test
    void deberiaLanzarErrorSiUsuarioNoExisteDespuesDeAutenticar() {
        LoginRequest request = loginRequest("martin@mail.com", "123456");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("martin@mail.com", null));
        when(usuarioRepository.findByEmail("martin@mail.com")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> authService.login(request));
    }

    @Test
    void deberiaLanzarErrorSiUsuarioEstaDesactivado() {
        LoginRequest request = loginRequest("martin@mail.com", "123456");
        Usuario usuario = usuarioActivo();
        usuario.setActivo(false);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("martin@mail.com", null));
        when(usuarioRepository.findByEmail("martin@mail.com")).thenReturn(Optional.of(usuario));

        assertThrows(ReglaNegocioException.class, () -> authService.login(request));
        verify(jwtService, never()).generarToken(any(Usuario.class));
    }

    @Test
    void deberiaObtenerPerfil() {
        when(usuarioRepository.findByEmail("martin@mail.com")).thenReturn(Optional.of(usuarioActivo()));

        UsuarioResponse response = authService.obtenerPerfil("MARTIN@MAIL.COM");

        assertEquals("martin@mail.com", response.getEmail());
        assertEquals("Martin", response.getNombre());
    }

    @Test
    void deberiaLanzarErrorSiPerfilNoExiste() {
        when(usuarioRepository.findByEmail("nadie@mail.com")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> authService.obtenerPerfil("nadie@mail.com"));
    }

    @Test
    void deberiaListarUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuarioActivo()));

        List<UsuarioResponse> usuarios = authService.listarUsuarios();

        assertEquals(1, usuarios.size());
        assertEquals("martin@mail.com", usuarios.get(0).getEmail());
    }

    @Test
    void deberiaBuscarUsuarioPorId() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioActivo()));

        UsuarioResponse response = authService.buscarUsuarioPorId(1L);

        assertEquals(1L, response.getId());
    }

    @Test
    void deberiaLanzarErrorSiUsuarioPorIdNoExiste() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> authService.buscarUsuarioPorId(99L));
    }

    @Test
    void deberiaCambiarEstado() {
        Usuario usuario = usuarioActivo();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        UsuarioResponse response = authService.cambiarEstado(1L, false);

        assertFalse(response.getActivo());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deberiaLanzarErrorAlCambiarEstadoSiNoExiste() {
        when(usuarioRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> authService.cambiarEstado(100L, true));
    }

    private RegistroRequest registroRequest(Rol rol) {
        RegistroRequest request = new RegistroRequest();
        request.setNombre(" Martin ");
        request.setEmail("MARTIN@MAIL.COM");
        request.setPassword("123456");
        request.setRol(rol);
        return request;
    }

    private LoginRequest loginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    private Usuario usuarioActivo() {
        return Usuario.builder()
                .id(1L)
                .nombre("Martin")
                .email("martin@mail.com")
                .password("password-encriptada")
                .rol(Rol.ADMIN)
                .activo(true)
                .build();
    }
}
