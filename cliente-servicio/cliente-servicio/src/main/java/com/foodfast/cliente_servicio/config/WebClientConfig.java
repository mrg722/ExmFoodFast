package com.foodfast.cliente_servicio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ClientRequest;
import com.foodfast.cliente_servicio.config.RequestHeaderContext;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient pedidoWebClient(@Value("${pedido.service.url}") String pedidoServiceUrl) {
        return WebClient.builder()
                .filter((request, next) -> {
                    String auth = RequestHeaderContext.getAuthorization();
                    if (auth != null && !auth.isBlank() && !request.headers().containsKey("Authorization")) {
                        ClientRequest newReq = ClientRequest.from(request)
                                .header("Authorization", auth)
                                .build();
                        return next.exchange(newReq);
                    }
                    return next.exchange(request);
                })
                .baseUrl(pedidoServiceUrl)
                .build();
    }
}
