package com.foodfast.pedido_servicio.config;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ClientRequest;
import com.foodfast.pedido_servicio.config.RequestHeaderContext;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder(
            @Value("${app.webclient.timeout-seconds:5}") Long timeoutSeconds) {

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(timeoutSeconds * 1000))
                .responseTimeout(Duration.ofSeconds(timeoutSeconds));

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();

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
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies);
    }
}
