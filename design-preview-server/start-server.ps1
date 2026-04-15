# ARES Design Preview Server - PowerShell Version
# Ejecuta: powershell -ExecutionPolicy Bypass -File start-server.ps1

$ErrorActionPreference = "Continue"

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "    ARES Design Preview Server" -ForegroundColor Red
Write-Host "    Iniciando..." -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Cambiar al directorio del script
Set-Location (Split-Path -Parent $MyInvocation.MyCommandPath)

# Verificar si Node.js esta instalado
try {
    $nodeVersion = & node --version 2>$null
    Write-Host "[OK] Node.js detectado: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Node.js no esta instalado" -ForegroundColor Red
    Write-Host ""
    Write-Host "PASOS PARA INSTALAR:" -ForegroundColor Yellow
    Write-Host "1. Abre: https://nodejs.org" -ForegroundColor White
    Write-Host "2. Descarga LTS (recomendado)" -ForegroundColor White
    Write-Host "3. Durante instalacion:" -ForegroundColor White
    Write-Host "   - Marca 'Add to PATH' (MUY IMPORTANTE)" -ForegroundColor Yellow
    Write-Host "   - Completa la instalacion" -ForegroundColor White
    Write-Host "4. REINICIA LA COMPUTADORA" -ForegroundColor Red
    Write-Host "5. Corre este script de nuevo" -ForegroundColor White
    Write-Host ""
    Read-Host "Press Enter para cerrar"
    exit 1
}

# Verificar npm
try {
    $npmVersion = & npm --version 2>$null
    Write-Host "[OK] npm detectado: $npmVersion" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] npm no funciona" -ForegroundColor Red
    Read-Host "Press Enter para cerrar"
    exit 1
}

Write-Host ""

# Verificar si node_modules existe
if (-not (Test-Path "node_modules")) {
    Write-Host "[PASO] Instalando dependencias..." -ForegroundColor Cyan
    Write-Host "Esto puede tomar 1-2 minutos..." -ForegroundColor Gray
    Write-Host ""
    
    & npm install
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "[ERROR] Fallo la instalacion" -ForegroundColor Red
        Write-Host ""
        Write-Host "Intenta:" -ForegroundColor Yellow
        Write-Host "1. Borra la carpeta: node_modules" -ForegroundColor White
        Write-Host "2. Borra el archivo: package-lock.json" -ForegroundColor White
        Write-Host "3. Corre este script de nuevo" -ForegroundColor White
        Write-Host ""
        Read-Host "Press Enter para cerrar"
        exit 1
    }
    Write-Host ""
}

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "[OK] Servidor iniciando..." -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "[URL] http://localhost:3000" -ForegroundColor Yellow
Write-Host ""
Write-Host "Para detener: Press Ctrl+C" -ForegroundColor Gray
Write-Host ""

& npm start

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "[ERROR] Servidor no pudo iniciar" -ForegroundColor Red
    Read-Host "Press Enter para cerrar"
    exit 1
}

Read-Host "Press Enter para cerrar"
