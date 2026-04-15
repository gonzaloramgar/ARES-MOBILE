# ARES Design Preview Server

Servidor local (localhost) para visualizar y elegir entre variantes de diseño para la app móvil ARES.

## Características

- **4 Variantes de Diseño** para el chat composer:
  - Minimal: Diseño limpio y simple
  - ChatGPT Style: Barra redondeada estilo ChatGPT
  - Glassmorphism: Efecto vidrio frosted moderno
  - Neon ARES: Estilo neon rojo característico

- **Preview Interactivo**: Visualiza cada variante en tiempo real
- **Documentación Integrada**: Detalles técnicos para implementar cada estilo
- **Selección Visual**: Elige tu favorito directamente desde el navegador

## Instalación Rápida

### 1. Instalar Node.js

Si no tienes Node.js instalado, descárgalo desde [nodejs.org](https://nodejs.org)

### 2. Instalar dependencias

```bash
# En la carpeta design-preview-server
npm install
```

### 3. Iniciar el servidor

```bash
npm start
```

Verás:
```
╔════════════════════════════════════════╗
║   ARES Design Preview Server           ║
║   http://localhost:3000                ║
║   Abre tu navegador para ver mockups    ║
╚════════════════════════════════════════╝
```

### 4. Abre en el navegador

```
http://localhost:3000
```

## Estructura

```
design-preview-server/
├── server.js           # Servidor Express
├── package.json        # Dependencias
├── public/
│   └── index.html      # Interfaz interactiva
└── README.md           # Este archivo
```

## Variantes Disponibles

### 1. Minimal
- Layout: `Row(horizontalArrangement = Arrangement.spacedBy(8.dp))`
- Ubicación actual en ChatScreen.kt
- Implementación: Más adelante puedes cambiar a otra variante

### 2. ChatGPT Style
```kotlin
Surface(
    shape = RoundedCornerShape(24.dp),
    color = Color(0x0d0d0d),
    border = BorderStroke(1.dp, Color(0x4d4d4d))
)
```
Cambio: Aumenta border-radius a 28dp para más efecto de píldora

### 3. Glassmorphism
```kotlin
Surface(
    modifier = Modifier.blur(16.dp),
    color = Color.White.copy(alpha = 0.05f),
    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
)
```
Requiere: Compose 1.4+ con `Modifier.blur()`

### 4. Neon ARES
```kotlin
Surface(
    color = Color(0xff2020).copy(alpha = 0.05f),
    border = BorderStroke(2.dp, Color(0xff2020)),
    modifier = Modifier.shadow(
        elevation = 10.dp,
        shape = RoundedCornerShape(12.dp)
    )
)
```
Ventaja: Usa colores existentes de ARES, máxima identidad visual

## Próximos Pasos

1. **Elige tu variante favorita** en http://localhost:3000
2. **Obtén el código** de la sección de detalles
3. **Aplica el cambio** en `app/src/main/java/com/ares/mobile/ui/screens/ChatScreen.kt`
4. **Compila y prueba** en el emulador/device

## Recomendación

Para ARES, se recomienda **Neon ARES** porque:
- ✅ Mantiene la identidad visual roja de ARES
- ✅ Usa colores ya definidos en el tema
- ✅ Efecto glow profesional
- ✅ Diferencia clara de otros apps
- ✅ Compatible con Android 8+

## Personalización

Puedes editar `public/index.html` para:
- Agregar más variantes
- Cambiar colores
- Añadir ejemplos de otros componentes
- Mostrar comparativas side-by-side

## Parar el servidor

Press `Ctrl+C` en la terminal
