@echo off
cd /d "%~dp0"
title Faster 360 - Launcher

echo ========================================================
echo   INICIANDO FASTER 360 - ARQUITECTURA CLOUD ^& RABBITMQ
echo ========================================================

echo 1. Levantando Contenedores Docker (RabbitMQ + PostgreSQL)...
docker compose up -d

echo 2. Iniciando Microservicio OrderService (Puerto 8081)...
start "OrderService (8081)" cmd /k "cd /d ""%~dp0orderservice"" && mvnw.cmd spring-boot:run"

echo 3. Iniciando Microservicio Notification-Ticket (Puerto 8082)...
start "NotificationTicket (8082)" cmd /k "cd /d ""%~dp0notification-ticket-service"" && mvnw.cmd spring-boot:run"

echo 4. Iniciando Microservicio RabbitAdmin (Puerto 8083)...
start "RabbitAdmin (8083)" cmd /k "cd /d ""%~dp0rabbit-admin-service"" && mvnw.cmd spring-boot:run"

echo 5. Iniciando Frontend Angular (Puerto 4200)...
start "Faster360 Frontend (4200)" cmd /k "cd /d ""%~dp0faster360-frontend"" && npm start"

echo.
echo ========================================================
echo   Todos los servicios han sido lanzados en sus terminales.
echo   - Frontend: http://localhost:4200
echo   - RabbitMQ Console: http://localhost:15672 (guest/guest)
echo ========================================================
echo.
pause
