package com.foodfast.inventario_servicio.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

class JwtSecurityTest {

    private static final String SECRET = "FoodFastClaveJWTUltraSegura2026ParaMicroserviciosConMasDe42Caracteres";

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void jwtServiceDebeExtraerSubjectYRol() {
        // Given
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET);
        String token = crearToken("damian", "ADMIN");

        // When / Then
        assertThat(jwtService.extraerSubject(token)).isEqualTo("damian");
        assertThat(jwtService.extraerRol(token)).isEqualTo("ADMIN");
    }

    @Test
    void jwtServiceDebeUsarRolUserCuandoNoExisteRol() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET);
        String token = Jwts.builder()
                .subject("usuario")
                .issuedAt(new Date())
                .signWith(clave())
                .compact();

        assertThat(jwtService.extraerRol(token)).isEqualTo("USER");
    }

    @Test
    void filtroNoDebeAplicarseEnSwaggerNiHealth() {
        JwtAuthenticationFilter filtro = new JwtAuthenticationFilter(mock(JwtService.class));
        MockHttpServletRequest swagger = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        swagger.setServletPath("/swagger-ui/index.html");
        MockHttpServletRequest health = new MockHttpServletRequest("GET", "/actuator/health");
        health.setServletPath("/actuator/health");

        assertThat(filtro.shouldNotFilter(swagger)).isTrue();
        assertThat(filtro.shouldNotFilter(health)).isTrue();
    }

    @Test
    void filtroDebeAutenticarConTokenValido() throws Exception {
        // Given
        JwtService jwtService = mock(JwtService.class);
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("damian");
        when(jwtService.extraerClaims("token-valido")).thenReturn(claims);
        when(jwtService.extraerRol("token-valido")).thenReturn("ADMIN");
        JwtAuthenticationFilter filtro = new JwtAuthenticationFilter(jwtService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/inventarios");
        request.addHeader("Authorization", "Bearer token-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        // When
        filtro.doFilterInternal(request, response, chain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("damian");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .contains("ROLE_ADMIN");
        verify(chain).doFilter(request, response);
    }

    @Test
    void filtroDebeContinuarSinAutenticarCuandoTokenEsInvalido() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        when(jwtService.extraerClaims("malo")).thenThrow(new RuntimeException("Token inválido"));
        JwtAuthenticationFilter filtro = new JwtAuthenticationFilter(jwtService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/inventarios");
        request.addHeader("Authorization", "Bearer malo");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filtro.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    private String crearToken(String subject, String rol) {
        return Jwts.builder()
                .subject(subject)
                .claim("rol", rol)
                .issuedAt(new Date())
                .signWith(clave())
                .compact();
    }

    private SecretKey clave() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }
}
