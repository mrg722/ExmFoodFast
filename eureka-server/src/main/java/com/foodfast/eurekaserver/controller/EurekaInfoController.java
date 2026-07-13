package com.foodfast.eurekaserver.controller;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EurekaInfoController {

    @GetMapping("/eureka-info")
    public Map<String, Object> info() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("servicio", "eureka-server");
        response.put("puerto", 8761);
        response.put("estado", "OK");
        response.put("funcion", "Registro central de microservicios FoodFast");
        response.put("debeRegistrar", List.of(
                "api-gateway",
                "autenticacion-servicio",
                "catalogo-servicio",
                "inventario-servicio",
                "pedido-servicio",
                "reparto-servicio",
                "cliente-servicio",
                "pago-servicio",
                "restaurante-servicio",
                "resena-servicio",
                "notificacion-servicio"
        ));
        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }
}
