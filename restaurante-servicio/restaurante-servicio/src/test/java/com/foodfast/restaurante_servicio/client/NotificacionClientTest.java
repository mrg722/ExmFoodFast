package com.foodfast.restaurante_servicio.client;

import com.foodfast.restaurante_servicio.dto.ApiResponse;
import com.foodfast.restaurante_servicio.dto.NotificacionResponse;
import com.foodfast.restaurante_servicio.exception.ServicioExternoException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotificacionClientTest {

    @Test
    void debeCrearNotificacionRestaurante() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        NotificacionClient client = new NotificacionClient(restTemplate, "http://localhost:8089");
        NotificacionResponse data = NotificacionResponse.builder()
                .id(1L)
                .clienteId(0L)
                .tipo("RESTAURANTE_ACTUALIZADO")
                .canal("SISTEMA")
                .estado("PENDIENTE")
                .titulo("Título")
                .mensaje("Mensaje")
                .build();
        ApiResponse<NotificacionResponse> body = ApiResponse.ok("ok", data);

        when(restTemplate.exchange(
                eq("http://localhost:8089/api/notificaciones"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponse<NotificacionResponse>>>any()
        )).thenReturn(ResponseEntity.ok(body));

        NotificacionResponse response = client.crearNotificacionRestaurante(1L, "Título", "Mensaje");

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void debeLanzarErrorSiRespuestaNoTieneData() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        NotificacionClient client = new NotificacionClient(restTemplate, "http://localhost:8089");
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponse<NotificacionResponse>>>any()
        )).thenReturn(ResponseEntity.ok(ApiResponse.ok("sin data", null)));

        assertThatThrownBy(() -> client.crearNotificacionRestaurante(1L, "Título", "Mensaje"))
                .isInstanceOf(ServicioExternoException.class);
    }

    @Test
    void debeLanzarErrorSiRestTemplateFalla() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        NotificacionClient client = new NotificacionClient(restTemplate, "http://localhost:8089");
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<ApiResponse<NotificacionResponse>>>any()
        )).thenThrow(new RestClientException("sin conexión"));

        assertThatThrownBy(() -> client.crearNotificacionRestaurante(1L, "Título", "Mensaje"))
                .isInstanceOf(ServicioExternoException.class)
                .hasMessageContaining("No se pudo comunicar");
    }
}
