package com.foodfast.pedido_servicio.security;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

class JwtSecurityTest {

    private static final String SECRET = "FoodFastClaveJWTUltraSegura2026ParaMicroserviciosConMasDe42Caracteres";
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void jwtServiceDebeExtraerSubjectYRol() {
        String token = generarToken("damian", "ADMIN");

        assertThat(jwtService.extraerSubject(token)).isEqualTo("damian");
        assertThat(jwtService.extraerRol(token)).isEqualTo("ADMIN");
    }

    @Test
    void jwtFilterConTokenValidoDebeAutenticarUsuario() throws Exception {
        String token = generarToken("damian", "ADMIN");
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/pedidos");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("damian");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .contains("ROLE_ADMIN");
    }

    @Test
    void jwtFilterConTokenInvalidoDebeLimpiarContexto() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/pedidos");
        request.addHeader("Authorization", "Bearer token-malo");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void jwtServiceSinRolDebeRetornarUserPorDefecto() {
        String token = generarTokenSinRol("damian");

        assertThat(jwtService.extraerRol(token)).isEqualTo("USER");
    }

    private String generarToken(String subject, String rol) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(subject)
                .claim("rol", rol)
                .signWith(key)
                .compact();
    }

    private String generarTokenSinRol(String subject) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(subject)
                .signWith(key)
                .compact();
    }
}
