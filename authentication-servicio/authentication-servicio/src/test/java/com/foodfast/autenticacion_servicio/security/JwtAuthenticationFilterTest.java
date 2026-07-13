package com.foodfast.autenticacion_servicio.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void noDebeAutenticarSiNoHayBearer() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        FoodFastUserDetailsService userDetailsService = mock(FoodFastUserDetailsService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
        verify(jwtService, never()).extraerEmail("token");
    }

    @Test
    void debeAutenticarConTokenValido() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        FoodFastUserDetailsService userDetailsService = mock(FoodFastUserDetailsService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");
        request.addHeader("Authorization", "Bearer token-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        UserDetails details = User.withUsername("martin@mail.com")
                .password("pass")
                .authorities("ROLE_ADMIN")
                .build();

        when(jwtService.extraerEmail("token-valido")).thenReturn("martin@mail.com");
        when(userDetailsService.loadUserByUsername("martin@mail.com")).thenReturn(details);
        when(jwtService.esTokenValido("token-valido", details)).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    @Test
    void debeLimpiarContextoSiTokenEsInvalido() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        FoodFastUserDetailsService userDetailsService = mock(FoodFastUserDetailsService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");
        request.addHeader("Authorization", "Bearer token-malo");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtService.extraerEmail("token-malo")).thenThrow(new IllegalArgumentException("token malo"));

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }
}
