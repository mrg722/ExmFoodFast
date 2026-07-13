package com.foodfast.pago_servicio.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodfast.pago_servicio.dto.ActualizarPagoRequest;
import com.foodfast.pago_servicio.dto.PagoRequest;
import com.foodfast.pago_servicio.dto.PagoResponse;
import com.foodfast.pago_servicio.dto.ProcesarPagoRequest;
import com.foodfast.pago_servicio.exception.RecursoNoEncontradoException;
import com.foodfast.pago_servicio.exception.ReglaNegocioException;
import com.foodfast.pago_servicio.exception.ServicioExternoException;
import com.foodfast.pago_servicio.model.EstadoPago;
import com.foodfast.pago_servicio.model.MetodoPago;
import com.foodfast.pago_servicio.service.PagoService;
import com.foodfast.pago_servicio.security.JwtAuthenticationFilter;
import com.foodfast.pago_servicio.security.JwtService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PagoController.class)
@AutoConfigureMockMvc(addFilters = false)
class PagoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PagoService pagoService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void estadoDebeResponderOk() throws Exception {
        mockMvc.perform(get("/api/pagos/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("OK"));
    }

    @Test
    void listarYBuscarDebenResponderOk() throws Exception {
        PagoResponse response = respuestaBase();
        when(pagoService.listar()).thenReturn(List.of(response));
        when(pagoService.buscarPorId(1L)).thenReturn(response);
        when(pagoService.buscarPorPedidoId(10L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/pagos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));

        mockMvc.perform(get("/api/pagos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pedidoId").value(10));

        mockMvc.perform(get("/api/pagos/pedido/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].estadoPago").value("PENDIENTE"));
    }

    @Test
    void crearDebeResponderCreated() throws Exception {
        PagoRequest request = new PagoRequest();
        request.setPedidoId(10L);
        request.setMonto(new BigDecimal("15990"));
        request.setMetodoPago(MetodoPago.WEBPAY_SIMULADO);
        when(pagoService.crear(any(PagoRequest.class))).thenReturn(respuestaBase());

        mockMvc.perform(post("/api/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.estadoPago").value("PENDIENTE"));
    }

    @Test
    void crearDebeResponderBadRequestConValidacion() throws Exception {
        PagoRequest request = new PagoRequest();
        request.setPedidoId(null);
        request.setMonto(BigDecimal.ZERO);

        mockMvc.perform(post("/api/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error de validación"));
    }

    @Test
    void actualizarProcesarAnularYEliminarDebenResponderCorrectamente() throws Exception {
        ActualizarPagoRequest update = new ActualizarPagoRequest();
        update.setMonto(new BigDecimal("20000"));
        update.setMetodoPago(MetodoPago.TARJETA);

        ProcesarPagoRequest procesar = new ProcesarPagoRequest();
        procesar.setPagoId(1L);
        procesar.setAprobado(true);

        PagoResponse actualizado = respuestaBase();
        actualizado.setMonto(new BigDecimal("20000"));
        PagoResponse aprobado = respuestaBase();
        aprobado.setEstadoPago(EstadoPago.APROBADO);

        when(pagoService.actualizar(any(Long.class), any(ActualizarPagoRequest.class))).thenReturn(actualizado);
        when(pagoService.procesar(any(ProcesarPagoRequest.class))).thenReturn(aprobado);
        when(pagoService.anular(1L)).thenReturn(respuestaBase());
        doNothing().when(pagoService).eliminar(1L);

        mockMvc.perform(put("/api/pagos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.monto").value(20000));

        mockMvc.perform(post("/api/pagos/procesar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(procesar)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoPago").value("APROBADO"));

        mockMvc.perform(patch("/api/pagos/1/anular"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/pagos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void erroresDebenResponderCodigosCorrectos() throws Exception {
        when(pagoService.buscarPorId(404L)).thenThrow(new RecursoNoEncontradoException("no encontrado"));
        when(pagoService.buscarPorId(400L)).thenThrow(new ReglaNegocioException("regla inválida"));
        when(pagoService.buscarPorId(503L)).thenThrow(new ServicioExternoException("pedido caído"));

        mockMvc.perform(get("/api/pagos/404")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/pagos/400")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/pagos/503")).andExpect(status().isServiceUnavailable());
    }

    private PagoResponse respuestaBase() {
        return PagoResponse.builder()
                .id(1L)
                .pedidoId(10L)
                .monto(new BigDecimal("15990"))
                .metodoPago(MetodoPago.WEBPAY_SIMULADO)
                .estadoPago(EstadoPago.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }
}
