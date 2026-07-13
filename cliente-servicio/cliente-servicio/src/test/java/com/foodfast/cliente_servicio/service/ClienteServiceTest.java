package com.foodfast.cliente_servicio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.foodfast.cliente_servicio.client.PedidoClient;
import com.foodfast.cliente_servicio.dto.ClienteRequest;
import com.foodfast.cliente_servicio.dto.ClienteResponse;
import com.foodfast.cliente_servicio.dto.DireccionRequest;
import com.foodfast.cliente_servicio.dto.PedidoResumenResponse;
import com.foodfast.cliente_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.cliente_servicio.exception.ReglaNegocioException;
import com.foodfast.cliente_servicio.model.Cliente;
import com.foodfast.cliente_servicio.model.Direccion;
import com.foodfast.cliente_servicio.repository.ClienteRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PedidoClient pedidoClient;

    @InjectMocks
    private ClienteService clienteService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(clienteService, "pedidoIntegrationEnabled", false);
    }

    @Test
    void listar_debeRetornarClientesMapeados() {
        // Given
        when(clienteRepository.findAll()).thenReturn(List.of(clienteConDireccion(1L, "ana@foodfast.cl")));

        // When
        List<ClienteResponse> response = clienteService.listar();

        // Then
        assertEquals(1, response.size());
        assertEquals("ana@foodfast.cl", response.get(0).getEmail());
        assertEquals(1, response.get(0).getDirecciones().size());
    }

    @Test
    void listarActivos_debeRetornarSoloActivos() {
        // Given
        when(clienteRepository.findByActivoTrue()).thenReturn(List.of(clienteConDireccion(1L, "activo@foodfast.cl")));

        // When
        List<ClienteResponse> response = clienteService.listarActivos();

        // Then
        assertEquals(1, response.size());
        assertTrue(response.get(0).getActivo());
    }

    @Test
    void buscarPorId_debeRetornarClienteCuandoExiste() {
        // Given
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteConDireccion(1L, "damian@foodfast.cl")));

        // When
        ClienteResponse response = clienteService.buscarPorId(1L);

        // Then
        assertEquals(1L, response.getId());
        assertEquals("Damian", response.getNombre());
    }

    @Test
    void buscarPorId_debeLanzarErrorCuandoIdEsInvalido() {
        // When / Then
        assertThrows(ReglaNegocioException.class, () -> clienteService.buscarPorId(0L));
    }

    @Test
    void buscarPorId_debeLanzarErrorCuandoNoExiste() {
        // Given
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(RecursoNoEncontradoException.class, () -> clienteService.buscarPorId(99L));
    }

    @Test
    void buscarPorEmail_debeRetornarClienteNormalmente() {
        // Given
        when(clienteRepository.findByEmail("damian@foodfast.cl"))
                .thenReturn(Optional.of(clienteConDireccion(1L, "damian@foodfast.cl")));

        // When
        ClienteResponse response = clienteService.buscarPorEmail("damian@foodfast.cl");

        // Then
        assertEquals("damian@foodfast.cl", response.getEmail());
    }

    @Test
    void crear_debeGuardarClienteConEmailNormalizado() {
        // Given
        ClienteRequest request = requestValido();
        request.setEmail("DAMIAN@FOODFAST.CL");
        when(clienteRepository.existsByEmail("damian@foodfast.cl")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente cliente = invocation.getArgument(0);
            cliente.setId(1L);
            cliente.getDirecciones().forEach(direccion -> direccion.setId(10L));
            return cliente;
        });

        // When
        ClienteResponse response = clienteService.crear(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("damian@foodfast.cl", response.getEmail());
        assertTrue(response.getActivo());
        assertEquals(1, response.getDirecciones().size());
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void crear_debeLanzarErrorCuandoEmailYaExiste() {
        // Given
        ClienteRequest request = requestValido();
        when(clienteRepository.existsByEmail("damian@foodfast.cl")).thenReturn(true);

        // When / Then
        assertThrows(ReglaNegocioException.class, () -> clienteService.crear(request));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void crear_debeLanzarErrorCuandoNoHayDireccionPrincipal() {
        // Given
        ClienteRequest request = requestValido();
        request.getDirecciones().get(0).setPrincipal(false);
        when(clienteRepository.existsByEmail("damian@foodfast.cl")).thenReturn(false);

        // When / Then
        assertThrows(ReglaNegocioException.class, () -> clienteService.crear(request));
    }

    @Test
    void crear_debeLanzarErrorCuandoHayDosDireccionesPrincipales() {
        // Given
        ClienteRequest request = requestValido();
        request.setDirecciones(List.of(direccion(true), direccion(true)));
        when(clienteRepository.existsByEmail("damian@foodfast.cl")).thenReturn(false);

        // When / Then
        assertThrows(ReglaNegocioException.class, () -> clienteService.crear(request));
    }

    @Test
    void crear_debeLanzarErrorCuandoRequestEsNulo() {
        // When / Then
        assertThrows(ReglaNegocioException.class, () -> clienteService.crear(null));
    }

    @Test
    void crear_debePermitirClienteSinDirecciones() {
        // Given
        ClienteRequest request = requestValido();
        request.setDirecciones(null);
        when(clienteRepository.existsByEmail("damian@foodfast.cl")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente cliente = invocation.getArgument(0);
            cliente.setId(1L);
            return cliente;
        });

        // When
        ClienteResponse response = clienteService.crear(request);

        // Then
        assertEquals(0, response.getDirecciones().size());
    }

    @Test
    void actualizar_debeModificarDatosYDirecciones() {
        // Given
        Cliente existente = clienteConDireccion(1L, "viejo@foodfast.cl");
        ClienteRequest request = requestValido();
        request.setNombre("Nuevo");
        request.setEmail("nuevo@foodfast.cl");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(clienteRepository.findByEmail("nuevo@foodfast.cl")).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ClienteResponse response = clienteService.actualizar(1L, request);

        // Then
        assertEquals("Nuevo", response.getNombre());
        assertEquals("nuevo@foodfast.cl", response.getEmail());
        assertEquals(1, response.getDirecciones().size());
    }

    @Test
    void actualizar_debePermitirMantenerMismoEmail() {
        // Given
        Cliente existente = clienteConDireccion(1L, "damian@foodfast.cl");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(clienteRepository.findByEmail("damian@foodfast.cl")).thenReturn(Optional.of(existente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ClienteResponse response = clienteService.actualizar(1L, requestValido());

        // Then
        assertEquals("damian@foodfast.cl", response.getEmail());
    }

    @Test
    void actualizar_debeLanzarErrorCuandoOtroClienteUsaEmail() {
        // Given
        Cliente existente = clienteConDireccion(1L, "uno@foodfast.cl");
        Cliente otro = clienteConDireccion(2L, "damian@foodfast.cl");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(clienteRepository.findByEmail("damian@foodfast.cl")).thenReturn(Optional.of(otro));

        // When / Then
        assertThrows(ReglaNegocioException.class, () -> clienteService.actualizar(1L, requestValido()));
    }

    @Test
    void activar_debeCambiarActivoATrue() {
        // Given
        Cliente cliente = clienteConDireccion(1L, "a@foodfast.cl");
        cliente.setActivo(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        // When
        ClienteResponse response = clienteService.activar(1L);

        // Then
        assertTrue(response.getActivo());
    }

    @Test
    void desactivar_debeCambiarActivoAFalse() {
        // Given
        Cliente cliente = clienteConDireccion(1L, "a@foodfast.cl");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        // When
        ClienteResponse response = clienteService.desactivar(1L);

        // Then
        assertFalse(response.getActivo());
    }

    @Test
    void eliminar_debeEliminarClienteExistente() {
        // Given
        Cliente cliente = clienteConDireccion(1L, "a@foodfast.cl");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        // When
        clienteService.eliminar(1L);

        // Then
        verify(clienteRepository).delete(cliente);
    }

    @Test
    void listarPedidosDelCliente_debeRetornarVacioCuandoIntegracionDesactivada() {
        // Given
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteConDireccion(1L, "a@foodfast.cl")));

        // When
        List<PedidoResumenResponse> pedidos = clienteService.listarPedidosDelCliente(1L, "Bearer token");

        // Then
        assertTrue(pedidos.isEmpty());
        verify(pedidoClient, never()).listarPedidosPorCliente(any(), anyString());
    }

    @Test
    void listarPedidosDelCliente_debeConsultarPedidoClientCuandoIntegracionActiva() {
        // Given
        ReflectionTestUtils.setField(clienteService, "pedidoIntegrationEnabled", true);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteConDireccion(1L, "a@foodfast.cl")));
        PedidoResumenResponse pedido = PedidoResumenResponse.builder()
                .id(100L)
                .clienteId(1L)
                .total(BigDecimal.valueOf(15990))
                .estado("CONFIRMADO")
                .build();
        when(pedidoClient.listarPedidosPorCliente(eq(1L), eq("Bearer token"))).thenReturn(List.of(pedido));

        // When
        List<PedidoResumenResponse> pedidos = clienteService.listarPedidosDelCliente(1L, "Bearer token");

        // Then
        assertEquals(1, pedidos.size());
        verify(pedidoClient).listarPedidosPorCliente(1L, "Bearer token");
    }

    private ClienteRequest requestValido() {
        return ClienteRequest.builder()
                .nombre("Damian")
                .apellido("Galaz")
                .email("damian@foodfast.cl")
                .telefono("+56911112222")
                .direcciones(List.of(direccion(true)))
                .build();
    }

    private DireccionRequest direccion(boolean principal) {
        return DireccionRequest.builder()
                .calle("Av. Providencia")
                .numero("1234")
                .comuna("Providencia")
                .ciudad("Santiago")
                .referencia("Depto 501")
                .principal(principal)
                .build();
    }

    private Cliente clienteConDireccion(Long id, String email) {
        Cliente cliente = Cliente.builder()
                .id(id)
                .nombre("Damian")
                .apellido("Galaz")
                .email(email)
                .telefono("+56911112222")
                .activo(true)
                .direcciones(new ArrayList<>())
                .build();
        Direccion direccion = Direccion.builder()
                .id(10L)
                .calle("Av. Providencia")
                .numero("1234")
                .comuna("Providencia")
                .ciudad("Santiago")
                .referencia("Depto 501")
                .principal(true)
                .build();
        cliente.agregarDireccion(direccion);
        return cliente;
    }
}
