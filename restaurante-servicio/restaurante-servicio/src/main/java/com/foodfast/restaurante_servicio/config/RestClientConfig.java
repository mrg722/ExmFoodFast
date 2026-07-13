package com.foodfast.restaurante_servicio.config;

import java.util.Collections;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder.build();
        restTemplate.setInterceptors(Collections.singletonList((request, body, execution) -> {
            String auth = RequestHeaderContext.getAuthorization();
            if (auth != null && !auth.isBlank() && !request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                request.getHeaders().set(HttpHeaders.AUTHORIZATION, auth);
            }
            return execution.execute(request, body);
        }));
        return restTemplate;
    }
}
