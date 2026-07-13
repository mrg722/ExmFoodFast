CREATE TABLE resenas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    restaurante_id BIGINT,
    calificacion INT NOT NULL,
    comentario VARCHAR(500) NOT NULL,
    activa BOOLEAN NOT NULL,
    fecha_creacion DATETIME NOT NULL,
    fecha_actualizacion DATETIME,
    CONSTRAINT uk_resena_cliente_producto UNIQUE (cliente_id, producto_id),
    CONSTRAINT chk_resenas_calificacion CHECK (calificacion BETWEEN 1 AND 5)
);
