package com.foodfast.restaurante_servicio.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JwtSecurityTest {

    private static final String SECRET = "FoodFastClaveJWTUltraSegura2026ParaMicroserviciosConMasDe42Caracteres";

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void debeExtraerSubjectYRol() {
        JwtService jwtService = jwtService();
        String token = token("damian", "ADMIN");

        assertThat(jwtService.extraerSubject(token)).isEqualTo("damian");
        assertThat(jwtService.extraerRol(token)).isEqualTo("ADMIN");
    }

    @Test
    void debeUsarRolUserPorDefecto() {
        JwtService jwtService = jwtService();
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder().subject("usuario").signWith(key).compact();

        assertThat(jwtService.extraerRol(token)).isEqualTo("USER");
    }

    @Test
    void filtroDebeAutenticarConTokenValido() throws Exception {
        JwtService jwtService = jwtService();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/restaurantes");
        request.addHeader("Authorization", "Bearer " + token("damian", "ADMIN"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        jakarta.servlet.FilterChain chain = mock(jakarta.servlet.FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("damian");
        verify(chain).doFilter(request, response);
    }

    @Test
    void filtroDebeContinuarSinAutenticacionConTokenInvalido() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/restaurantes");
        request.addHeader("Authorization", "Bearer token-invalido");
        MockHttpServletResponse response = new MockHttpServletResponse();
        jakarta.servlet.FilterChain chain = mock(jakarta.servlet.FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void filtroNoDebeFiltrarSwagger() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        MockHttpServletResponse response = new MockHttpServletResponse();
        jakarta.servlet.FilterChain chain = mock(jakarta.servlet.FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    private JwtService jwtService() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET);
        return jwtService;
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
