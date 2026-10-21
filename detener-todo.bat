@echo off
cd /d "%~dp0"
title Faster 360 - Detener Todo

echo ========================================================
echo   DETENIENDO FASTER 360 - TODOS LOS SERVICIOS
echo ========================================================

echo 1. Deteniendo contenedores Docker (RabbitMQ y PostgreSQL)...
docker compose down

echo 2. Deteniendo Microservicios Java (Spring Boot)...
taskkill /F /IM java.exe 2>nul

echo 3. Deteniendo Frontend Angular (Node)...
taskkill /F /IM node.exe 2>nul

echo.
echo ========================================================
echo   Todos los servicios han sido detenidos correctamente.
echo ========================================================
echo.
pause
