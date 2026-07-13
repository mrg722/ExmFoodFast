package com.foodfast.cliente_servicio.client;

import com.foodfast.cliente_servicio.dto.ApiResponse;
import com.foodfast.cliente_servicio.dto.PedidoResumenResponse;
import com.foodfast.cliente_servicio.exception.ServicioExternoException;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
public class PedidoClient {

    private final WebClient pedidoWebClient;
    private final Long timeoutSeconds;

    public PedidoClient(WebClient pedidoWebClient,
                        @Value("${app.webclient.timeout-seconds:5}") Long timeoutSeconds) {
        this.pedidoWebClient = pedidoWebClient;
        this.timeoutSeconds = timeoutSeconds;
    }

    public List<PedidoResumenResponse> listarPedidosPorCliente(Long clienteId, String authorizationHeader) {
        try {
            ApiResponse<List<PedidoResumenResponse>> response = pedidoWebClient.get()
                    .uri("/api/pedidos/cliente/{clienteId}", clienteId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader == null ? "" : authorizationHeader)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .defaultIfEmpty("Sin detalle")
                                    .map(body -> new ServicioExternoException(
                                            "pedido-servicio respondio HTTP "
                                                    + clientResponse.statusCode().value()
                                                    + ": " + body)))
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<PedidoResumenResponse>>>() {})
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            if (response == null || response.getData() == null) {
                return List.of();
            }

            return response.getData();
        } catch (WebClientResponseException ex) {
            throw new ServicioExternoException("pedido-servicio respondio HTTP " + ex.getStatusCode().value());
        } catch (ServicioExternoException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error al comunicarse con pedido-servicio", ex);
            throw new ServicioExternoException("No se pudo comunicar con pedido-servicio");
        }
    }
}
