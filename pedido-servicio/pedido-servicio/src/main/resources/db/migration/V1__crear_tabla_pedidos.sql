CREATE TABLE IF NOT EXISTS pedidos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    producto_id BIGINT NULL,
    cantidad INT NULL,
    fecha_creacion DATETIME NOT NULL,
    estado VARCHAR(30) NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    direccion_entrega VARCHAR(150) NOT NULL,
    observacion VARCHAR(255) NULL
);
