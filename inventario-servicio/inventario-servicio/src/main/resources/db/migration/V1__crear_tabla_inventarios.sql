CREATE TABLE IF NOT EXISTS inventarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id BIGINT NOT NULL UNIQUE,
    cantidad_disponible INT NOT NULL,
    cantidad_reservada INT NOT NULL DEFAULT 0,
    ubicacion VARCHAR(120) NOT NULL,
    CONSTRAINT chk_inventarios_disponible_no_negativo CHECK (cantidad_disponible >= 0),
    CONSTRAINT chk_inventarios_reservada_no_negativo CHECK (cantidad_reservada >= 0),
    CONSTRAINT chk_inventarios_reservada_menor_igual_disponible CHECK (cantidad_reservada <= cantidad_disponible)
);
