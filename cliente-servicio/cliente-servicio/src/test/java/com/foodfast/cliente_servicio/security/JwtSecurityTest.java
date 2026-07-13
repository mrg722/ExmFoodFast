package com.foodfast.cliente_servicio.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    void jwtService_debeExtraerSubjectYRol() {
        // Given
        String token = token("damian@foodfast.cl", "ADMIN");

        // When / Then
        assertEquals("damian@foodfast.cl", jwtService.extraerSubject(token));
        assertEquals("ADMIN", jwtService.extraerRol(token));
    }

    @Test
    void jwtService_debeRetornarUserCuandoRolNoExiste() {
        // Given
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("usuario")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(key)
                .compact();

        // When / Then
        assertEquals("USER", jwtService.extraerRol(token));
    }

    @Test
    void jwtFilter_debeAutenticarCuandoTokenEsValido() throws Exception {
        // Given
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/clientes");
        request.addHeader("Authorization", "Bearer " + token("damian", "ADMIN"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());

        // When
        filter.doFilter(request, response, chain);

        // Then
        assertEquals("damian", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    void jwtFilter_debeLimpiarContextoCuandoTokenEsInvalido() throws Exception {
        // Given
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/clientes");
        request.addHeader("Authorization", "Bearer token-invalido");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> assertNull(SecurityContextHolder.getContext().getAuthentication());

        // When
        filter.doFilter(request, response, chain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void jwtFilter_noDebeFiltrarSwagger() throws Exception {
        // Given
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        request.setServletPath("/swagger-ui/index.html");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> assertNull(SecurityContextHolder.getContext().getAuthentication());

        // When
        filter.doFilter(request, response, chain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private String token(String subject, String rol) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(subject)
                .claim("rol", rol)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(key)
                .compact();
    }
}
