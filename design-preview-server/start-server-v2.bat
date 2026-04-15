@echo off
setlocal enabledelayedexpansion

REM ARES Design Preview Server Launcher - Windows
REM Version 2: Alternativa si el .bat no funciona

title ARES Design Preview Server

cd /d "%~dp0"

echo.
echo ================================================
echo     ARES Design Preview Server
echo     Iniciando...
echo ================================================
echo.

REM Verificar si Node.js esta instalado
node --version >nul 2>&1
if errorlevel 1 (
    echo [PASO 1] Descargar Node.js
    echo.
    echo Abre este link en tu navegador:
    echo https://nodejs.org
    echo.
    echo [PASO 2] Descarga la version LTS (recomendado)
    echo.
    echo [PASO 3] Durante la instalacion:
    echo   - IMPORTANTE: Marca "Add to PATH"
    echo   - Click Next hasta terminar
    echo.
    echo [PASO 4] REINICIA LA COMPUTADORA
    echo.
    echo [PASO 5] Corre este script de nuevo
    echo.
    pause
    exit /b 1
)

echo [OK] Node.js detectado
echo.

REM Verificar si npm esta disponible
npm --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] npm no funciona
    echo Reinstala Node.js desde: https://nodejs.org
    pause
    exit /b 1
)

echo [OK] npm encontrado
echo.

REM Verificar si node_modules existe
if not exist "node_modules" (
    echo [PASO] Instalando dependencias...
    echo Esto puede tomar 1-2 minutos...
    echo.
    call npm install
    if errorlevel 1 (
        echo.
        echo [ERROR] Fallo la instalacion
        echo Intenta:
        echo 1. Borra la carpeta: node_modules
        echo 2. Borra el archivo: package-lock.json
        echo 3. Corre este script de nuevo
        echo.
        pause
        exit /b 1
    )
    echo.
)

echo ================================================
echo [OK] Servidor iniciando...
echo ================================================
echo.
echo [URL] http://localhost:3000
echo.
echo Para detener: Press Ctrl+C
echo.

call npm start

if errorlevel 1 (
    echo.
    echo [ERROR] Servidor no pudo iniciar
    pause
    exit /b 1
)

pause
