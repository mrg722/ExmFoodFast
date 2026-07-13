package com.foodfast.reparto_servicio.client;

import com.foodfast.reparto_servicio.dto.ApiResponse;
import com.foodfast.reparto_servicio.dto.PedidoResponse;
import com.foodfast.reparto_servicio.exception.ServicioExternoException;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
public class PedidoClient {

    private final WebClient pedidoWebClient;
    private final Long timeoutSeconds;

    public PedidoClient(WebClient pedidoWebClient,
                        @Value("${app.webclient.timeout-seconds:5}") Long timeoutSeconds) {
        this.pedidoWebClient = pedidoWebClient;
        this.timeoutSeconds = timeoutSeconds;
    }

    public PedidoResponse obtenerPedido(Long pedidoId) {
        try {
            ApiResponse<PedidoResponse> response = pedidoWebClient.get()
                    .uri("/api/pedidos/{id}", pedidoId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .defaultIfEmpty("Sin detalle")
                                    .map(body -> new ServicioExternoException(
                                            "pedido-servicio respondió con HTTP "
                                                    + clientResponse.statusCode().value()
                                                    + ": " + body)))
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<PedidoResponse>>() {})
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            if (response == null || response.getData() == null) {
                throw new ServicioExternoException("pedido-servicio no devolvió datos del pedido");
            }

            return response.getData();
        } catch (WebClientResponseException ex) {
            throw new ServicioExternoException("pedido-servicio respondió con HTTP " + ex.getStatusCode().value());
        } catch (ServicioExternoException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error al consultar pedido-servicio", ex);
            throw new ServicioExternoException("No se pudo comunicar con pedido-servicio");
        }
    }
}
