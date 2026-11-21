@echo off
setlocal
cd /d "%~dp0"

REM Esegue build-apk.ps1 passando tutti i parametri forniti da riga di comando
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-apk.ps1" %*

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERRORE] La build dell'APK non e' andata a buon fine.
    exit /b %ERRORLEVEL%
)

exit /b 0
