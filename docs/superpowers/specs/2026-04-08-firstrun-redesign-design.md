# First Run Screen Redesign — Design Spec
*Date: 2026-04-08*

## Goal
Replace the current `FirstRunScreen.kt` with a polished design that mirrors the ARES PC splash/setup aesthetic: the animated radar logo, full title expansion, and a scanline progress panel for the model install status.

---

## Visual Design

### Logo (top section)
Faithful port of the PC `SplashWindow.xaml` logo, drawn with `Canvas` in Jetpack Compose:

| Element | Detail |
|---|---|
| Outer glow ring | Pulsing `drawCircle` (scale 0.88↔1.12, alpha 0.5↔0.9, 2s loop) |
| Mid ring | Static `drawCircle`, opacity 0.28 |
| Inner ring | Static `drawCircle`, opacity 0.42 |
| Dashed ring | Rotating `drawCircle` with `PathEffect.dashPathEffect`, 8s CW |
| Crosshair lines | 4 `drawLine` segments (up/down/left/right, gap at center) |
| Corner tick marks | 8 `drawLine` L-shapes at each corner |
| Radar ping × 2 | Two phase-offset `drawCircle` that scale 0.25→1.4 while fading out (2.2s, offset 1.1s) |
| Diamond outer | Rotating `drawPath` diamond outline, 10s CCW |
| Diamond inner | Rotating `drawPath` diamond filled with `#ff2222→#cc0000` radial gradient + red glow shadow, 6s CW |
| Orbit dot 1 | `drawCircle` orbiting at r=52 CW, 3.5s |
| Orbit dot 2 | `drawCircle` orbiting at r=38 CCW, 5.5s |
| Center dot | `drawCircle` r=3.5, `#FF2020`, glow |

Animation engine: `rememberInfiniteTransition()` with `animateFloat` for each angle and the pulse scale. The logo canvas is 160×160dp.

### Title block
```
ARES              — NeonRed, 30sp, FontFamily.Monospace, ExtraBold, letterSpacing=10sp,
                    glow via TextStyle(shadow = Shadow(color = NeonRed.copy(alpha=0.6f), blurRadius = 24f))
AUTONOMOUS · RESPONSE · ENGINE · SYSTEM  — NeonRed at 12% alpha, 8sp, Monospace, letterSpacing=2sp
on-device AI · Gemma 4                  — TextSecondary, 9sp
```

### Divider
Thin horizontal line fading transparent→`#FF202030`→transparent, centered, 65% width.

### Scanline Progress Card (`SurfaceVariantDark` / `NeonRedBorder`)
Replaces the old static "Estado del modelo" card with a dynamic panel matching Option C (and the PC splash):

```
› [status text]█           ← prefix "›" in NeonRed, blinking cursor █
━━━━━━━━━ ~~scanline~~ ━━  ← 1dp scan animation (LinearGradient sweep, 2s loop)
[████░░░░░░░░░░░] 47%      ← progress bar + percentage
```

States and their display:
| `ModelInstallState` | Status text | Bar width |
|---|---|---|
| `Checking` | "Verificando instalación█" | 0%, animated scanline |
| `Missing` | "⬡ Pendiente · E2B█" | 0% |
| `Downloading(p)` | "↓ Descargando Gemma 4 E2B · p%█" | p% |
| `Ready` | "✓ Modelo listo · [path_basename]" (no cursor) | 100% |
| `Error` | "✗ [message]█" | 0% |

### Permissions Card
Same border card style. Permission items displayed as compact chips:
`📷 Cámara`, `🎤 Micrófono`, `📍 Ubicación`, `🔔 Notificaciones`

### Buttons (bottom)
1. `AresOutlinedButton("Conceder permisos")` — full width
2. `AresFilledButton(...)` — "Descargar modelo Gemma 4" / "Descargando..." (disabled) / "Empezar →" (when Ready)
3. Small text link `AresOutlinedButton("Continuar sin modelo →")` — always enabled, calls `viewModel.markFirstRunCompleted(); onContinue()`

---

## Implementation Notes

### New composable: `AresLogoCanvas`
A `@Composable fun AresLogoCanvas(modifier: Modifier = Modifier, sizeDp: Dp = 160.dp)` that encapsulates the entire animated logo. Uses `Canvas(modifier.size(sizeDp))` + `rememberInfiniteTransition()`.

Animation values needed (all `Float`, infinite):
- `pulseScale` — 0.88f→1.12f, 2s, `FastOutSlowIn`
- `diamondAngle` — 0f→360f, 6s, linear
- `diamondOuterAngle` — 0f→-360f, 10s, linear
- `dashRingAngle` — 0f→360f, 8s, linear
- `ping1Scale` / `ping1Alpha` — scale 0.25→1.4, alpha 0.55→0f, 2.2s
- `ping2Scale` / `ping2Alpha` — same but delayed 1.1s
- `orbit1Angle` — 0f→360f, 3.5s
- `orbit2Angle` — 0f→-360f, 5.5s

Use `withTransform { rotate(angle, pivot) }` when drawing the rotating elements.

### Scanline animation
`Box(Modifier.height(1.dp).fillMaxWidth().clipToBounds())` containing an inner `Box` of `fillMaxWidth(0.3f)` with `background(Brush.horizontalGradient([Transparent, NeonRed@60%, Transparent]))`, animated via `Modifier.offset { IntOffset((scanOffset * parentWidth).toInt(), 0) }`.

`scanOffset: Float` from `rememberInfiniteTransition`, -0.3f → 1.3f, 2s, `LinearEasing`.

### File to modify
`ARES-mobile/app/src/main/java/com/ares/mobile/ui/screens/FirstRunScreen.kt` — rewrite `FirstRunScreen` body. `AresFilledButton` and `AresOutlinedButton` stay unchanged.

No new files needed. No new dependencies.

---

## Out of Scope
- Multi-step wizard flow (not requested)
- Theming or color changes (stays NeonRed / BackgroundDeep)
- Changes to SettingsScreen, ChatScreen, or other screens
