CREATE TABLE clientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    telefono VARCHAR(30) NOT NULL,
    activo BOOLEAN NOT NULL
);

CREATE TABLE direcciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    calle VARCHAR(150) NOT NULL,
    numero VARCHAR(30) NOT NULL,
    comuna VARCHAR(100) NOT NULL,
    ciudad VARCHAR(100) NOT NULL,
    referencia VARCHAR(255),
    principal BOOLEAN NOT NULL,
    CONSTRAINT fk_direccion_cliente
        FOREIGN KEY (cliente_id) REFERENCES clientes(id)
        ON DELETE CASCADE
);
