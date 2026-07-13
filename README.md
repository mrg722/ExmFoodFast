# FoodFast EV3 - Examen Final FullStack I

Versión final liviana para defensa. El proyecto mantiene **12 servicios Spring Boot**:

- `eureka-server` en `8761`
- `api-gateway` en `8080`
- `authentication-servicio` en `8090`
- `catalogo-servicio` en `8081`
- `inventario-servicio` en `8082`
- `pedido-servicio` en `8083`
- `reparto-servicio` en `8084`
- `cliente-servicio` en `8085`
- `pago-servicio` en `8086`
- `restaurante-servicio` en `8087`
- `resena-servicio` en `8088`
- `notificacion-servicio` en `8089`

## Modo Docker liviano

Para que el PC no se congele, esta versión usa **1 solo contenedor MySQL** con **10 bases de datos separadas**:

- `authentication_db`
- `catalogo_db`
- `cliente_db`
- `inventario_db`
- `notificacion_db`
- `pago_db`
- `pedido_db`
- `reparto_db`
- `resena_db`
- `restaurante_db`

Esto permite levantar los **12 microservicios juntos** sin ejecutar 10 motores MySQL distintos. Cada microservicio conserva su base lógica propia y no se hacen joins entre bases.

## Requisitos

- Docker Desktop funcionando.
- WSL2 o Hyper-V habilitado.
- Puertos libres: `8080-8090`, `8761`, `3307`.
- Idealmente 12 GB de RAM o más.

## Levantar el sistema

Desde PowerShell en la raíz del proyecto:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\start-foodfast.ps1
```

No se recomienda usar `docker compose up --build` directo en PCs justos de memoria, porque construye y levanta todo de golpe.

## Comandos manuales alternativos

```powershell
$env:COMPOSE_PARALLEL_LIMIT="1"
docker compose config
docker compose build --progress=plain
docker compose up -d mysql-foodfast eureka-server
docker compose up -d authentication-servicio api-gateway
docker compose up -d catalogo-servicio inventario-servicio pedido-servicio
docker compose up -d cliente-servicio pago-servicio reparto-servicio resena-servicio restaurante-servicio notificacion-servicio
docker compose ps
```

## URLs de defensa

- Eureka: <http://localhost:8761>
- Gateway health: <http://localhost:8080/actuator/health>
- Gateway routes: <http://localhost:8080/actuator/gateway/routes>
- Swagger Auth: <http://localhost:8090/swagger-ui/index.html>
- Swagger Inventario: <http://localhost:8082/swagger-ui/index.html>
- Swagger Pedido: <http://localhost:8083/swagger-ui/index.html>

## Rutas principales por Gateway

- `/api/auth/**` → `authentication-servicio`
- `/api/productos/**`, `/api/categorias/**` → `catalogo-servicio`
- `/api/inventarios/**`, `/api/stock/**` → `inventario-servicio`
- `/api/pedidos/**`, `/api/v1/pedidos/**` → `pedido-servicio`
- `/api/entregas/**`, `/api/repartidores/**` → `reparto-servicio`
- `/api/clientes/**` → `cliente-servicio`
- `/api/pagos/**` → `pago-servicio`
- `/api/restaurantes/**`, `/api/horarios-restaurante/**` → `restaurante-servicio`
- `/api/resenas/**` → `resena-servicio`
- `/api/notificaciones/**` → `notificacion-servicio`

## Seguridad JWT

`authentication-servicio` genera el JWT. Los microservicios de negocio usan el mismo `JWT_SECRET` y `APP_SECURITY_ENABLED=true` para validar tokens.

Flujo para demostrar:

1. Login en Auth por Gateway.
2. Copiar `accessToken`.
3. Probar un endpoint protegido sin token: debe responder `401/403`.
4. Probar el mismo endpoint con `Authorization: Bearer TOKEN`: debe responder `200`.
5. Probar el flujo `Pedido -> Inventario` con token.

## Flujo estrella de defensa

Demostrar:

```text
Auth -> Gateway -> Pedido -> Inventario
```

Este flujo evidencia JWT, Gateway, Eureka, comunicación REST entre microservicios, regla de stock, persistencia y errores controlados.

## Tests y cobertura

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.un-tests.ps1
```

JaCoCo genera reportes en:

```text
<servicio>/target/site/jacoco/index.html
```

## Apagar

```powershell
.\stop-foodfast.ps1
```

## Reset completo del proyecto Docker

Borra contenedores y volumen MySQL del proyecto:

```powershell
.eset-foodfast.ps1
```

## Qué demuestra para la rúbrica

- 10 microservicios de negocio + Gateway + Eureka.
- API Gateway como entrada única.
- Eureka Service Discovery.
- JPA/Hibernate, Flyway y base lógica por microservicio.
- CSR: Controller, Service, Repository.
- DTOs, validaciones, `ResponseEntity`, excepciones y logs.
- Swagger/OpenAPI.
- JWT con Auth y servicios protegidos.
- Comunicación REST entre microservicios.
- Tests unitarios y JaCoCo.
- Docker Compose con los 12 servicios Spring Boot levantados juntos.
