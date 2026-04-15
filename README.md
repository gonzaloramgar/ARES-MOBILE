# Proyecto ARES

Plataforma de asistente IA con dos clientes principales y un servidor local de previsualizacion de diseno.

## Estructura del repositorio

- [ARES-desktop](ARES-desktop)
  - Cliente de escritorio en C#/.NET (WPF)
  - Incluye escaneo de sistema, herramientas locales, memoria y configuracion persistente

- [ARES-mobile](ARES-mobile)
  - Cliente Android en Kotlin + Jetpack Compose
  - Chat, memoria, tareas programadas, configuracion, integracion de herramientas del dispositivo
  - Ver detalles en [ARES-mobile/README.md](ARES-mobile/README.md)

- [design-preview-server](design-preview-server)
  - Servidor local (Node.js + Express) para iterar propuestas visuales
  - Variantes de UI y configuracion por pestaña

## Estado actual

- Mobile: base funcional con navegacion, flujo de primer arranque, tools principales y tests base
- Desktop: estructura completa con modulos de agente, memoria y utilidades locales
- Design preview: panel web para elegir estilos por pantalla/pestaña

## Requisitos generales

- Git
- Node.js (solo para design-preview-server)
- Android Studio + JDK 17/21 (para ARES-mobile)
- Visual Studio 2022 o superior (para ARES-desktop)

## Como empezar rapido

### 1) Version movil (Android)

Ir a [ARES-mobile](ARES-mobile) y ejecutar:

```powershell
cd ARES-mobile
.\gradlew.bat assembleDebug
```

### 2) Servidor de diseno (localhost)

```powershell
cd design-preview-server
npm install
npm start
```

Abrir:
- http://localhost:3000
- http://localhost:3000/tabs-designer

### 3) Version desktop

Abrir `Ares.sln` en Visual Studio y compilar en Debug/Release.

## Documentacion relacionada

- Mobile setup: [ARES-mobile/SETUP.md](ARES-mobile/SETUP.md)
- Mobile testing: [ARES-mobile/TESTING.md](ARES-mobile/TESTING.md)
- Preview server: [design-preview-server/README.md](design-preview-server/README.md)

## Notas

- Los artefactos locales/build estan excluidos en [.gitignore](.gitignore).
- El servidor de diseno es auxiliar: no es requisito para compilar mobile/desktop.
