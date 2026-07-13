package com.foodfast.cliente_servicio.service;

import com.foodfast.cliente_servicio.client.PedidoClient;
import com.foodfast.cliente_servicio.dto.ClienteRequest;
import com.foodfast.cliente_servicio.dto.ClienteResponse;
import com.foodfast.cliente_servicio.dto.DireccionRequest;
import com.foodfast.cliente_servicio.dto.DireccionResponse;
import com.foodfast.cliente_servicio.dto.PedidoResumenResponse;
import com.foodfast.cliente_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.cliente_servicio.exception.ReglaNegocioException;
import com.foodfast.cliente_servicio.model.Cliente;
import com.foodfast.cliente_servicio.model.Direccion;
import com.foodfast.cliente_servicio.repository.ClienteRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PedidoClient pedidoClient;

    @Value("${pedido.service.enabled:false}")
    private boolean pedidoIntegrationEnabled;

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listarActivos() {
        return clienteRepository.findByActivoTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        return toResponse(obtenerCliente(id));
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorEmail(String email) {
        validarTexto(email, "email");
        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con email: " + email));
        return toResponse(cliente);
    }

    @Transactional(readOnly = true)
    public List<PedidoResumenResponse> listarPedidosDelCliente(Long clienteId, String authorizationHeader) {
        obtenerCliente(clienteId);

        if (!pedidoIntegrationEnabled) {
            log.info("Integracion con pedido-servicio desactivada. Retornando lista vacia para clienteId={}", clienteId);
            return List.of();
        }

        return pedidoClient.listarPedidosPorCliente(clienteId, authorizationHeader);
    }

    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        validarRequest(request);
        String emailNormalizado = normalizarEmail(request.getEmail());

        if (clienteRepository.existsByEmail(emailNormalizado)) {
            throw new ReglaNegocioException("Ya existe un cliente con el email: " + emailNormalizado);
        }

        Cliente cliente = Cliente.builder()
                .nombre(request.getNombre().trim())
                .apellido(request.getApellido().trim())
                .email(emailNormalizado)
                .telefono(request.getTelefono().trim())
                .activo(true)
                .build();

        agregarDirecciones(cliente, request.getDirecciones());
        validarDireccionPrincipal(cliente);

        Cliente guardado = clienteRepository.save(cliente);
        log.info("Cliente creado id={} email={}", guardado.getId(), guardado.getEmail());
        return toResponse(guardado);
    }

    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        validarRequest(request);
        Cliente cliente = obtenerCliente(id);
        String emailNormalizado = normalizarEmail(request.getEmail());

        clienteRepository.findByEmail(emailNormalizado).ifPresent(encontrado -> {
            if (!encontrado.getId().equals(id)) {
                throw new ReglaNegocioException("Otro cliente ya usa el email: " + emailNormalizado);
            }
        });

        cliente.setNombre(request.getNombre().trim());
        cliente.setApellido(request.getApellido().trim());
        cliente.setEmail(emailNormalizado);
        cliente.setTelefono(request.getTelefono().trim());

        cliente.limpiarDirecciones();
        agregarDirecciones(cliente, request.getDirecciones());
        validarDireccionPrincipal(cliente);

        Cliente actualizado = clienteRepository.save(cliente);
        log.info("Cliente actualizado id={}", actualizado.getId());
        return toResponse(actualizado);
    }

    @Transactional
    public ClienteResponse activar(Long id) {
        Cliente cliente = obtenerCliente(id);
        cliente.setActivo(true);
        return toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse desactivar(Long id) {
        Cliente cliente = obtenerCliente(id);
        cliente.setActivo(false);
        return toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public void eliminar(Long id) {
        Cliente cliente = obtenerCliente(id);
        clienteRepository.delete(cliente);
        log.info("Cliente eliminado id={}", id);
    }

    private Cliente obtenerCliente(Long id) {
        if (id == null || id <= 0) {
            throw new ReglaNegocioException("El id del cliente debe ser mayor a cero");
        }
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con id: " + id));
    }

    private void validarRequest(ClienteRequest request) {
        if (request == null) {
            throw new ReglaNegocioException("La solicitud del cliente es obligatoria");
        }
        validarTexto(request.getNombre(), "nombre");
        validarTexto(request.getApellido(), "apellido");
        validarTexto(request.getEmail(), "email");
        validarTexto(request.getTelefono(), "telefono");
    }

    private void validarTexto(String valor, String campo) {
        if (valor == null || valor.trim().isBlank()) {
            throw new ReglaNegocioException("El campo " + campo + " es obligatorio");
        }
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }

    private void agregarDirecciones(Cliente cliente, List<DireccionRequest> direcciones) {
        if (direcciones == null) {
            return;
        }
        direcciones.forEach(direccionRequest -> cliente.agregarDireccion(toDireccion(direccionRequest)));
    }

    private Direccion toDireccion(DireccionRequest request) {
        validarDireccionRequest(request);
        return Direccion.builder()
                .calle(request.getCalle().trim())
                .numero(request.getNumero().trim())
                .comuna(request.getComuna().trim())
                .ciudad(request.getCiudad().trim())
                .referencia(request.getReferencia())
                .principal(Boolean.TRUE.equals(request.getPrincipal()))
                .build();
    }

    private void validarDireccionRequest(DireccionRequest request) {
        if (request == null) {
            throw new ReglaNegocioException("La direccion no puede ser nula");
        }
        validarTexto(request.getCalle(), "calle");
        validarTexto(request.getNumero(), "numero");
        validarTexto(request.getComuna(), "comuna");
        validarTexto(request.getCiudad(), "ciudad");
        if (request.getPrincipal() == null) {
            throw new ReglaNegocioException("Debe indicar si la direccion es principal");
        }
    }

    private void validarDireccionPrincipal(Cliente cliente) {
        long principales = cliente.getDirecciones()
                .stream()
                .filter(direccion -> Boolean.TRUE.equals(direccion.getPrincipal()))
                .count();

        if (!cliente.getDirecciones().isEmpty() && principales != 1) {
            throw new ReglaNegocioException("El cliente debe tener exactamente una direccion principal");
        }
    }

    private ClienteResponse toResponse(Cliente cliente) {
        return ClienteResponse.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .email(cliente.getEmail())
                .telefono(cliente.getTelefono())
                .activo(cliente.getActivo())
                .direcciones(cliente.getDirecciones().stream().map(this::toDireccionResponse).toList())
                .build();
    }

    private DireccionResponse toDireccionResponse(Direccion direccion) {
        return DireccionResponse.builder()
                .id(direccion.getId())
                .calle(direccion.getCalle())
                .numero(direccion.getNumero())
                .comuna(direccion.getComuna())
                .ciudad(direccion.getCiudad())
                .referencia(direccion.getReferencia())
                .principal(direccion.getPrincipal())
                .build();
    }
}
