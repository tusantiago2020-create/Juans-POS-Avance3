@echo off
chcp 65001 >nul
cd /d "%~dp0.."
echo Agrega tu repositorio GitHub al ejecutarlo:
echo     git remote add origin https://github.com/TU_USUARIO/juans-pos.git
echo     git branch -M main
echo     git push -u origin main
echo IMPORTANTE: GitHub requiere autenticarte desde tu propia cuenta.
pause
