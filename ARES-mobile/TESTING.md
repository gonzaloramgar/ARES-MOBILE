# ARES Mobile - Guia de prueba

## Estado actual

ARES-mobile ya tiene:

- arranque con Compose + Hilt + Room
- navegacion principal
- First Run con permisos y descarga del modelo
- chat con texto, voz y captura rapida de camara
- tools de clipboard, location, alarm, voice y camera
- tests unitarios e instrumentados base

El bloqueo actual para probarlo en esta maquina no es el codigo: es el toolchain Java.

## Lo que necesitas para compilar

Necesitas una de estas opciones:

1. Android Studio con Gradle JDK en 17 o 21
2. Un JDK 17 o 21 instalado localmente y apuntado en `JAVA_HOME`

Con JDK 25 el build de Android falla al arrancar Gradle/AGP.

## Opcion recomendada

### Android Studio

1. Abre `ARES-mobile/` en Android Studio.
2. Ve a `File > Settings > Build, Execution, Deployment > Build Tools > Gradle`.
3. En `Gradle JDK`, selecciona una runtime compatible:
	`Embedded JDK`, `jbr-21`, `JDK 21` o `JDK 17`.
4. Deja que haga `Sync`.
5. Ejecuta `assembleDebug` o pulsa `Run` sobre la app.

### Si usas terminal

En Windows PowerShell:

```powershell
$env:JAVA_HOME = 'C:\ruta\a\jdk-21'
$env:Path = "$env:JAVA_HOME\\bin;$env:Path"
Set-Location 'c:\Users\grg30\Desktop\PROYECTOS\ProyectoARES\ARES-mobile'
.\gradlew.bat assembleDebug
```

## Flujo minimo para probar la app

### 1. Compilacion

Comando:

```powershell
.\gradlew.bat assembleDebug
```

Resultado esperado:

- APK debug generado sin errores

### 2. Ejecucion en emulador o movil

Puedes usar:

1. Emulador Android API 34 o 35
2. Telefono Android real con depuracion USB

Comando si usas terminal:

```powershell
.\gradlew.bat installDebug
```

Luego abre la app `ARES`.

## Smoke test recomendado

Haz esta secuencia exacta:

### First Run

1. Abrir la app.
2. Ver que aparece `FirstRunScreen`.
3. Pulsar `Solicitar permisos`.
4. Aceptar camara, microfono, ubicacion y notificaciones.
5. Pulsar `Descargar / verificar modelo`.

Si no quieres bajar el modelo todavia:

1. Pulsa `Continuar sin modelo`.
2. La app debe entrar igualmente al chat.

### Chat basico

En la pantalla de chat prueba:

1. Escribir `hola` y enviar.
2. Ver que aparece tu mensaje y una respuesta.
3. Pulsar `Portapapeles` en quick actions.
4. Pulsar `Ubicacion`.
5. Pulsar `Alarma 10m`.

Resultado esperado:

- no hay crash
- aparece typing indicator
- se insertan mensajes y resultados de tools

### Voz

1. Pulsa el icono de microfono.
2. Dicta una frase sencilla.
3. Comprueba que se envia al chat.
4. Escribe luego `/speak hola desde ares`.

Resultado esperado:

- STT rellena el chat
- TTS reproduce audio

### Camara

1. Pulsa el icono de camara.
2. Haz una captura.
3. Comprueba que se guarda y lanza el prompt `/camera`.

Resultado esperado:

- no hay crash
- `CameraTool` encuentra `captures/latest.jpg`

### Memoria

1. Entra en `Memoria`.
2. Crea una clave `nombre` con valor `Carlos`.
3. Cierra y vuelve a abrir la app.
4. Entra otra vez en `Memoria`.

Resultado esperado:

- el recuerdo sigue ahi

### Tareas

1. Desde chat, manda `/alarm 1 prueba corta`.
2. Entra en `Tareas`.
3. Verifica que la tarea aparece.
4. Espera el disparo de la alarma.

Resultado esperado:

- la tarea se lista
- llega notificacion local

### Settings

1. Entra en `Config`.
2. Cambia `AUTO` a `E2B` y luego a `E4B`.
3. Activa y desactiva `Thinking mode`.
4. Pulsa `Reabrir primer arranque`.

Resultado esperado:

- persiste la preferencia
- al volver a abrir, reaparece onboarding

## Tests automaticos

### Unit tests

```powershell
.\gradlew.bat testDebugUnitTest
```

Cubren:

- `ModelRouterTest`
- `ToolRegistryTest`
- `AgentLoopTest`

### Instrumented tests

Necesitan emulador o dispositivo conectado:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

Cubren:

- `DatabaseTest`

## Cosas a vigilar mientras pruebas

- Si `RecognizerIntent` no aparece, el emulador puede no tener servicios de voz listos.
- `LocationTool` puede devolver vacio si el emulador no tiene una ubicacion simulada.
- La descarga del modelo es muy pesada; para smoke testing puedes continuar sin modelo.
- El cliente de Gemma ahora intenta usar LiteRT-LM y, si no puede, cae a fallback local. Eso sirve para validar UX y flujo incluso antes de tener inferencia final cerrada.

## Orden recomendado de validacion

1. `assembleDebug`
2. arranque de la app
3. chat texto
4. quick actions
5. voz
6. camara
7. memoria persistente
8. tareas/alarma
9. `testDebugUnitTest`
10. `connectedDebugAndroidTest`

## Si quieres probarlo hoy en esta maquina

La accion concreta es esta:

1. Instalar o localizar JDK 17/21
2. configurar Android Studio o `JAVA_HOME` con esa version
3. ejecutar `assembleDebug`
4. lanzar la app en emulador

Cuando tengas ese JDK, el siguiente paso ya no es teorico: relanzo el build y te corrijo los errores reales que salgan.
