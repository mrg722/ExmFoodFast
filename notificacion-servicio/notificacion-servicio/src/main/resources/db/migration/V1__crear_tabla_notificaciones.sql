CREATE TABLE notificaciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    canal VARCHAR(50) NOT NULL,
    estado VARCHAR(50) NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    mensaje VARCHAR(500) NOT NULL,
    referencia_tipo VARCHAR(80),
    referencia_id BIGINT,
    fecha_creacion DATETIME NOT NULL,
    fecha_envio DATETIME,
    fecha_lectura DATETIME,
    error_envio VARCHAR(255)
);
