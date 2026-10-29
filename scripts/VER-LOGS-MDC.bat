@echo off
cd /d "%~dp0.."
echo Mostrando ultimas 50 lineas del registro Juan'S POS...
echo Para salir pulsa Ctrl+C.
powershell -NoProfile -Command "Get-Content -Path 'logs\juans-pos.log' -Tail 50 -Wait"
pause
