# 🚀 Quick Start - ARES Design Preview

## 1️⃣ Instalar Node.js (si no lo tienes)

👉 **Descarga desde**: https://nodejs.org
- Elige LTS (recomendado)
- Instala normalmente
- Verifica con: `node --version` en terminal

---

## 2️⃣ Iniciar el Servidor

### Opción A: Windows (Más Fácil) ⭐
1. Ve a la carpeta: `design-preview-server`
2. **Haz doble click** en `start-server.bat`
3. Si Node.js no está instalado:
   - Se abrirá automáticamente una guía de instalación en el navegador
   - Sigue los pasos indicados
   - Después de instalar y reiniciar, vuelve a hacer doble click
4. ¡Listo! El servidor estará en `http://localhost:3000`

### Opción B: Terminal Manual
```bash
cd "C:\Users\grg30\Desktop\PROYECTOS\ProyectoARES\design-preview-server"
npm install
npm start
```

---

## 3️⃣ Abre en el Navegador

Cuando veas esto en la terminal:
```
╔════════════════════════════════════════╗
║   ARES Design Preview Server           ║
║   http://localhost:3000                ║
║   Abre tu navegador para ver mockups    ║
╚════════════════════════════════════════╝
```

👉 **Abre**: http://localhost:3000

---

## 4️⃣ Páginas Disponibles

| URL | Descripción |
|-----|-------------|
| `http://localhost:3000` | Mockups interactivos de 4 variantes |
| `http://localhost:3000/compare` | Análisis detallado pro/contra |

---

## 5️⃣ Elige tu Variante

En el sitio web:
1. Observa las 4 variantes
2. Lee las características
3. Haz click en **"Selecciona tu favorito"**
4. Ve a la sección "Análisis detallado" para ver pros y contras

---

## 6️⃣ Implementa en el Código

**Archivo**: `app/src/main/java/com/ares/mobile/ui/screens/ChatScreen.kt`

👉 **Guía completa en**: `design-preview-server/IMPLEMENTATION.md`

### Pasos:
1. Abre el archivo ChatScreen.kt
2. Busca la línea ~159 donde está el Surface composer
3. Reemplaza según tu variante elegida
4. Compila: `gradlew assembleDebug`

---

## 7️⃣ Recomendación 🏆

**Implementa: Neon ARES**

✅ Máxima identidad ARES (rojo neon)  
✅ Premium sin perder performance  
✅ Mejor diferenciación visual  
✅ Compatible Android 8+  

---

## Troubleshooting

### "El .bat se abre y se cierra rápidamente"
→ Descarga e instala Node.js desde https://nodejs.org
→ Asegúrate de elegir "Add to PATH" durante la instalación
→ Reinicia la computadora después de instalar
→ Vuelve a hacer doble click en `start-server.bat`

### "Node is not recognized"
→ Instala Node.js desde nodejs.org, reinicia terminal

### "Port 3000 already in use"
→ Otro proceso usa el puerto:
```bash
# Encuentra qué usa el puerto
netstat -ano | findstr :3000

# Mata el proceso (busca el PID)
taskkill /PID <PID> /F
```

### El navegador muestra "Cannot GET /"
→ Asegúrate que server.js está corriendo
→ Abre `http://localhost:3000` (con puerto)

---

## Archivos de Referencia

- 📄 **IMPLEMENTATION.md** - Código Kotlin para cada variante
- 📖 **README.md** - Documentación completa
- 🎨 **public/index.html** - Mockups interactivos
- 🔍 **public/compare.html** - Análisis comparativo

---

## Parar el Servidor

Press: **Ctrl + C** en la terminal

---

## ¿Preguntas?

1. **¿Cómo veo el código exacto?** → Ve a `http://localhost:3000/compare`
2. **¿Cuál es la mejor?** → Neon ARES (ya preseleccionada)
3. **¿Puedo cambiar colores?** → Sí, en `IMPLEMENTATION.md` línea de color

---

**¡Listo!** 🎉 Ya puedes explorar las variantes de diseño en el navegador.
