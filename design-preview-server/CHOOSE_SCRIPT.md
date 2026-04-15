# ¿Qué script debo usar?

Si el `.bat` no funciona, tienes alternativas:

## Opción 1: Doble click en `start-server.bat` ✓ (Recomendado)
- Más simple
- Si falla, intenta Opción 2

## Opción 2: Doble click en `start-server-v2.bat` (Alternativa)
- Versión simplificada del .bat
- Mejor debugging de errores
- Probablemente funcione si Opción 1 falla

## Opción 3: Script PowerShell (Avanzado)
1. Abre PowerShell como Administrador
2. Ve a la carpeta: `design-preview-server`
3. Ejecuta:
```powershell
powershell -ExecutionPolicy Bypass -File start-server.ps1
```

O más simple:
```powershell
.\start-server.ps1
```

## Opción 4: Terminal Manual
1. Abre PowerShell o CMD
2. Ve a la carpeta: `design-preview-server`
3. Ejecuta:
```bash
npm install
npm start
```

---

## Si NINGUNO funciona: Verificación

Abre PowerShell y verifica:
```powershell
node --version
npm --version
```

Si ves números de versión (ej: v20.10.0), Node.js está instalado.

Si ves error "no se reconoce", necesitas:
1. Instalar Node.js desde https://nodejs.org
2. Marcar "Add to PATH" durante instalación
3. REINICIAR la computadora
4. Volver a intentar

---

¿Cuál script probaste? ¿Qué error ves exactamente?
