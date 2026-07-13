package com.foodfast.autenticacion_servicio.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ServiceLayerEv3Test {

    @Test
    void debeExistirCapaServiceParaTestingUnitario() {
        assertDoesNotThrow(() -> Class.forName("com.foodfast.autenticacion_servicio.service.AuthService"));
    }
}
