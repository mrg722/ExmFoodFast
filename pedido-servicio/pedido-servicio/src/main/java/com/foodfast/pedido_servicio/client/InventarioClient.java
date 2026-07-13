package com.foodfast.pedido_servicio.client;

import com.foodfast.pedido_servicio.dto.ApiResponse;
import com.foodfast.pedido_servicio.dto.DescontarStockRequest;
import com.foodfast.pedido_servicio.dto.InventarioResponse;
import com.foodfast.pedido_servicio.dto.InventarioStockResponse;
import com.foodfast.pedido_servicio.exception.ServicioExternoException;
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
public class InventarioClient {

    private final WebClient webClient;
    private final Long timeoutSeconds;

    public InventarioClient(WebClient.Builder webClientBuilder,
                            @Value("${services.inventario.url}") String inventarioServiceUrl,
                            @Value("${app.webclient.timeout-seconds:5}") Long timeoutSeconds) {
        this.webClient = webClientBuilder.baseUrl(inventarioServiceUrl).build();
        this.timeoutSeconds = timeoutSeconds;
    }

    public InventarioStockResponse consultarStock(Long productoId) {
        return consultarStock(productoId, null);
    }

    public InventarioStockResponse consultarStock(Long productoId, String authorizationHeader) {
        try {
            log.info("Consultando stock en inventario-servicio productoId={} auth={}", productoId,
                    authorizationHeader != null ? "present" : "missing");

            WebClient.RequestHeadersSpec<?> requestSpec = webClient.get()
                    .uri("/api/inventarios/stock/{productoId}", productoId);

            if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                requestSpec.header("Authorization", authorizationHeader);
            }

            ApiResponse<InventarioStockResponse> response = requestSpec.retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .defaultIfEmpty("Sin detalle")
                                    .map(body -> new ServicioExternoException(
                                            "Inventario respondió con error HTTP "
                                                    + clientResponse.statusCode().value()
                                                    + ": " + body)))
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<InventarioStockResponse>>() {})
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            if (response == null || response.getData() == null) {
                throw new ServicioExternoException("Inventario no devolvió datos de stock");
            }

            return response.getData();
        } catch (WebClientResponseException ex) {
            throw new ServicioExternoException(
                    "Inventario respondió con error HTTP " + ex.getStatusCode().value()
            );
        } catch (ServicioExternoException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error al consultar inventario-servicio", ex);
            throw new ServicioExternoException("No se pudo comunicar con inventario-servicio");
        }
    }

    public InventarioResponse descontarStock(Long productoId, Integer cantidad) {
        return descontarStock(productoId, cantidad, null);
    }

    public InventarioResponse descontarStock(Long productoId, Integer cantidad, String authorizationHeader) {
        try {
            log.info("Solicitando descuento de stock productoId={} cantidad={} auth={}", productoId, cantidad,
                    authorizationHeader != null ? "present" : "missing");

            DescontarStockRequest request = DescontarStockRequest.builder()
                    .productoId(productoId)
                    .cantidad(cantidad)
                    .build();

            WebClient.RequestBodySpec requestBodySpec = webClient.post()
                    .uri("/api/inventarios/descontar");

            if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                requestBodySpec.header("Authorization", authorizationHeader);
            }

            ApiResponse<InventarioResponse> response = requestBodySpec
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .defaultIfEmpty("Sin detalle")
                                    .map(body -> new ServicioExternoException(
                                            "Inventario respondió con error HTTP "
                                                    + clientResponse.statusCode().value()
                                                    + ": " + body)))
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<InventarioResponse>>() {})
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            if (response == null || response.getData() == null) {
                throw new ServicioExternoException("Inventario no devolvió datos del descuento de stock");
            }

            return response.getData();
        } catch (WebClientResponseException ex) {
            throw new ServicioExternoException(
                    "Inventario respondió con error HTTP " + ex.getStatusCode().value()
            );
        } catch (ServicioExternoException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error al descontar stock en inventario-servicio", ex);
            throw new ServicioExternoException("No se pudo descontar stock en inventario-servicio");
        }
    }
}
