@echo off
setlocal enabledelayedexpansion
REM ARES Design Preview Server Launcher - Windows
REM Este script instala dependencias e inicia el servidor

cd /d "%~dp0"

echo.
echo ================================================
echo     ARES Design Preview Server
echo     Iniciando...
echo ================================================
echo.

REM Verificar si Node.js está instalado
where node >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Node.js no esta instalado
    echo.
    echo Por favor:
    echo 1. Instala Node.js desde: https://nodejs.org (elige LTS)
    echo 2. Durante instalacion marca "Add to PATH"
    echo 3. Reinicia la computadora
    echo 4. Vuelve a correr este script
    echo.
    pause
    exit /b 1
)

echo [OK] Node.js encontrado: 
node --version
echo.

REM Verificar si node_modules existe
if not exist "node_modules" (
    echo [*] Instalando dependencias (primera vez)...
    echo [*] Esto puede tomar 1-2 minutos...
    echo.
    call npm install
    if errorlevel 1 (
        echo.
        echo [ERROR] Fallo la instalacion de dependencias
        echo Intenta:
        echo   1. npm install -g npm  (actualizar npm)
        echo   2. Borra la carpeta node_modules y package-lock.json
        echo   3. Corre este script de nuevo
        echo.
        pause
        exit /b 1
    )
)

echo.
echo ================================================
echo [OK] Iniciando servidor...
echo ================================================
echo.
echo [URL] http://localhost:3000
echo.
echo Para detener: Press Ctrl+C
echo.

call npm start

if errorlevel 1 (
    echo.
    echo [ERROR] El servidor no pudo iniciar
    pause
    exit /b 1
)
