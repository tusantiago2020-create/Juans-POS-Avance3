@echo off
setlocal
echo Cerrando Juan'S POS (solo si el proceso en el puerto 8080 es este proyecto)...
powershell -NoProfile -Command "$c=Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1; if ($null -eq $c) { Write-Host 'No hay servidor en el puerto 8080'; exit 0 }; $proc=Get-CimInstance Win32_Process -Filter ('ProcessId=' + $c.OwningProcess); if ($null -ne $proc -and $proc.CommandLine -match 'pe[.]juans[.]pos[.]JuansApplication|juans-pos-1[.]0[.]0[.]jar') { Stop-Process -Id $c.OwningProcess -Force; Write-Host 'JuanS POS detenido.' } else { Write-Host 'El puerto 8080 pertenece a otra aplicacion. No se detuvo nada.' }"
pause
