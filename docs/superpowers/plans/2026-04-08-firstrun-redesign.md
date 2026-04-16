# First Run Screen Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rewrite `FirstRunScreen.kt` with a faithful port of the ARES PC animated radar logo and replace the static model status card with a scanline progress panel.

**Architecture:** All new composables (`AresLogoCanvas`, `ScanlineProgressCard`) are private functions added at the bottom of the same file. `AresFilledButton` and `AresOutlinedButton` are untouched. `FirstRunScreen` body is rewritten to use the new composables.

**Tech Stack:** Jetpack Compose Canvas drawscope, `rememberInfiniteTransition`, `animateFloat`, `BoxWithConstraints`, `PathEffect`, `Brush`.

---

## File Map

| File | Action |
|---|---|
| `ARES-mobile/app/src/main/java/com/ares/mobile/ui/screens/FirstRunScreen.kt` | Modify — rewrite `FirstRunScreen` body, add `AresLogoCanvas` and `ScanlineProgressCard` private composables, update imports |

---

## Task 1: Add imports and `AresLogoCanvas` composable

**Files:**
- Modify: `ARES-mobile/app/src/main/java/com/ares/mobile/ui/screens/FirstRunScreen.kt`

- [ ] **Step 1: Replace the import block at the top of `FirstRunScreen.kt`**

Replace everything from line 1 (`package …`) down to and including the last `import` line with:

```kotlin
package com.ares.mobile.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ares.mobile.ai.ModelInstallState
import com.ares.mobile.ui.theme.BackgroundDeep
import com.ares.mobile.ui.theme.BorderSubtle
import com.ares.mobile.ui.theme.NeonRed
import com.ares.mobile.ui.theme.NeonRedBorder
import com.ares.mobile.ui.theme.NeonRedDim
import com.ares.mobile.ui.theme.SurfaceDark
import com.ares.mobile.ui.theme.SurfaceVariantDark
import com.ares.mobile.ui.theme.TextAres
import com.ares.mobile.ui.theme.TextMuted
import com.ares.mobile.ui.theme.TextSecondary
import com.ares.mobile.viewmodel.SettingsViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
```

- [ ] **Step 2: Add `AresLogoCanvas` at the bottom of the file (after the last `}` of `AresOutlinedButton`)**

```kotlin
@Composable
private fun AresLogoCanvas(modifier: Modifier = Modifier, sizeDp: Dp = 160.dp) {
    val transition = rememberInfiniteTransition(label = "logo")

    // Outer ring pulse (scale 0.88 ↔ 1.12, 2 s)
    val pulseScale by transition.animateFloat(
        initialValue = 0.88f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse",
    )
    // Mid ring pulse (scale 1.0 ↔ 1.08, 1.5 s, offset 0.5 s)
    val midPulseScale by transition.animateFloat(
        initialValue = 1.0f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse,
            initialStartOffset = StartOffset(500),
        ),
        label = "midPulse",
    )
    // Inner diamond rotates CW 6 s
    val diamondAngle by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart),
        label = "diamond",
    )
    // Outer diamond rotates CCW 10 s
    val diamondOuterAngle by transition.animateFloat(
        initialValue = 0f, targetValue = -360f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Restart),
        label = "diamondOuter",
    )
    // Dashed ring rotates CW 8 s
    val dashRingAngle by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart),
        label = "dashRing",
    )
    // Radar ping 1 (scale + fade, 2.2 s)
    val ping1Scale by transition.animateFloat(
        initialValue = 0.25f, targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "p1s",
    )
    val ping1Alpha by transition.animateFloat(
        initialValue = 0.55f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "p1a",
    )
    // Radar ping 2 — same but offset 1.1 s
    val ping2Scale by transition.animateFloat(
        initialValue = 0.25f, targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            tween(2200, easing = LinearEasing), RepeatMode.Restart,
            initialStartOffset = StartOffset(1100),
        ),
        label = "p2s",
    )
    val ping2Alpha by transition.animateFloat(
        initialValue = 0.55f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            tween(2200, easing = LinearEasing), RepeatMode.Restart,
            initialStartOffset = StartOffset(1100),
        ),
        label = "p2a",
    )
    // Orbit dot 1 — CW 3.5 s
    val orbit1Angle by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3500, easing = LinearEasing), RepeatMode.Restart),
        label = "o1",
    )
    // Orbit dot 2 — CCW 5.5 s
    val orbit2Angle by transition.animateFloat(
        initialValue = 0f, targetValue = -360f,
        animationSpec = infiniteRepeatable(tween(5500, easing = LinearEasing), RepeatMode.Restart),
        label = "o2",
    )

    Canvas(modifier = modifier.size(sizeDp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.width / 2f
        val red = NeonRed
        val sp = 1.dp.toPx()
        val dashPx = 4.dp.toPx()
        val gapPx = 3.dp.toPx()

        // ── Outer glow ring (pulsing scale) ──────────────────────────────
        withTransform({ scale(pulseScale, pivot = Offset(cx, cy)) }) {
            drawCircle(
                color = red.copy(alpha = 0.22f),
                radius = r * 0.89f,
                style = Stroke(width = 8.dp.toPx()),
            )
        }

        // ── Mid ring (slight pulse) ───────────────────────────────────────
        withTransform({ scale(midPulseScale, pivot = Offset(cx, cy)) }) {
            drawCircle(color = red.copy(alpha = 0.28f), radius = r * 0.71f, style = Stroke(width = sp))
        }

        // ── Inner ring ───────────────────────────────────────────────────
        drawCircle(color = red.copy(alpha = 0.42f), radius = r * 0.51f, style = Stroke(width = sp))

        // ── Dashed ring (rotating) ────────────────────────────────────────
        withTransform({ rotate(dashRingAngle, Offset(cx, cy)) }) {
            drawCircle(
                color = red.copy(alpha = 0.30f),
                radius = r * 0.615f,
                style = Stroke(
                    width = sp,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashPx, gapPx)),
                ),
            )
        }

        // ── Crosshair lines (gap at center) ──────────────────────────────
        val gapFrac = 0.43f
        val lineAlpha = 0.60f
        drawLine(red.copy(alpha = lineAlpha), Offset(0f, cy), Offset(cx * gapFrac, cy), strokeWidth = sp)
        drawLine(red.copy(alpha = lineAlpha), Offset(cx * (2f - gapFrac), cy), Offset(size.width, cy), strokeWidth = sp)
        drawLine(red.copy(alpha = lineAlpha), Offset(cx, 0f), Offset(cx, cy * gapFrac), strokeWidth = sp)
        drawLine(red.copy(alpha = lineAlpha), Offset(cx, cy * (2f - gapFrac)), Offset(cx, size.height), strokeWidth = sp)

        // ── Corner tick marks ─────────────────────────────────────────────
        val tick = 12.dp.toPx()
        val tkW = 1.5.dp.toPx()
        val ta = 0.50f
        val w = size.width
        val h = size.height
        drawLine(red.copy(ta), Offset(0f, 0f), Offset(tick, 0f), tkW)
        drawLine(red.copy(ta), Offset(0f, 0f), Offset(0f, tick), tkW)
        drawLine(red.copy(ta), Offset(w - tick, 0f), Offset(w, 0f), tkW)
        drawLine(red.copy(ta), Offset(w, 0f), Offset(w, tick), tkW)
        drawLine(red.copy(ta), Offset(0f, h - tick), Offset(0f, h), tkW)
        drawLine(red.copy(ta), Offset(0f, h), Offset(tick, h), tkW)
        drawLine(red.copy(ta), Offset(w, h - tick), Offset(w, h), tkW)
        drawLine(red.copy(ta), Offset(w - tick, h), Offset(w, h), tkW)

        // ── Radar pings ───────────────────────────────────────────────────
        val pingR = r * 0.845f
        drawCircle(red.copy(alpha = ping1Alpha), radius = pingR * ping1Scale, style = Stroke(width = 1.5.dp.toPx()))
        drawCircle(red.copy(alpha = ping2Alpha), radius = pingR * ping2Scale, style = Stroke(width = 1.5.dp.toPx()))

        // ── Diamond outer (outline, CCW) ──────────────────────────────────
        withTransform({ rotate(diamondOuterAngle, Offset(cx, cy)) }) {
            val dO = r * 0.44f
            val diamondOuter = Path().apply {
                moveTo(cx, cy - dO); lineTo(cx + dO, cy)
                lineTo(cx, cy + dO); lineTo(cx - dO, cy)
                close()
            }
            drawPath(diamondOuter, color = red.copy(alpha = 0.55f), style = Stroke(width = 1.5.dp.toPx()))
        }

        // ── Diamond inner (filled, CW) ────────────────────────────────────
        withTransform({ rotate(diamondAngle, Offset(cx, cy)) }) {
            val dI = r * 0.235f
            val diamondInner = Path().apply {
                moveTo(cx, cy - dI); lineTo(cx + dI, cy)
                lineTo(cx, cy + dI); lineTo(cx - dI, cy)
                close()
            }
            drawPath(
                diamondInner,
                brush = Brush.radialGradient(
                    listOf(Color(0xFFFF3333), Color(0xFFCC0000)),
                    center = Offset(cx - dI * 0.3f, cy - dI * 0.5f),
                    radius = dI * 1.6f,
                ),
            )
            drawPath(diamondInner, color = red.copy(alpha = 0.70f), style = Stroke(width = sp))
        }

        // ── Orbiting dot 1 (CW, outer orbit) ─────────────────────────────
        val o1Rad = orbit1Angle * PI.toFloat() / 180f
        val o1R = r * 0.80f
        drawCircle(red.copy(alpha = 0.9f), radius = 3.dp.toPx(), center = Offset(cx + o1R * cos(o1Rad), cy + o1R * sin(o1Rad)))

        // ── Orbiting dot 2 (CCW, inner orbit) ────────────────────────────
        val o2Rad = orbit2Angle * PI.toFloat() / 180f
        val o2R = r * 0.585f
        drawCircle(red.copy(alpha = 0.5f), radius = 2.dp.toPx(), center = Offset(cx + o2R * cos(o2Rad), cy + o2R * sin(o2Rad)))

        // ── Center dot ────────────────────────────────────────────────────
        drawCircle(red, radius = 3.5.dp.toPx(), center = Offset(cx, cy))
    }
}
```

- [ ] **Step 3: Build to verify no compile errors so far**

```bash
cd ARES-mobile && ./gradlew :app:compileDebugKotlin 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL` (or only pre-existing warnings, zero new errors).

---

## Task 2: Add `ScanlineProgressCard` composable

**Files:**
- Modify: `ARES-mobile/app/src/main/java/com/ares/mobile/ui/screens/FirstRunScreen.kt`

- [ ] **Step 1: Add `ScanlineProgressCard` after `AresLogoCanvas` (before the end of the file)**

```kotlin
@Composable
private fun ScanlineProgressCard(installState: ModelInstallState) {
    val scanTransition = rememberInfiniteTransition(label = "scan")
    val scanOffset by scanTransition.animateFloat(
        initialValue = -0.3f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "scanOffset",
    )

    val cursorTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by cursorTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Reverse),
        label = "cursorAlpha",
    )

    val statusText = when (val s = installState) {
        is ModelInstallState.Missing -> "⬡ Pendiente · ${s.variant.displayName}"
        is ModelInstallState.Downloading -> "↓ Descargando ${s.variant.displayName} · ${s.progressPercent}%"
        is ModelInstallState.Ready -> "✓ Modelo listo · ${s.path.substringAfterLast('/')}"
        is ModelInstallState.Error -> "✗ ${s.message}"
        ModelInstallState.Checking -> "Verificando instalación"
    }
    val showCursor = installState !is ModelInstallState.Ready
    val progressFraction = when (val s = installState) {
        is ModelInstallState.Downloading -> s.progressPercent / 100f
        is ModelInstallState.Ready -> 1f
        else -> 0f
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceVariantDark, RoundedCornerShape(11.dp))
            .border(1.dp, NeonRedBorder, RoundedCornerShape(11.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Status line with blinking cursor
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("›", color = NeonRed, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.width(5.dp))
            Text(statusText, color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            if (showCursor) {
                Text(
                    "█",
                    color = NeonRed.copy(alpha = cursorAlpha),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }

        // Scanline sweep
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(SurfaceDark),
        ) {
            val trackWidth = maxWidth
            Box(
                modifier = Modifier
                    .width(trackWidth * 0.3f)
                    .height(1.dp)
                    .offset { IntOffset((scanOffset * trackWidth.toPx()).toInt(), 0) }
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, NeonRed.copy(alpha = 0.6f), Color.Transparent),
                        ),
                    ),
            )
        }

        // Progress bar + percentage
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier.weight(1f).height(3.dp),
                color = NeonRed,
                trackColor = SurfaceDark,
            )
            Text(
                "${(progressFraction * 100).toInt()}%",
                color = NeonRed,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}
```

- [ ] **Step 2: Build to verify no compile errors**

```bash
cd ARES-mobile && ./gradlew :app:compileDebugKotlin 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`

---

## Task 3: Rewrite `FirstRunScreen` body

**Files:**
- Modify: `ARES-mobile/app/src/main/java/com/ares/mobile/ui/screens/FirstRunScreen.kt`

- [ ] **Step 1: Replace the `FirstRunScreen` composable body (lines 56–201 in the original file)**

Replace the entire `fun FirstRunScreen(...)` function (keep the private `requestedPermissions` array above it, keep the `AresFilledButton` and `AresOutlinedButton` functions below it) with:

```kotlin
@Composable
fun FirstRunScreen(
    onContinue: () -> Unit,
    viewModel: SettingsViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(20.dp))

            // ── Animated ARES logo ──────────────────────────────────────
            AresLogoCanvas(sizeDp = 160.dp)

            // ── Title block ─────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    "ARES",
                    style = TextStyle(
                        color = NeonRed,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 10.sp,
                        shadow = Shadow(
                            color = NeonRed.copy(alpha = 0.6f),
                            offset = Offset.Zero,
                            blurRadius = 24f,
                        ),
                    ),
                )
                Text(
                    "AUTONOMOUS · RESPONSE · ENGINE · SYSTEM",
                    color = NeonRed.copy(alpha = 0.20f),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "on-device AI · Gemma 4",
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }

            // ── Divider ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, NeonRedBorder, Color.Transparent),
                        ),
                    ),
            )

            // ── Scanline progress card ───────────────────────────────────
            ScanlineProgressCard(installState = state.installState)

            // ── Permissions chips ───────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark, RoundedCornerShape(11.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(11.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "PERMISOS",
                    color = TextMuted,
                    fontSize = 8.sp,
                    letterSpacing = 1.5.sp,
                    fontFamily = FontFamily.Monospace,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    listOf("📷 Cámara", "🎤 Micro", "📍 GPS", "🔔 Notif.").forEach { label ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(BackgroundDeep, RoundedCornerShape(6.dp))
                                .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                label,
                                color = TextSecondary,
                                fontSize = 7.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            // ── Buttons ─────────────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AresOutlinedButton("Conceder permisos") {
                    permissionLauncher.launch(requestedPermissions)
                }
                AresFilledButton(
                    label = when {
                        state.isInstalling -> "Descargando..."
                        state.installState is ModelInstallState.Ready -> "Empezar →"
                        else -> "Descargar modelo Gemma 4"
                    },
                    enabled = !state.isInstalling,
                ) {
                    if (state.installState is ModelInstallState.Ready) {
                        viewModel.markFirstRunCompleted()
                        onContinue()
                    } else {
                        viewModel.installSelectedModel(context)
                    }
                }
                AresFilledButton(label = "Continuar sin modelo →") {
                    viewModel.markFirstRunCompleted()
                    onContinue()
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}
```

- [ ] **Step 2: Full build to confirm everything compiles**

```bash
cd ARES-mobile && ./gradlew assembleDebug 2>&1 | tail -30
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
cd ARES-mobile && git add app/src/main/java/com/ares/mobile/ui/screens/FirstRunScreen.kt
git commit -m "feat(mobile): redesign FirstRunScreen with PC-faithful ARES logo and scanline progress panel"
```

---

## Self-Review Checklist

- [x] **Logo elements**: outer glow ring (pulse), mid ring (pulse), inner ring, dashed ring (rotate), crosshairs, corner ticks, radar ping ×2 (phase offset), diamond outer (CCW outline), diamond inner (CW filled gradient), orbit dot 1 (CW), orbit dot 2 (CCW), center dot — all present.
- [x] **Scanline card states**: Checking, Missing, Downloading (with %), Ready (100%, no cursor), Error (0%, cursor) — all covered.
- [x] **Buttons**: Conceder permisos → permission launcher; Descargar/Descargando.../Empezar → calls service or `onContinue()`; Continuar → always calls `onContinue()`. ✓
- [x] **Imports**: `kotlin.math.cos`, `kotlin.math.sin`, `kotlin.math.PI`, `Offset`, `Shadow`, `PathEffect`, `BoxWithConstraints`, `IntOffset`, `verticalScroll`, `rememberScrollState` all present.
- [x] **`AresFilledButton` / `AresOutlinedButton`**: unchanged, stay in the same file.
- [x] **No placeholders**: all code is complete.
