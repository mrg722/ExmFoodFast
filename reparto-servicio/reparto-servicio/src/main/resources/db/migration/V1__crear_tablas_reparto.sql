CREATE TABLE repartidores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    telefono VARCHAR(30) NOT NULL,
    vehiculo VARCHAR(50) NOT NULL,
    activo BOOLEAN NOT NULL,
    disponible BOOLEAN NOT NULL
);

CREATE TABLE entregas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    repartidor_id BIGINT NULL,
    direccion_entrega VARCHAR(180) NOT NULL,
    estado_entrega VARCHAR(30) NOT NULL,
    fecha_creacion DATETIME NOT NULL,
    fecha_actualizacion DATETIME NULL,
    CONSTRAINT uk_entregas_pedido UNIQUE (pedido_id),
    CONSTRAINT fk_entregas_repartidor
        FOREIGN KEY (repartidor_id)
        REFERENCES repartidores(id)
        ON DELETE SET NULL
);
