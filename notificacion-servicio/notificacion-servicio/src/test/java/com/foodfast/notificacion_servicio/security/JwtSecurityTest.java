package com.foodfast.notificacion_servicio.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.ServletException;
import java.io.IOException;
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
    void debeExtraerSubjectYRolDesdeToken() {
        String token = generarToken("damian", "ADMIN");

        assertThat(jwtService.extraerSubject(token)).isEqualTo("damian");
        assertThat(jwtService.extraerRol(token)).isEqualTo("ADMIN");
    }

    @Test
    void debeUsarRolUserCuandoNoExisteRol() {
        String token = Jwts.builder()
                .subject("damian")
                .signWith(signingKey())
                .compact();

        assertThat(jwtService.extraerRol(token)).isEqualTo("USER");
    }

    @Test
    void filtroDebeSetearAutenticacionConTokenValido() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/notificaciones");
        request.addHeader("Authorization", "Bearer " + generarToken("damian", "ADMIN"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("damian");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting(Object::toString)
                .contains("ROLE_ADMIN");
    }

    @Test
    void filtroDebeLimpiarContextoConTokenInvalidoYNoFiltrarSwagger() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockHttpServletRequest swagger = new MockHttpServletRequest();
        swagger.setServletPath("/swagger-ui/index.html");
        assertThat(filter.shouldNotFilter(swagger)).isTrue();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/notificaciones");
        request.addHeader("Authorization", "Bearer token-malo");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private String generarToken(String subject, String rol) {
        return Jwts.builder()
                .subject(subject)
                .claim("rol", rol)
                .signWith(signingKey())
                .compact();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }
}
