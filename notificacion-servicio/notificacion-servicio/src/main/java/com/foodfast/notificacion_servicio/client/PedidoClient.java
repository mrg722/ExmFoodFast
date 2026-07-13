package com.foodfast.notificacion_servicio.client;

import com.foodfast.notificacion_servicio.dto.ApiResponse;
import com.foodfast.notificacion_servicio.dto.PedidoResponse;
import com.foodfast.notificacion_servicio.exception.ServicioExternoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
public class PedidoClient {

    private final RestClient restClient;
    private final boolean enabled;

    public PedidoClient(RestClient.Builder restClientBuilder,
                        @Value("${services.pedido.url}") String pedidoServiceUrl,
                        @Value("${services.pedido.enabled:false}") boolean enabled) {
        this.restClient = restClientBuilder.baseUrl(pedidoServiceUrl).build();
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public PedidoResponse buscarPedido(Long pedidoId) {
        if (!enabled) {
            return null;
        }

        try {
            ApiResponse<PedidoResponse> response = restClient.get()
                    .uri("/api/pedidos/{id}", pedidoId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, responseSpec) -> {
                        throw new ServicioExternoException("pedido-servicio respondió con HTTP "
                                + responseSpec.getStatusCode().value());
                    })
                    .body(new ParameterizedTypeReference<ApiResponse<PedidoResponse>>() {});

            if (response == null || response.getData() == null) {
                throw new ServicioExternoException("pedido-servicio no devolvió datos del pedido");
            }
            return response.getData();
        } catch (RestClientResponseException ex) {
            throw new ServicioExternoException("pedido-servicio respondió con HTTP " + ex.getStatusCode().value());
        } catch (ServicioExternoException ex) {
            throw ex;
        } catch (RestClientException ex) {
            log.error("Error de comunicación con pedido-servicio", ex);
            throw new ServicioExternoException("No se pudo comunicar con pedido-servicio");
        }
    }
}
