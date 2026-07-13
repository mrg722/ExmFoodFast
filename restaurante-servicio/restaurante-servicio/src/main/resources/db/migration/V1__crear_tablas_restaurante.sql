CREATE TABLE restaurantes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(255),
    direccion VARCHAR(180) NOT NULL,
    telefono VARCHAR(30) NOT NULL,
    email VARCHAR(120),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    abierto BOOLEAN NOT NULL DEFAULT FALSE
);
 
CREATE TABLE horarios_restaurante (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    restaurante_id BIGINT NOT NULL,
    dia_semana VARCHAR(20) NOT NULL,
    hora_apertura TIME NOT NULL,
    hora_cierre TIME NOT NULL,
    CONSTRAINT fk_horario_restaurante
        FOREIGN KEY (restaurante_id) REFERENCES restaurantes(id)
);
