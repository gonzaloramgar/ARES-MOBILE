# ARES Mobile

Aplicacion Android de ARES enfocada en asistencia local con IA, herramientas del dispositivo y flujo de chat moderno en Jetpack Compose.

## Que incluye esta version

- Chat principal con:
  - envio por teclado
  - soporte voz (STT)
  - captura de camara
  - quick actions
- Motor de agente con:
  - ejecucion de herramientas
  - historial de conversacion
  - respuestas por iteraciones
- Pantallas principales:
  - Chat
  - Memoria
  - Tareas
  - Configuracion
- Persistencia local con Room para:
  - memoria
  - tareas programadas
  - historial
- Soporte de modelo local (Gemma variantes E2B/E4B) con:
  - verificacion/instalacion
  - estado de descarga
  - eliminacion local
- Integraciones de herramientas:
  - portapapeles
  - camara
  - ubicacion
  - voz
  - alarmas

## Stack tecnico

- Kotlin
- Jetpack Compose + Material 3
- Navigation Compose
- Hilt / DI en app container + viewmodel factory
- Room
- DataStore
- Coroutines + StateFlow

## Estructura clave

- app/src/main/java/com/ares/mobile/ui/navigation: navegacion principal y bottom bar
- app/src/main/java/com/ares/mobile/ui/screens: pantallas de producto
- app/src/main/java/com/ares/mobile/ui/components: componentes reutilizables
- app/src/main/java/com/ares/mobile/agent: loop del agente, registry y contratos de tools
- app/src/main/java/com/ares/mobile/ai: cliente de modelo, router y estados de instalacion
- app/src/main/java/com/ares/mobile/tools: tools del dispositivo
- app/src/main/java/com/ares/mobile/viewmodel: estado de UI

## Pantallas

### Chat

- Header de estado del modelo
- Lista de mensajes con typing indicator
- Barra de acciones rapidas
- Composer con camara, microfono y envio

### Memoria

- Alta de clave/valor
- Listado de recuerdos guardados
- Eliminacion individual

### Tareas

- Listado de tareas programadas
- Metadata de fecha/hora
- Eliminacion individual

### Configuracion

- Preferencia de modelo (E2B/E4B)
- Thinking mode
- Instalacion/reinstalacion del modelo local
- Clave de Gemini (modo online)
- Token de Hugging Face
- Reapertura de onboarding

## Requisitos

- Android Studio (recomendado)
- JDK 17 o JDK 21
- SDK Android segun configuracion del modulo

Nota: con JDK 25 puedes encontrar fallos de toolchain en AGP/Gradle.

## Como correr

Desde ARES-mobile:

```powershell
# Windows
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

```bash
# macOS/Linux
./gradlew assembleDebug
./gradlew installDebug
```

## Testing

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
```

Para una guia paso a paso de pruebas funcionales:
- revisar SETUP.md
- revisar TESTING.md

## Estado funcional esperado

- La app puede arrancar y navegar entre las 4 tabs sin crash.
- Sin modelo instalado, el flujo de chat debe mantenerse util con mensajes de ayuda.
- Con permisos aceptados, voz/camara/ubicacion deben responder.

## Servidor de diseno (opcional)

En este repositorio existe un servidor local auxiliar para iterar UI:
- carpeta: ../design-preview-server
- objetivo: probar variantes visuales antes de aplicarlas en Compose

No es requerido para compilar ni ejecutar ARES-mobile.

## Notas

- Este README describe la version movil actual y su alcance.
- Los artefactos locales/build se excluyen via .gitignore de raiz.
