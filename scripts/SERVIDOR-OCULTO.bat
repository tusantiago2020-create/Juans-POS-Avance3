@echo off
setlocal
cd /d "%~dp0.."
if not exist "logs" mkdir "logs"
echo.>> "logs\juans-pos.log"
echo ==== INICIO %date% %time% ====>> "logs\juans-pos.log"
where java >nul 2>&1
if errorlevel 1 (
  echo ERROR: Java no esta en PATH.>> "logs\juans-pos.log"
  exit /b 1
)
where mvn >nul 2>&1
if errorlevel 1 (
  echo ERROR: Maven no esta en PATH.>> "logs\juans-pos.log"
  exit /b 1
)
call mvn spring-boot:run >> "logs\juans-pos.log" 2>&1
echo ==== FIN %date% %time% ====>> "logs\juans-pos.log"
