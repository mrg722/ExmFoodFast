package com.foodfast.autenticacion_servicio.config;

public final class RequestHeaderContext {
    private static final ThreadLocal<String> AUTH = new ThreadLocal<>();
    private RequestHeaderContext() {}
    public static void setAuthorization(String authorization) { AUTH.set(authorization); }
    public static String getAuthorization() { return AUTH.get(); }
    public static void clear() { AUTH.remove(); }
}
