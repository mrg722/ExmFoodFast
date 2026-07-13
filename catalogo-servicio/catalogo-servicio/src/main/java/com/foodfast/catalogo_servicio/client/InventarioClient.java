package com.foodfast.catalogo_servicio.client;

import com.foodfast.catalogo_servicio.dto.ApiResponse;
import com.foodfast.catalogo_servicio.dto.InventarioStockResponse;
import com.foodfast.catalogo_servicio.exception.ServicioExternoException;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class InventarioClient {

    private final WebClient webClient;
    private final boolean integrationEnabled;
    private final long timeoutSeconds;

    public InventarioClient(WebClient.Builder webClientBuilder,
                            @Value("${services.inventario.url}") String inventarioUrl,
                            @Value("${services.inventario.enabled:false}") boolean integrationEnabled,
                            @Value("${app.webclient.timeout-seconds:5}") long timeoutSeconds) {
        this.webClient = webClientBuilder.baseUrl(inventarioUrl).build();
        this.integrationEnabled = integrationEnabled;
        this.timeoutSeconds = timeoutSeconds;
    }

    public InventarioStockResponse consultarStock(Long productoId) {
        if (!integrationEnabled) {
            return new InventarioStockResponse(productoId, null, null, null, null);
        }

        try {
            ApiResponse<InventarioStockResponse> response = webClient.get()
                    .uri("/api/inventarios/stock/{productoId}", productoId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .defaultIfEmpty("Sin detalle")
                                    .map(body -> new ServicioExternoException(
                                            "inventario-servicio respondió HTTP "
                                                    + clientResponse.statusCode().value()
                                                    + ": " + body)))
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<InventarioStockResponse>>() {})
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            if (response == null || response.getDatos() == null) {
                throw new ServicioExternoException("inventario-servicio no devolvió datos de stock");
            }

            return response.getDatos();
        } catch (ServicioExternoException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error al consultar inventario-servicio", ex);
            throw new ServicioExternoException("No se pudo comunicar con inventario-servicio");
        }
    }

    public boolean isIntegrationEnabled() {
        return integrationEnabled;
    }
}
