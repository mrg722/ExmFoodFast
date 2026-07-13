package com.foodfast.pago_servicio.client;

import com.foodfast.pago_servicio.dto.ApiResponse;
import com.foodfast.pago_servicio.dto.PedidoResponse;
import com.foodfast.pago_servicio.exception.ServicioExternoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class PedidoClient {

    private final RestClient restClient;
    private final boolean integrationEnabled;

    public PedidoClient(RestClient.Builder restClientBuilder,
                        @Value("${services.pedido.url}") String pedidoServiceUrl,
                        @Value("${services.pedido.enabled:false}") boolean integrationEnabled) {
        this.restClient = restClientBuilder.baseUrl(pedidoServiceUrl).build();
        this.integrationEnabled = integrationEnabled;
    }

    public PedidoResponse obtenerPedido(Long pedidoId) {
        if (!integrationEnabled) {
            log.info("Integración con pedido-servicio desactivada; se omite validación remota pedidoId={}", pedidoId);
            return PedidoResponse.builder()
                    .id(pedidoId)
                    .estado("NO_VALIDADO")
                    .build();
        }

        try {
            ApiResponse<PedidoResponse> response = restClient.get()
                    .uri("/api/pedidos/{id}", pedidoId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, clientResponse) -> {
                        throw new ServicioExternoException(
                                "pedido-servicio respondió HTTP " + clientResponse.getStatusCode().value());
                    })
                    .body(new ParameterizedTypeReference<ApiResponse<PedidoResponse>>() {});

            if (response == null || response.getData() == null) {
                throw new ServicioExternoException("pedido-servicio no devolvió datos del pedido");
            }

            return response.getData();
        } catch (ServicioExternoException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new ServicioExternoException("No se pudo comunicar con pedido-servicio");
        }
    }
}
