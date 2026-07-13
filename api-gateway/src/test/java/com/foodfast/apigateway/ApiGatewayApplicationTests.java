package com.foodfast.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.gateway.discovery.locator.enabled=false"
        }
)
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
        // Verifica que el contexto Spring del API Gateway inicia correctamente.
    }
}
