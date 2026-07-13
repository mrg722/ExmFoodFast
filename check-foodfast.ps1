Write-Host "=== Diagnostico FoodFast ===" -ForegroundColor Cyan
Write-Host "Ubicacion:"; pwd
Write-Host "Docker:"; docker --version; docker compose version; docker info
Write-Host "Compose config:"; docker compose config
Write-Host "Estado:"; docker compose ps
Write-Host "Eureka/Gateway:"
try { Invoke-RestMethod http://localhost:8080/actuator/health } catch { Write-Host $_.Exception.Message -ForegroundColor Red }
