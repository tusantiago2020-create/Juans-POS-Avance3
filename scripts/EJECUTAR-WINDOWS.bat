@echo off
chcp 65001 >nul
cd /d "%~dp0.."
echo ====================================
echo  JUAN'S POS - AVANCE 3
 echo ====================================
where java >nul 2>nul
if errorlevel 1 (echo ERROR: Falta Java JDK 17 o superior. & pause & exit /b 1)
where mvn >nul 2>nul
if errorlevel 1 (echo ERROR: Falta Maven 3.9+. Instala Maven y agregalo al PATH. & pause & exit /b 1)
echo Se iniciara el sistema en http://localhost:8080
echo Modo demo H2: no necesitas MySQL.
echo Usa admin / Admin123! para el entorno local.
call mvn spring-boot:run
pause
