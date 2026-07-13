package com.foodfast.cliente_servicio.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.foodfast.cliente_servicio.model.Cliente;
import com.foodfast.cliente_servicio.model.Direccion;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository clienteRepository;

    @Test
    void save_debeGuardarClienteConDireccion() {
        // Given
        Cliente cliente = clienteValido("ana@foodfast.cl", true);
        cliente.agregarDireccion(direccion(true));

        // When
        Cliente guardado = clienteRepository.saveAndFlush(cliente);

        // Then
        assertNotNull(guardado.getId());
        assertEquals("ana@foodfast.cl", guardado.getEmail());
        assertEquals(1, guardado.getDirecciones().size());
        assertEquals(guardado, guardado.getDirecciones().get(0).getCliente());
    }

    @Test
    void existsByEmail_debeRetornarTrueCuandoEmailExiste() {
        // Given
        clienteRepository.save(clienteValido("carlos@foodfast.cl", true));

        // When
        boolean existe = clienteRepository.existsByEmail("carlos@foodfast.cl");

        // Then
        assertTrue(existe);
    }

    @Test
    void findByEmail_debeEncontrarClienteConDirecciones() {
        // Given
        Cliente cliente = clienteValido("maria@foodfast.cl", true);
        cliente.agregarDireccion(direccion(true));
        clienteRepository.saveAndFlush(cliente);

        // When
        var encontrado = clienteRepository.findByEmail("maria@foodfast.cl");

        // Then
        assertTrue(encontrado.isPresent());
        assertEquals("Maria", encontrado.get().getNombre());
        assertEquals(1, encontrado.get().getDirecciones().size());
    }

    @Test
    void findByActivoTrue_debeFiltrarClientesActivos() {
        // Given
        clienteRepository.save(clienteValido("activo@foodfast.cl", true));
        clienteRepository.save(clienteValido("inactivo@foodfast.cl", false));

        // When
        var activos = clienteRepository.findByActivoTrue();

        // Then
        assertEquals(1, activos.size());
        assertEquals("activo@foodfast.cl", activos.get(0).getEmail());
    }

    @Test
    void delete_debeEliminarCliente() {
        // Given
        Cliente guardado = clienteRepository.saveAndFlush(clienteValido("borrar@foodfast.cl", true));

        // When
        clienteRepository.delete(guardado);
        clienteRepository.flush();

        // Then
        assertFalse(clienteRepository.findById(guardado.getId()).isPresent());
    }

    private Cliente clienteValido(String email, boolean activo) {
        return Cliente.builder()
                .nombre(email.startsWith("maria") ? "Maria" : "Cliente")
                .apellido("FoodFast")
                .email(email)
                .telefono("+56911112222")
                .activo(activo)
                .direcciones(new ArrayList<>())
                .build();
    }

    private Direccion direccion(boolean principal) {
        return Direccion.builder()
                .calle("Av. Providencia")
                .numero("1234")
                .comuna("Providencia")
                .ciudad("Santiago")
                .referencia("Depto 501")
                .principal(principal)
                .build();
    }
}
