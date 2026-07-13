package com.foodfast.resena_servicio.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtSecurityTest {

    private static final String SECRET = "FoodFastClaveJWTUltraSegura2026ParaMicroserviciosConMasDe42Caracteres";
    private JwtService jwtService;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();
        Field field = JwtService.class.getDeclaredField("jwtSecret");
        field.setAccessible(true);
        field.set(jwtService, SECRET);
        filter = new JwtAuthenticationFilter(jwtService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void debeExtraerSubjectYRol() {
        String token = crearToken("damian", "ADMIN");

        assertThat(jwtService.extraerSubject(token)).isEqualTo("damian");
        assertThat(jwtService.extraerRol(token)).isEqualTo("ADMIN");
    }

    @Test
    void debeRetornarRolUserSiNoTieneClaimRol() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("usuario")
                .issuedAt(new Date())
                .signWith(key)
                .compact();

        assertThat(jwtService.extraerRol(token)).isEqualTo("USER");
    }

    @Test
    void filtroDebeAutenticarConTokenValidoYContinuarCadena() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + crearToken("damian", "USER"));
        when(request.getServletPath()).thenReturn("/api/resenas");

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("damian");
        verify(chain).doFilter(request, response);
    }

    @Test
    void filtroDebeIgnorarTokenInvalidoYRutasPublicas() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer token-invalido");
        when(request.getServletPath()).thenReturn("/api/resenas");

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);

        HttpServletRequest publicRequest = mock(HttpServletRequest.class);
        when(publicRequest.getServletPath()).thenReturn("/swagger-ui/index.html");
        assertThat(filter.shouldNotFilter(publicRequest)).isTrue();
    }

    private String crearToken(String subject, String rol) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(subject)
                .claim("rol", rol)
                .issuedAt(new Date())
                .signWith(key)
                .compact();
    }
}
