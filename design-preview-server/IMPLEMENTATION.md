# ARES Design Variants - Implementación Técnica

## Ubicación Actual en el Código

**Archivo**: `app/src/main/java/com/ares/mobile/ui/screens/ChatScreen.kt`
**Línea**: ~159-240

### Estructura Actual (Minimal - Default)
```kotlin
Surface(
    color = Color.Transparent,
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 12.dp)
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // InputField
        OutlinedTextField(
            value = inputValue,
            onValueChange = { inputValue = it },
            modifier = Modifier
                .fillMaxWidth()
                .onPreviewKeyEvent { ... },
            shape = RoundedCornerShape(12.dp),
            colors = outlinedTextFieldColors(),
            ...
        )
        
        // Actions Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) { ... }
    }
}
```

---

## Variante 1: Minimal (Actual)

**Status**: ✅ Implementado actualmente

**Ventajas**:
- Limpio y simple
- Buen balance entre espacio
- Fácil de mantener

**Código**:
```kotlin
// Sin cambios - es la implementación actual
```

---

## Variante 2: ChatGPT Style

**Status**: ⏳ Listo para implementar

**Cambios requeridos**:

1. Cambiar Surface border-radius:
```kotlin
Surface(
    color = Color(0x0d0d0d),  // Oscuro
    shape = RoundedCornerShape(28.dp),  // MÁS redondeado (píldora)
    border = BorderStroke(1.dp, Color(0x4d4d4d)),  // Border gris
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp)
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = inputValue,
            onValueChange = { inputValue = it },
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.dp, Color.Transparent),  // Sin border
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent
            ),
            ...
        )
        
        Button(
            onClick = { /* send */ },
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xff2020)
            )
        ) {
            Text("→")
        }
    }
}
```

---

## Variante 3: Glassmorphism

**Status**: ⏳ Listo para implementar

**Requisitos**: 
- Compose 1.4.0+
- `androidx.compose.foundation:foundation:1.6.0+`

**Cambios requeridos**:

```kotlin
Surface(
    color = Color.White.copy(alpha = 0.05f),
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 12.dp)
        .blur(radius = 16.dp)
        .background(
            color = Color(0x0d0d0d),
            shape = RoundedCornerShape(16.dp)
        ),
    shape = RoundedCornerShape(16.dp),
    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = inputValue,
            onValueChange = { inputValue = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color.Transparent,
                focusedBorderColor = Color(0xff2020),
                unfocusedBorderColor = Color.Transparent
            ),
            ...
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { /* send */ },
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xff2020).copy(alpha = 0.8f)
                )
            ) {
                Text("ENVIAR")
            }
        }
    }
}
```

---

## Variante 4: Neon ARES (Recomendada)

**Status**: ⏳ Listo para implementar - **RECOMENDADO**

**Ventajas**:
- ✅ Mantiene identidad ARES (rojo neon)
- ✅ Efecto premium y moderno
- ✅ Diferenciación visual clara
- ✅ Colores ya definidos en el tema

**Cambios requeridos**:

```kotlin
Surface(
    color = Color(0xff2020).copy(alpha = 0.05f),  // Fondo rojo muy suave
    shape = RoundedCornerShape(12.dp),
    border = BorderStroke(2.dp, Color(0xff2020)),  // Border rojo vibrante
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 12.dp)
        .shadow(
            elevation = 10.dp,
            shape = RoundedCornerShape(12.dp),
            spotColor = Color(0xff2020).copy(alpha = 0.3f)
        ),
    shadowElevation = 10.dp
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = inputValue,
            onValueChange = { inputValue = it },
            modifier = Modifier
                .fillMaxWidth()
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.key == Key.Enter && !keyEvent.isShiftPressed) {
                        submitMessage()
                        true
                    } else if (keyEvent.key == Key.Enter && keyEvent.isShiftPressed) {
                        inputValue += "\n"
                        true
                    } else {
                        false
                    }
                },
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color.Transparent,
                focusedBorderColor = Color(0xff4040),  // Rojo más claro al enfocar
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color(0xff9090)
            ),
            ...
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Action buttons (camera, mic, newline)
            Spacer(modifier = Modifier.weight(1f))
            
            FilledIconButton(
                onClick = { submitMessage() },
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xff2020)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Enviar",
                    tint = Color.White
                )
            }
        }
    }
}
```

---

## Pasos para Implementar

### 1. Abre el archivo
```
app/src/main/java/com/ares/mobile/ui/screens/ChatScreen.kt
```

### 2. Localiza el composable ChatInputComposer
Busca la línea con `Surface(color = Color.Transparent...`

### 3. Reemplaza la implementación
Copia el código de la variante deseada

### 4. Ajusta imports si es necesario
```kotlin
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.filled.Send
```

### 5. Compila y prueba
```bash
gradlew assembleDebug
```

---

## Comparativa Rápida

| Característica | Minimal | ChatGPT | Glass | Neon |
|---|---|---|---|---|
| Complejidad | Baja | Media | Alta | Media |
| Performance | Excelente | Bueno | Regular* | Bueno |
| Identidad ARES | Media | Baja | Baja | Alta |
| Moderno | Medio | Alto | Muy Alto | Muy Alto |
| Mantenibilidad | Alta | Alta | Media | Alta |

*Glassmorphism requiere blur que puede afectar performance en devices bajos

---

## Recomendación Final

**Implementa: Neon ARES**

Razones:
1. Máxima identidad visual ARES
2. Efecto premium sin sacrificar performance
3. Mejor diferenciación del resto de apps
4. Colores ya disponibles en el tema
5. Compatible con Android 8+

---

## Rollback (Volver a Minimal)

Si quieres volver a la versión actual:
```bash
git checkout -- app/src/main/java/com/ares/mobile/ui/screens/ChatScreen.kt
```
