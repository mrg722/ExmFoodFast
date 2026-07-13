package com.foodfast.pago_servicio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder(@Value("${app.rest-client.timeout-seconds:5}") int timeoutSeconds) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        int timeoutMillis = timeoutSeconds * 1000;
        requestFactory.setConnectTimeout(timeoutMillis);
        requestFactory.setReadTimeout(timeoutMillis);
        return RestClient.builder()
                .requestFactory(requestFactory)
                .requestInterceptor((request, body, execution) -> {
                    String auth = RequestHeaderContext.getAuthorization();
                    if (auth != null && !auth.isBlank() && !request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                        request.getHeaders().set(HttpHeaders.AUTHORIZATION, auth);
                    }
                    return execution.execute(request, body);
                });
    }
}
