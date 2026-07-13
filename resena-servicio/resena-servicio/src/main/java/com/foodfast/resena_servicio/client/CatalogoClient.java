package com.foodfast.resena_servicio.client;

import com.foodfast.resena_servicio.dto.ApiResponse;
import com.foodfast.resena_servicio.dto.ProductoResponse;
import com.foodfast.resena_servicio.exception.ServicioExternoException;
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
public class CatalogoClient {

    private final WebClient webClient;
    private final Long timeoutSeconds;

    public CatalogoClient(WebClient.Builder webClientBuilder,
                          @Value("${services.catalogo.url}") String catalogoServiceUrl,
                          @Value("${app.webclient.timeout-seconds:5}") Long timeoutSeconds) {
        this.webClient = webClientBuilder.baseUrl(catalogoServiceUrl).build();
        this.timeoutSeconds = timeoutSeconds;
    }

    public ProductoResponse buscarProducto(Long productoId) {
        try {
            ApiResponse<ProductoResponse> response = webClient.get()
                    .uri("/api/productos/{id}", productoId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .defaultIfEmpty("Sin detalle")
                                    .map(body -> new ServicioExternoException(
                                            "catalogo-servicio respondió con HTTP "
                                                    + clientResponse.statusCode().value()
                                                    + ": " + body)))
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<ProductoResponse>>() {})
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            if (response == null || response.getData() == null) {
                throw new ServicioExternoException("catalogo-servicio no devolvió datos del producto");
            }
            return response.getData();
        } catch (WebClientResponseException ex) {
            throw new ServicioExternoException("catalogo-servicio respondió con HTTP " + ex.getStatusCode().value());
        } catch (ServicioExternoException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error al consultar catalogo-servicio", ex);
            throw new ServicioExternoException("No se pudo comunicar con catalogo-servicio");
        }
    }
}
