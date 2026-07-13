$ErrorActionPreference = "Stop"
$env:COMPOSE_PARALLEL_LIMIT = "1"

Write-Host "=== FoodFast EV3 - inicio liviano ===" -ForegroundColor Cyan
Write-Host "Validando docker-compose.yml..."
docker compose config

Write-Host "Construyendo imagenes de forma secuencial. La primera vez puede tardar." -ForegroundColor Yellow
docker compose build --progress=plain

Write-Host "Levantando MySQL unico + Eureka..."
docker compose up -d mysql-foodfast eureka-server
Start-Sleep -Seconds 25

Write-Host "Levantando Auth + Gateway..."
docker compose up -d authentication-servicio api-gateway
Start-Sleep -Seconds 20

Write-Host "Levantando servicios principales..."
docker compose up -d catalogo-servicio inventario-servicio pedido-servicio
Start-Sleep -Seconds 20

Write-Host "Levantando servicios restantes..."
docker compose up -d cliente-servicio pago-servicio reparto-servicio resena-servicio restaurante-servicio notificacion-servicio
Start-Sleep -Seconds 10

Write-Host "Estado final:" -ForegroundColor Cyan
docker compose ps

Write-Host "URLs principales:" -ForegroundColor Green
Write-Host "Eureka:  http://localhost:8761"
Write-Host "Gateway: http://localhost:8080/actuator/health"
Write-Host "Routes:  http://localhost:8080/actuator/gateway/routes"
Write-Host "Swagger Auth: http://localhost:8090/swagger-ui/index.html"
Write-Host "Swagger Inventario: http://localhost:8082/swagger-ui/index.html"
Write-Host "Swagger Pedido: http://localhost:8083/swagger-ui/index.html"
