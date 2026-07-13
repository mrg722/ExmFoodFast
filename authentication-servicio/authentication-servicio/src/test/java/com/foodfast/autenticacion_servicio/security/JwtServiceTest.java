package com.foodfast.autenticacion_servicio.security;

import com.foodfast.autenticacion_servicio.model.Rol;
import com.foodfast.autenticacion_servicio.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "FoodFastClaveJWTUltraSegura2026ParaMicroserviciosConMasDe42Caracteres");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 86400000L);
    }

    @Test
    void deberiaGenerarTokenYExtraerEmail() {
        Usuario usuario = usuario();

        String token = jwtService.generarToken(usuario);

        assertNotNull(token);
        assertEquals("martin@mail.com", jwtService.extraerEmail(token));
    }

    @Test
    void deberiaValidarTokenCorrectamente() {
        Usuario usuario = usuario();
        String token = jwtService.generarToken(usuario);
        UserDetails userDetails = User.withUsername("martin@mail.com")
                .password("password")
                .authorities("ROLE_ADMIN")
                .build();

        assertTrue(jwtService.esTokenValido(token, userDetails));
    }

    @Test
    void deberiaRechazarTokenConUsuarioDistinto() {
        String token = jwtService.generarToken(usuario());
        UserDetails userDetails = User.withUsername("otro@mail.com")
                .password("password")
                .authorities("ROLE_ADMIN")
                .build();

        assertFalse(jwtService.esTokenValido(token, userDetails));
    }

    @Test
    void deberiaLanzarErrorConTokenInvalido() {
        assertThrows(Exception.class, () -> jwtService.extraerEmail("token-invalido"));
    }

    private Usuario usuario() {
        return Usuario.builder()
                .id(1L)
                .nombre("Martin")
                .email("martin@mail.com")
                .password("password")
                .rol(Rol.ADMIN)
                .activo(true)
                .build();
    }
}
