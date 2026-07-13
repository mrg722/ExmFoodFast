$ErrorActionPreference = "Continue"
Write-Host "RESET FoodFast: borra contenedores y volumen MySQL del proyecto." -ForegroundColor Red
docker compose down -v --remove-orphans
docker builder prune -f
Write-Host "Listo. Si quieres limpieza total de Docker, usar manualmente: docker system prune -af --volumes" -ForegroundColor Yellow
