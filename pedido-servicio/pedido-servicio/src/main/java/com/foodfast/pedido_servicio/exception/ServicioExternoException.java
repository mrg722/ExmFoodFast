package com.foodfast.pedido_servicio.exception;

public class ServicioExternoException extends RuntimeException {
    public ServicioExternoException(String mensaje) {
        super(mensaje);
    }
}
