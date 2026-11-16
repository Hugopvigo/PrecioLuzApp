# Rediseño 2026 — PrecioLuz App

Objetivo: lavado de cara de la app Android para acercarla a la estética minimalista de
https://precioluz.hugoperezvigo.es/ **sin perder funcionalidad** y añadiendo las mejoras
que la web ya tiene. Trabajo en `main`, commits pequeños por rebanada, tests tras cada paso.

## Decisiones cerradas con Hugo (2026-11-16)

- **Aurora muy suave**: se conserva el fondo aurora, pero con opacidad y movimiento reducidos.
- **Barra flotante Hoy/Mañana abajo**: se mantiene (es exclusiva de la app, la web no la tiene).
- **Mínima / media / máxima del día**: se mantienen y se refuerzan en la tarjeta de estadísticas.
- **Mejoras de la web a importar**: selector 1 h / 2 h / 3 h, % respecto a la media de hoy,
  línea de media y franjas de tramo en el gráfico, texto de ahorro en la recomendación.
- **Colores de tramo (Hugo, 2026-11-16)**: valle = verde (barato), llano = ámbar (medio),
  punta = rojo (caro), con más potencia y buena visibilidad en tema oscuro. El valle actual
  es gris y apenas se ve: se sustituye por verde vivo en ambos temas.
- **Notificaciones locales**: se implementan en esta tanda (WorkManager ya está como dependencia).

## Rebanadas

### 1. Sistema visual ✅
- `Color.kt`: tokens claros/oscuros alineados con `index.css` de la web.
- `Theme.kt`: esquema de color con los tokens nuevos.
- `Type.kt`: pesos y tracking más sobrios (Bold en vez de ExtraBold).
- `AuroraBackground.kt`: blobs más tenues (α ≈ .30) y ciclo más lento (28 s).
- `GlassCard.kt`: superficie sólida + borde 1 px, radio 16 dp (sin glow).
- `MainActivity.kt` / `FloatingTabBar.kt`: cabecera plana, píldora inferior conservada.

### 2. Contenido
- `HeroPriceCard.kt`: añadir «% por encima/debajo de la media de hoy (X)».
- `StatRow.kt`: mínima / media / máxima, más limpias.
- `RecoCard.kt`: selector 1/2/3 h + recomendación con ahorro + hora más cara.
- `PriceChart.kt`: línea de media, tira de tramos, etiqueta de selección, mín/máx.
- `HourListItem.kt` / pantallas: densidad y espaciado acordes a la web.

### 3. Lógica de dominio + tests
- `PriceMath.kt`: `cheapestWindow`, `versusAverage`, `savingPercent` (funciones puras).
- Tests unitarios de las tres funciones.

### 4. Ajustes y notificaciones
- `SettingsDataStore`: persistir los dos interruptores de notificación.
- `SettingsViewModel` / `SettingsScreen`: ajustes reales dentro de un `ModalBottomSheet`.
- `NotificationScheduler` + `PriceNotificationWorker` + `Notifier`: aviso de precios de
  mañana (~20:15) y resumen del día (08:00), con permiso `POST_NOTIFICATIONS`.
- Tests del formato de los mensajes de notificación.

### 5. Verificación
- `./gradlew testDebugUnitTest` y `./gradlew assembleDebug` (JDK 21 disponible).
- Detector de Impeccable sobre los ficheros de UI cambiados (una sola pasada).
- Actualizar `README.md`, `Changelog.md` y commit por rebanada.

## Fuera de alcance
Widget, selector de zona, publicación en Play Store, cambios en el backend de la web.
