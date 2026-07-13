package com.foodfast.reparto_servicio.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
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
        String token = token("damian", "ADMIN");

        // When / Then
        assertThat(jwtService.extraerSubject(token)).isEqualTo("damian");
        assertThat(jwtService.extraerRol(token)).isEqualTo("ADMIN");
    }

    @Test
    void jwtServiceDebeRetornarUserSiRolNoExiste() {
        // Given
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET);
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder().subject("damian").signWith(key).compact();

        // When / Then
        assertThat(jwtService.extraerRol(token)).isEqualTo("USER");
    }

    @Test
    void filtroDebeAutenticarSiAuthorizationTieneBearerValido() throws Exception {
        // Given
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getServletPath()).thenReturn("/api/repartidores");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token("damian", "ADMIN"));

        // When
        filter.doFilter(request, response, chain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("damian");
        verify(chain).doFilter(request, response);
    }

    @Test
    void filtroDebeIgnorarTokenInvalidoYLlamarCadena() throws Exception {
        // Given
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getServletPath()).thenReturn("/api/repartidores");
        when(request.getHeader("Authorization")).thenReturn("Bearer token_invalido");

        // When
        filter.doFilter(request, response, chain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldNotFilterDebeSerTrueParaSwaggerYHealth() {
        JwtService jwtService = new JwtService();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);

        assertThat(invokeShouldNotFilter(filter, "/swagger-ui/index.html")).isTrue();
        assertThat(invokeShouldNotFilter(filter, "/v3/api-docs")).isTrue();
        assertThat(invokeShouldNotFilter(filter, "/actuator/health")).isTrue();
        assertThat(invokeShouldNotFilter(filter, "/api/entregas")).isFalse();
    }

    private boolean invokeShouldNotFilter(JwtAuthenticationFilter filter, String path) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn(path);
        return (boolean) ReflectionTestUtils.invokeMethod(filter, "shouldNotFilter", request);
    }

    private String token(String subject, String rol) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(subject)
                .claim("rol", rol)
                .signWith(key)
                .compact();
    }
}
