$ErrorActionPreference = "Continue"
$services = @(
  "authentication-servicio/authentication-servicio",
  "catalogo-servicio/catalogo-servicio",
  "cliente-servicio/cliente-servicio",
  "inventario-servicio/inventario-servicio",
  "notificacion-servicio/notificacion-servicio",
  "pago-servicio/pago-servicio",
  "pedido-servicio/pedido-servicio",
  "reparto-servicio/reparto-servicio",
  "resena-servicio/resena-servicio",
  "restaurante-servicio/restaurante-servicio"
)

foreach ($service in $services) {
  Write-Host "========================================" -ForegroundColor Cyan
  Write-Host "Tests: $service" -ForegroundColor Cyan
  Write-Host "========================================" -ForegroundColor Cyan
  Push-Location $service
  if (Test-Path ".\mvnw.cmd") {
    .\mvnw.cmd clean test
  } else {
    mvn clean test
  }
  Pop-Location
}
Write-Host "Revisar target/site/jacoco/index.html en servicios principales." -ForegroundColor Green
