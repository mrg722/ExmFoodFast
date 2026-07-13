package com.foodfast.autenticacion_servicio.repository;

import com.foodfast.autenticacion_servicio.model.Rol;
import com.foodfast.autenticacion_servicio.model.Usuario;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("test")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository repository;

    @Test
    void deberiaGuardarUsuario() {
        Usuario guardado = repository.save(usuario("martin@mail.com", Rol.ADMIN));

        assertNotNull(guardado.getId());
        assertEquals("martin@mail.com", guardado.getEmail());
        assertTrue(guardado.getActivo());
        assertNotNull(guardado.getFechaCreacion());
    }

    @Test
    void deberiaBuscarUsuarioPorEmail() {
        repository.save(usuario("martin@mail.com", Rol.CLIENTE));

        Optional<Usuario> resultado = repository.findByEmail("martin@mail.com");

        assertTrue(resultado.isPresent());
        assertEquals("Martin", resultado.get().getNombre());
    }

    @Test
    void deberiaRetornarVacioSiEmailNoExiste() {
        Optional<Usuario> resultado = repository.findByEmail("nadie@mail.com");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deberiaValidarExistenciaPorEmail() {
        repository.save(usuario("martin@mail.com", Rol.REPARTIDOR));

        assertTrue(repository.existsByEmail("martin@mail.com"));
        assertFalse(repository.existsByEmail("otro@mail.com"));
    }

    @Test
    void deberiaListarYEliminarUsuarios() {
        Usuario guardado = repository.save(usuario("martin@mail.com", Rol.RESTAURANTE));

        List<Usuario> usuarios = repository.findAll();
        repository.deleteById(guardado.getId());

        assertEquals(1, usuarios.size());
        assertFalse(repository.findById(guardado.getId()).isPresent());
    }

    private Usuario usuario(String email, Rol rol) {
        return Usuario.builder()
                .nombre("Martin")
                .email(email)
                .password("123456")
                .rol(rol)
                .activo(true)
                .build();
    }
}
