package com.foodfast.restaurante_servicio.client;

import com.foodfast.restaurante_servicio.dto.ApiResponse;
import com.foodfast.restaurante_servicio.dto.NotificacionRequest;
import com.foodfast.restaurante_servicio.dto.NotificacionResponse;
import com.foodfast.restaurante_servicio.exception.ServicioExternoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class NotificacionClient {

    private final RestTemplate restTemplate;
    private final String notificacionServiceUrl;

    public NotificacionClient(RestTemplate restTemplate,
                              @Value("${services.notificacion.url}") String notificacionServiceUrl) {
        this.restTemplate = restTemplate;
        this.notificacionServiceUrl = notificacionServiceUrl;
    }

    public NotificacionResponse crearNotificacionRestaurante(Long restauranteId, String titulo, String mensaje) {
        try {
            NotificacionRequest request = NotificacionRequest.builder()
                    .clienteId(0L)
                    .tipo("RESTAURANTE_ACTUALIZADO")
                    .canal("SISTEMA")
                    .titulo(titulo)
                    .mensaje(mensaje)
                    .referenciaTipo("RESTAURANTE")
                    .referenciaId(restauranteId)
                    .build();

            ResponseEntity<ApiResponse<NotificacionResponse>> response = restTemplate.exchange(
                    notificacionServiceUrl + "/api/notificaciones",
                    HttpMethod.POST,
                    new org.springframework.http.HttpEntity<>(request),
                    new ParameterizedTypeReference<ApiResponse<NotificacionResponse>>() {}
            );

            ApiResponse<NotificacionResponse> body = response.getBody();
            if (body == null || body.getData() == null) {
                throw new ServicioExternoException("notificacion-servicio no devolvió datos");
            }
            return body.getData();
        } catch (ServicioExternoException ex) {
            throw ex;
        } catch (RestClientException ex) {
            log.error("Error al comunicarse con notificacion-servicio", ex);
            throw new ServicioExternoException("No se pudo comunicar con notificacion-servicio");
        }
    }
}
