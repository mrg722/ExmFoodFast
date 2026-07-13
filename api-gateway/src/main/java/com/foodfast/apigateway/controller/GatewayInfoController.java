package com.foodfast.apigateway.controller;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gateway")
public class GatewayInfoController {

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("servicio", "api-gateway");
        response.put("puerto", 8080);
        response.put("estado", "OK");
        response.put("funcion", "Entrada unica y enrutamiento de microservicios FoodFast");
        response.put("notaJwt", "Gateway conserva el header Authorization; Auth genera JWT y los microservicios protegidos validan el token.");
        response.put("rutas", List.of(
                "/api/auth/** -> autenticacion-servicio:8090",
                "/api/productos/** o /api/catalogo/** -> catalogo-servicio:8081",
                "/api/inventarios/** o /api/inventario/** -> inventario-servicio:8082",
                "/api/pedidos/** -> pedido-servicio:8083",
                "/api/repartos/** -> reparto-servicio:8084",
                "/api/clientes/** -> cliente-servicio:8085",
                "/api/pagos/** -> pago-servicio:8086",
                "/api/restaurantes/** -> restaurante-servicio:8087",
                "/api/resenas/** -> resena-servicio:8088",
                "/api/notificaciones/** -> notificacion-servicio:8089"
        ));
        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }
}
