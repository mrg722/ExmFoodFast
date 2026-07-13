# CONTINGENCIA FOODFAST EFT

Archivo para dejar en la raíz del proyecto FoodFast.

---

## 1. Links oficiales de instalación

- JDK 21 Temurin: https://adoptium.net/temurin/releases/
- Apache Maven: https://maven.apache.org/download.cgi
- Docker Desktop Windows: https://docs.docker.com/desktop/setup/install/windows-install/
- Docker Desktop producto: https://www.docker.com/products/docker-desktop/

---
Si Maven muestra 25, fuerza Java 21 en esa ventana:

$jdk21 = Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory |
    Where-Object { $_.Name -like "jdk-21*" } |
    Select-Object -First 1

$env:JAVA_HOME = $jdk21.FullName
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

java -version
.\mvnw.cmd -v

Haz esto ahora
Opción 1: por comando

Ejecuta:

docker desktop engine ls

Después:

docker desktop engine use linux

Espera a que Docker Desktop reinicie completamente. Luego comprueba:

docker context ls
docker info --format "Sistema de contenedores: {{.OSType}}"

Debe aparecer:

desktop-linux *
Sistema de contenedores: linux

Recién entonces ejecuta:

docker compose down --remove-orphans
docker compose up --build -d
docker compose ps

## 2. Verificar entorno del PC 

```powershell
java -version
javac -version
$env:JAVA_HOME
.
\mvnw.cmd -v
docker --version
docker compose version
wsl -l -v
```

> Java esperado: 21. Maven recomendado: usar Maven Wrapper del proyecto (`mvnw.cmd`).

---

## 3. Si Java no es 21

### Opción rápida con winget

```powershell
winget search Temurin
winget install EclipseAdoptium.Temurin.21.JDK --accept-source-agreements --accept-package-agreements
```

Cerrar y abrir PowerShell nuevamente. Luego validar:

```powershell
java -version
javac -version
```

### Ajustar JAVA_HOME si quedó mal

```powershell
$jdk = Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory |
Where-Object { $_.Name -like "jdk-21*" } |
Sort-Object LastWriteTime -Descending |
Select-Object -First 1

[Environment]::SetEnvironmentVariable("JAVA_HOME", $jdk.FullName, "User")
$env:JAVA_HOME = $jdk.FullName
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

java -version
javac -version
```

---

## 4. Si Maven falla

Usar siempre Maven Wrapper del proyecto, no Maven global:

```powershell
.
\mvnw.cmd -v
.
\mvnw.cmd -U clean verify
```

Si falla por descarga de dependencias:

```powershell
.
\mvnw.cmd -U dependency:go-offline
.
\mvnw.cmd -U clean verify
```

Si falla por archivos generados antiguos:

```powershell
Get-ChildItem -Path . -Directory -Recurse -Filter target | Remove-Item -Recurse -Force
.
\mvnw.cmd clean verify
```

---

## 5. Comandos Docker para levantar en el PC del profesor

Desde la raíz del proyecto:

```powershell
cd "C:\Ruta\Al\Proyecto\ExmFoodFastFN"
```

Validar Compose:

```powershell
docker compose config
docker compose config --services
```

Construir imágenes por primera vez en el PC del profesor:

```powershell
docker compose build
```

Levantar ecosistema:

```powershell
docker compose up -d
```

Verificar estado:

```powershell
docker compose ps
```

Ver logs:

```powershell
docker compose logs --tail=80 eureka-server api-gateway authentication-servicio inventario-servicio pedido-servicio
```

Detener sin borrar base ni imágenes:

```powershell
docker compose down
```

No usar salvo reset total de base de datos:

```powershell
docker compose down -v
```

---

## 6. Links que se deben mostrar en defensa

### Eureka

```text
http://localhost:8761
```

Mostrar servicios registrados.

### Gateway Health

```text
http://localhost:8080/actuator/health
```

Debe mostrar:

```json
{
  "status": "UP"
}
```

### Gateway Routes

```text
http://localhost:8080/actuator/gateway/routes
```

Buscar rutas hacia:

```text
lb://authentication-servicio
lb://inventario-servicio
lb://pedido-servicio
```

### Swagger Auth

```text
http://localhost:8090/swagger-ui/index.html
```

### Swagger Inventario

```text
http://localhost:8082/swagger-ui/index.html
```

### Swagger Pedido

```text
http://localhost:8083/swagger-ui/index.html
```

### Prueba por Gateway

```text
http://localhost:8080/api/inventarios
```

---

## 7. Swagger Auth: Register

Endpoint:

```text
POST /api/auth/register
```

Body:

```json
{
  "nombre": "Martin Reyes",
  "email": "martin.defensa01@foodfast.cl",
  "password": "123456",
  "rol": "ADMIN"
}
```

Si el correo ya existe, cambiar a:

```text
martin.defensa02@foodfast.cl
martin.defensa03@foodfast.cl
martin.defensa04@foodfast.cl
```

Respuesta esperada:

```text
201 Created
```

---

## 8. Swagger Auth: Login

Endpoint:

```text
POST /api/auth/login
```

Body:

```json
{
  "email": "martin.defensa01@foodfast.cl",
  "password": "123456"
}
```

Respuesta esperada:

```text
200 OK
```

Copiar el token desde:

```text
data.accessToken
```

o desde el campo equivalente que muestre Swagger.

Copiar solo el valor que empieza con:

```text
eyJ...
```

---

## 9. Swagger Inventario: autorizar con token

Abrir Swagger Inventario:

```text
http://localhost:8082/swagger-ui/index.html
```

Primero probar sin token:

```text
GET /api/inventarios
```

Respuesta esperada:

```text
401 Unauthorized
```

Luego presionar:

```text
Authorize
```

Pegar solo el token:

```text
eyJ...
```

No escribir `Bearer` si Swagger ya lo agrega automáticamente.

Repetir:

```text
GET /api/inventarios
```

Respuesta esperada:

```text
200 OK
```

---

## 10. Swagger Inventario: crear inventario

Endpoint:

```text
POST /api/inventarios
```

Body:

```json
{
  "productoId": 91001,
  "cantidadDisponible": 10,
  "cantidadReservada": 0,
  "ubicacion": "Bodega defensa"
}
```

Si `productoId` ya existe, usar:

```text
91002
91003
91004
```

Respuesta esperada:

```text
201 Created
```

Guardar el `id` devuelto. Ese es el ID de inventario.

---

## 11. Swagger Inventario: consultar por ID

Endpoint:

```text
GET /api/inventarios/{id}
```

Usar el ID devuelto por el POST.

Respuesta esperada:

```text
200 OK
```

---

## 12. Swagger Inventario: validar error 400

Endpoint:

```text
POST /api/inventarios
```

Body inválido:

```json
{
  "productoId": 92001,
  "cantidadDisponible": -1,
  "cantidadReservada": 0,
  "ubicacion": "Bodega defensa"
}
```

Respuesta esperada:

```text
400 Bad Request
```

---

## 13. Swagger Inventario: consultar inexistente 404

Endpoint:

```text
GET /api/inventarios/999999
```

Respuesta esperada:

```text
404 Not Found
```

---

## 14. Swagger Inventario: stock inicial

Endpoint:

```text
GET /api/inventarios/producto/91001/stock
```

Respuesta esperada:

```text
200 OK
```

Stock esperado si se creó con 10:

```text
10
```

---

## 15. Swagger Inventario: descontar stock válido

Endpoint:

```text
PUT /api/inventarios/descontar-stock
```

Body:

```json
{
  "productoId": 91001,
  "cantidad": 2
}
```

Respuesta esperada:

```text
200 OK
```

Luego consultar stock otra vez:

```text
GET /api/inventarios/producto/91001/stock
```

Resultado esperado:

```text
8
```

---

## 16. Swagger Inventario: stock insuficiente

Endpoint:

```text
PUT /api/inventarios/descontar-stock
```

Body:

```json
{
  "productoId": 91001,
  "cantidad": 999
}
```

Respuesta esperada según implementación:

```text
400 Bad Request
```

o:

```text
409 Conflict
```

Luego confirmar que el stock no quedó negativo.

---

## 17. Swagger Inventario: eliminar inventario

Endpoint:

```text
DELETE /api/inventarios/{id}
```

Usar el ID de inventario creado.

Respuesta esperada:

```text
200 OK
```

o:

```text
204 No Content
```

Confirmar eliminación:

```text
GET /api/inventarios/{id}
```

Respuesta esperada:

```text
404 Not Found
```

---

## 18. Probar Inventario por Gateway

En Postman o navegador:

```text
GET http://localhost:8080/api/inventarios
```

Sin token:

```text
401 Unauthorized
```

Con Bearer Token:

```text
200 OK
```

Header manual si se usa Postman:

```text
Authorization: Bearer eyJ...
```

---

## 19. Comandos de tests por microservicio

Desde la raíz del proyecto:

### Auth

```powershell
Push-Location .\authentication-servicio\authentication-servicio
.\mvnw.cmd clean verify
Pop-Location
```

### Catálogo

```powershell
Push-Location .\catalogo-servicio\catalogo-servicio
.\mvnw.cmd clean verify
Pop-Location
```

### Cliente

```powershell
Push-Location .\cliente-servicio\cliente-servicio
.\mvnw.cmd clean verify
Pop-Location
```

### Inventario

```powershell
Push-Location .\inventario-servicio\inventario-servicio
.\mvnw.cmd clean verify
Pop-Location
```

### Notificación

```powershell
Push-Location .\notificacion-servicio\notificacion-servicio
.\mvnw.cmd clean verify
Pop-Location
```

### Pago

```powershell
Push-Location .\pago-servicio\pago-servicio
.\mvnw.cmd clean verify
Pop-Location
```

### Pedido

```powershell
Push-Location .\pedido-servicio\pedido-servicio
.\mvnw.cmd clean verify
Pop-Location
```

### Reparto

```powershell
Push-Location .\reparto-servicio\reparto-servicio
.\mvnw.cmd clean verify
Pop-Location
```

### Reseña

```powershell
Push-Location .\resena-servicio\resena-servicio
.\mvnw.cmd clean verify
Pop-Location
```

### Restaurante

```powershell
Push-Location .\restaurante-servicio\restaurante-servicio
.\mvnw.cmd clean verify
Pop-Location
```

### API Gateway

```powershell
Push-Location .\api-gateway\api-gateway
.\mvnw.cmd clean verify
Pop-Location
```

### Eureka Server

```powershell
Push-Location .\eureka-server\eureka-server
.\mvnw.cmd clean verify
Pop-Location
```

---

## 20. Ejecutar todos los tests secuencialmente

Desde la raíz del proyecto:

```powershell
$services = @(
  "authentication-servicio",
  "catalogo-servicio",
  "cliente-servicio",
  "inventario-servicio",
  "notificacion-servicio",
  "pago-servicio",
  "pedido-servicio",
  "reparto-servicio",
  "resena-servicio",
  "restaurante-servicio",
  "api-gateway",
  "eureka-server"
)

foreach ($s in $services) {
  Write-Host "=== Ejecutando tests de $s ===" -ForegroundColor Cyan
  Push-Location ".\$s\$s"
  .\mvnw.cmd clean verify
  if ($LASTEXITCODE -ne 0) {
    Pop-Location
    throw "Fallaron los tests de $s"
  }
  Pop-Location
}
```

---

## 21. Rutas JaCoCo

Inventario:

```text
inventario-servicio\inventario-servicio\target\site\jacoco\index.html
```

Pedido:

```text
pedido-servicio\pedido-servicio\target\site\jacoco\index.html
```

Abrir desde PowerShell:

```powershell
Start-Process (Resolve-Path ".\inventario-servicio\inventario-servicio\target\site\jacoco\index.html")
```

---

## 22. Comandos útiles de diagnóstico

### Ver uso Docker

```powershell
docker system df
```

### Ver contenedores

```powershell
docker ps
```

### Ver imágenes

```powershell
docker images
```

### Ver logs en vivo

```powershell
docker compose logs -f inventario-servicio
```

### Ver puerto ocupado

```powershell
netstat -ano | findstr :8080
netstat -ano | findstr :8082
netstat -ano | findstr :8090
netstat -ano | findstr :8761
```

### Matar proceso por PID si el profesor lo autoriza

```powershell
taskkill /PID NUMERO_PID /F
```

---

## 23. Respuesta corta si algo falla

### Si Docker tarda

```text
El primer levantamiento en este equipo puede tardar porque Docker está creando imágenes y descargando dependencias. Verifico avance con docker compose ps y docker compose logs.
```

### Si Maven falla por Java

```text
El proyecto está preparado para Java 21. Validaré java -version y JAVA_HOME, y corregiré la instalación si el JDK del equipo no coincide.
```

### Si Swagger no abre aún

```text
El servicio puede estar iniciado pero aún cargando Spring Boot. Verifico estado con docker compose ps, logs y actuator health.
```

### Si el token falla

```text
Genero nuevamente el token desde Auth login y lo pego en Authorize sin modificarlo. Debe empezar con eyJ.
```

---

## 24. Orden rápido de defensa técnica

1. Eureka: `http://localhost:8761`
2. Gateway Health: `http://localhost:8080/actuator/health`
3. Gateway Routes: `http://localhost:8080/actuator/gateway/routes`
4. Swagger Auth: register/login/token
5. Swagger Inventario: 401 sin token
6. Swagger Inventario: Authorize con JWT
7. Swagger Inventario: 200 con token
8. Crear inventario: 201
9. Consultar inventario: 200
10. Validación inválida: 400
11. Inexistente: 404
12. Stock válido: 10 → 8
13. Stock insuficiente: error controlado
14. Gateway por `http://localhost:8080/api/inventarios`
15. Código CSR/JPA
16. Test + JaCoCo
17. Logs
18. GitHub/Trello
19. Modificación en vivo
