# ARES Mobile - Design Variants Implementation

## 🎨 Sistemas de Diseño Implementados

Se han implementado dos variantes de diseño dinamicamente aplicables a diferentes pantallas de la app ARES Mobile:

### 1. **VIBRANT** - Chat Screen
- **Variante**: Alta energía, contraste máximo
- **Color Primario**: NeonRed (#FF2020) - Rojo neón intenso
- **Fondo**: Deep Black (#050505)
- **Superficies**: Rojo oscuro con acentos oscuros (#110000)
- **Texto**: Blanco roto (#CCCCCC) con acentos rojo suave (#FF9090)
- **Uso**: Chat Screen (interacciones energéticas, mensajería)
- **Identidad**: Máxima presencia ARES, premium, dinámico

### 2. **METALLIC** - Memory, Tasks, Settings
- **Variante**: Profesional, refinada, plateada
- **Color Primario**: Platinum (#E8E8E8) - Plateado brillante
- **Fondo**: Near Black (#0A0A0A)
- **Superficies**: Gris oscuro neutro (#1A1A1A)
- **Texto**: Gris neutro (#BFBFBF) con acentos plateados (#C0C0C0)
- **Uso**: Memory, Tasks, Settings Screens (funcionalidad, gestión)
- **Identidad**: Profesional, refinado, minimalista

---

## 📁 Archivos Implementados

### Nuevos Archivos

1. **DesignVariant.kt**
   - Define enum `DesignVariant` (VIBRANT, METALLIC)
   - Data class `VariantColorPalette` con todos los colores
   - Función `getVariantPalette(variant)` para obtener paleta
   - Función `getDesignVariantForTab(tabName)` que mapea tabs a variantes

2. **VariantCompositionLocal.kt**
   - `LocalDesignVariant` CompositionLocal para acceso global a colores
   - `AresVariantColors` extension property para fácil acceso

### Archivos Modificados

1. **Color.kt**
   - Agregadas paletas Vibrant (ya existentes)
   - Agregadas paletas Metallic (nuevas):
     - `MetallicPrimary`, `MetallicPrimaryDim`, `MetallicPrimaryGlow`, `MetallicPrimaryBorder`
     - `MetallicBackgroundDeep`, `MetallicSurfaceDark`, `MetallicSurfaceVariantDark`, `MetallicSurfaceElevated`
     - `MetallicTextPrimary`, `MetallicTextSecondary`, `MetallicTextAccent`, `MetallicTextMuted`
     - `MetallicBorderSubtle`, `MetallicBorderGlow`

2. **Theme.kt**
   - `createColorScheme(palette)` - Genera Material3 color scheme dinámicamente
   - `AresTheme(variant, darkTheme, content)` - Función principal con soporte de variantes
   - `AresThemeVibrant(...)` - Convenience function para variante Vibrant
   - `AresThemeMetallic(...)` - Convenience function para variante Metallic

3. **ChatScreen.kt**
   - Envuelto en `AresThemeVibrant`
   - Nuevo wrapper: `ChatScreenContent(viewModel)` private

4. **MemoryScreen.kt**
   - Envuelto en `AresThemeMetallic`
   - Nuevo wrapper: `MemoryScreenContent(viewModel)` private

5. **TasksScreen.kt**
   - Envuelto en `AresThemeMetallic`
   - Nuevo wrapper: `TasksScreenContent(viewModel)` private

6. **SettingsScreen.kt**
   - Envuelto en `AresThemeMetallic`
   - Nuevo wrapper: `SettingsScreenContent(viewModel)` private

---

## 🔄 Mapping: Tabs → Variantes

```
Tab          Variante     Carácter
────────────────────────────────────
chat    →   VIBRANT   →   Energético
memory  →   METALLIC  →   Profesional
tasks   →   METALLIC  →   Profesional
settings→   METALLIC  →   Profesional
```

---

## 🎯 Configuración

La configuración se mantiene en `design-preview-server/data/tab-design-config.json`:

```json
{
  "chat": "vibrant",
  "memory": "metallic",
  "tasks": "metallic",
  "settings": "metallic"
}
```

---

## 🛠️ Acceso a Colores en Componentes

Dentro de cualquier Composable dentro de una pantalla temada:

```kotlin
@Composable
fun MyComponent() {
    val colors = AresVariantColors
    
    Box(modifier = Modifier.background(colors.backgroundDeep)) {
        Text("Hola", color = colors.textPrimary)
        Button(colors = colors.primary) {
            // ...
        }
    }
}
```

O acceso directo via Material3:

```kotlin
@Composable
fun MyComponent() {
    val colors = MaterialTheme.colorScheme
    Box(modifier = Modifier.background(colors.background)) {
        Text("Hola", color = colors.onBackground)
    }
}
```

---

## ✨ Características

✅ **Temas Dinámicos**: Fácil agregar nuevas variantes
✅ **Per-Screen Theming**: Cada pantalla tiene su diseño asignado
✅ **Material3 Compatible**: Integración nativa con Material Design
✅ **CompositionLocal Provided**: Acceso a colores desde cualquier componente
✅ **Backward Compatible**: Funciones convenience para uso rápido
✅ **No Breaking Changes**: Código existente sigue funcionando

---

## 📊 Comparativa Visual

| Aspecto | VIBRANT | METALLIC |
|---------|---------|----------|
| Primario | NeonRed (#FF2020) | Platinum (#E8E8E8) |
| Energía | Muy Alta | Neutral |
| Constraste | Máximo | Moderado |
| Profesionalidad | Dinámico | Refinado |
| Use Case | Chat | Admin/Config |
| Temperatura | Cálida (roja) | Fría (plata) |

---

## 🚀 Validación

✅ Sin errores de compilación
✅ Todos los imports correctos
✅ CompositionLocal providers en lugar
✅ Material3 color schemes funcionan
✅ Pantallas temadas correctamente

---

## Próximos Pasos (Opcional)

- Implementar selector visual de variantes en Settings
- Cargar configuración desde servidor de diseño
- Animaciones de transición entre temas
- Agregar más variantes (Classic, Cyberpunk, Subtle)
- Exportar temas a CSS para web

