CREATE TABLE pagos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    metodo_pago VARCHAR(30) NOT NULL,
    estado_pago VARCHAR(30) NOT NULL,
    codigo_transaccion VARCHAR(120),
    fecha_creacion DATETIME NOT NULL,
    fecha_procesamiento DATETIME
);