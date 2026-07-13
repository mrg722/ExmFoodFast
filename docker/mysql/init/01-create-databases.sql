-- FoodFast EV3 - base de datos unica para modo liviano
-- Mantiene 10 bases separadas, una por microservicio.
CREATE DATABASE IF NOT EXISTS authentication_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS catalogo_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS cliente_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS inventario_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS notificacion_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS pago_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS pedido_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS reparto_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS resena_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS restaurante_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'appuser'@'%' IDENTIFIED BY '1234';
GRANT ALL PRIVILEGES ON authentication_db.* TO 'appuser'@'%';
GRANT ALL PRIVILEGES ON catalogo_db.* TO 'appuser'@'%';
GRANT ALL PRIVILEGES ON cliente_db.* TO 'appuser'@'%';
GRANT ALL PRIVILEGES ON inventario_db.* TO 'appuser'@'%';
GRANT ALL PRIVILEGES ON notificacion_db.* TO 'appuser'@'%';
GRANT ALL PRIVILEGES ON pago_db.* TO 'appuser'@'%';
GRANT ALL PRIVILEGES ON pedido_db.* TO 'appuser'@'%';
GRANT ALL PRIVILEGES ON reparto_db.* TO 'appuser'@'%';
GRANT ALL PRIVILEGES ON resena_db.* TO 'appuser'@'%';
GRANT ALL PRIVILEGES ON restaurante_db.* TO 'appuser'@'%';
FLUSH PRIVILEGES;
