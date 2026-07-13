package com.foodfast.autenticacion_servicio.security;

import com.foodfast.autenticacion_servicio.model.Rol;
import com.foodfast.autenticacion_servicio.model.Usuario;
import com.foodfast.autenticacion_servicio.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FoodFastUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private FoodFastUserDetailsService service;

    @Test
    void deberiaCargarUsuarioActivo() {
        Usuario usuario = Usuario.builder()
                .email("admin@mail.com")
                .password("pass")
                .rol(Rol.ADMIN)
                .activo(true)
                .build();
        when(usuarioRepository.findByEmail("admin@mail.com")).thenReturn(Optional.of(usuario));

        UserDetails details = service.loadUserByUsername("admin@mail.com");

        assertEquals("admin@mail.com", details.getUsername());
        assertEquals("pass", details.getPassword());
        assertTrue(details.isEnabled());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void deberiaMarcarUsuarioInactivoComoDisabled() {
        Usuario usuario = Usuario.builder()
                .email("cliente@mail.com")
                .password("pass")
                .rol(Rol.CLIENTE)
                .activo(false)
                .build();
        when(usuarioRepository.findByEmail("cliente@mail.com")).thenReturn(Optional.of(usuario));

        UserDetails details = service.loadUserByUsername("cliente@mail.com");

        assertFalse(details.isEnabled());
    }

    @Test
    void deberiaLanzarErrorSiNoExiste() {
        when(usuarioRepository.findByEmail("nadie@mail.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("nadie@mail.com"));
    }
}
