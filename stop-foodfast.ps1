$ErrorActionPreference = "Continue"
Write-Host "Apagando FoodFast sin borrar bases..." -ForegroundColor Yellow
docker compose down --remove-orphans
docker compose ps
