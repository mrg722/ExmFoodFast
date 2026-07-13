package com.foodfast.catalogo_servicio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ClientRequest;
import com.foodfast.catalogo_servicio.config.RequestHeaderContext;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
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
                });
    }
}
