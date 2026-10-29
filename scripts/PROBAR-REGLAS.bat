@echo off
chcp 65001 >nul
cd /d "%~dp0.."
if not exist build mkdir build
javac -encoding UTF-8 -d build src\main\java\pe\juans\pos\service\ReglasNegocio.java pruebas\PruebaReglas.java
if errorlevel 1 (echo ERROR: Instala JDK 17 o superior. & pause & exit /b 1)
java -cp build PruebaReglas
pause
