Set-Location $PSScriptRoot

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "   INICIANDO FASTER 360 - ARQUITECTURA CLOUD & RABBITMQ" -ForegroundColor Yellow
Write-Host "========================================================" -ForegroundColor Cyan

Write-Host "`n1. Levantando Docker (PostgreSQL, RabbitMQ cluster, Kafka, Kafka-UI)..." -ForegroundColor Green
docker compose up -d
docker compose -f docker-compose.rabbitmq-cluster.yml up -d
docker compose -f docker-compose.kafka.yml up -d
docker compose -f docker-compose.kafka-ui.yml up -d
Write-Host "   Esperando 40s a que los brokers Kafka esten listos..." -ForegroundColor DarkGray
Start-Sleep -Seconds 40

Write-Host "`n1b. Iniciando Kafka Admin (8084) - crea los topicos del sistema..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$PSScriptRoot\kafka-admin-service'; .\mvnw.cmd spring-boot:run"
Start-Sleep -Seconds 20

Write-Host "`n2. Iniciando Microservicio OrderService (8081)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$PSScriptRoot\orderservice'; .\mvnw.cmd spring-boot:run"

Write-Host "`n3. Iniciando Microservicio Notification-Ticket (8082)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$PSScriptRoot\notification-ticket-service'; .\mvnw.cmd spring-boot:run"

Write-Host "`n4. Iniciando Microservicio RabbitAdmin (8083)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$PSScriptRoot\rabbit-admin-service'; .\mvnw.cmd spring-boot:run"

Write-Host "`n5. Iniciando Frontend Angular (4200)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$PSScriptRoot\faster360-frontend'; npm start"

Write-Host "`n========================================================" -ForegroundColor Cyan
Write-Host "   ¡Todos los servicios iniciados con exito!" -ForegroundColor Yellow
Write-Host "   - Frontend: http://localhost:4200" -ForegroundColor White
Write-Host "   - RabbitMQ Management: http://localhost:15672 (guest/guest)" -ForegroundColor White
Write-Host "   - Kafka-UI: http://localhost:8090" -ForegroundColor White
Write-Host "   - Kafka Admin API: http://localhost:8084/api/kafka/topics" -ForegroundColor White
Write-Host "========================================================`n" -ForegroundColor Cyan
# 