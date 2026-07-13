package com.foodfast.autenticacion_servicio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodfast.autenticacion_servicio.dto.AuthResponse;
import com.foodfast.autenticacion_servicio.dto.LoginRequest;
import com.foodfast.autenticacion_servicio.dto.RegistroRequest;
import com.foodfast.autenticacion_servicio.dto.UsuarioResponse;
import com.foodfast.autenticacion_servicio.model.Rol;
import com.foodfast.autenticacion_servicio.service.AuthService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService))
                .build();
    }

    @Test
    void deberiaHacerLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("martin@mail.com");
        request.setPassword("123456");

        AuthResponse response = AuthResponse.builder()
                .accessToken("token-prueba")
                .tokenType("Bearer")
                .usuario(usuarioResponse())
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login correcto"))
                .andExpect(jsonPath("$.data.accessToken").value("token-prueba"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.usuario.email").value("martin@mail.com"));
    }

    @Test
    void deberiaRegistrarUsuarioEnRegister() throws Exception {
        RegistroRequest request = registroRequest();

        AuthResponse response = AuthResponse.builder()
                .accessToken("token-prueba")
                .tokenType("Bearer")
                .usuario(usuarioResponse())
                .build();

        when(authService.register(any(RegistroRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuario registrado"))
                .andExpect(jsonPath("$.data.accessToken").value("token-prueba"));
    }

    @Test
    void deberiaRegistrarUsuarioEnRegistroAlias() throws Exception {
        RegistroRequest request = registroRequest();

        when(authService.register(any(RegistroRequest.class))).thenReturn(AuthResponse.builder()
                .accessToken("token-alias")
                .tokenType("Bearer")
                .usuario(usuarioResponse())
                .build());

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.accessToken").value("token-alias"));
    }

    @Test
    void deberiaObtenerPerfilConLinksHateoas() throws Exception {
        when(authService.obtenerPerfil("martin@mail.com")).thenReturn(usuarioResponse());

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("martin@mail.com");

        mockMvc.perform(get("/api/auth/me")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Perfil obtenido"))
                .andExpect(jsonPath("$.data.nombre").value("Martin"))
                .andExpect(jsonPath("$.data.email").value("martin@mail.com"));
    }

    @Test
    void deberiaObtenerPerfilAlias() throws Exception {
        when(authService.obtenerPerfil("martin@mail.com")).thenReturn(usuarioResponse());

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("martin@mail.com");

        mockMvc.perform(get("/api/auth/perfil")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("martin@mail.com"));
    }

    @Test
    void deberiaListarUsuarios() throws Exception {
        when(authService.listarUsuarios()).thenReturn(List.of(usuarioResponse()));

        mockMvc.perform(get("/api/auth/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuarios obtenidos"))
                .andExpect(jsonPath("$.data[0].email").value("martin@mail.com"));
    }

    @Test
    void deberiaBuscarUsuarioPorId() throws Exception {
        when(authService.buscarUsuarioPorId(1L)).thenReturn(usuarioResponse());

        mockMvc.perform(get("/api/auth/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usuario obtenido"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void deberiaCambiarEstadoUsuario() throws Exception {
        UsuarioResponse response = usuarioResponse();
        response.setActivo(false);
        when(authService.cambiarEstado(1L, false)).thenReturn(response);

        mockMvc.perform(put("/api/auth/usuarios/1/estado")
                        .param("activo", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Estado de usuario actualizado"))
                .andExpect(jsonPath("$.data.activo").value(false));
    }

    private RegistroRequest registroRequest() {
        RegistroRequest request = new RegistroRequest();
        request.setNombre("Martin");
        request.setEmail("martin@mail.com");
        request.setPassword("123456");
        request.setRol(Rol.CLIENTE);
        return request;
    }

    private UsuarioResponse usuarioResponse() {
        return UsuarioResponse.builder()
                .id(1L)
                .nombre("Martin")
                .email("martin@mail.com")
                .rol(Rol.CLIENTE)
                .activo(true)
                .build();
    }
}
