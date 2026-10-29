@echo off
chcp 65001 >nul
cd /d "%~dp0.."
call mvn clean test package
if errorlevel 1 (echo ERROR en pruebas o compilacion. & pause & exit /b 1)
echo Proyecto compilado. Archivo JAR: target\juans-pos-1.0.0.jar
pause
