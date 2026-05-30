# Plan.md — PrecioLuz App

## Visión

App nativa Android moderna, bonita, sencilla y elegante para consultar los precios PVPC de la luz en España (Península). Sin ads, sin registro, sin servidor. Notificaciones locales configurables.

---

## Decisiones de diseño

| Decisión | Elección | Razón |
|---|---|---|
| Plataforma | Android nativo (Kotlin + Compose) | Mejor UX, acceso completo a APIs del sistema |
| Notificaciones | Locales (WorkManager) | No necesitamos servidor ni Firebase para este caso de uso |
| Zona geográfica | Solo Península (geo_id 8741) | La gran mayoría de usuarios. Selector de zona en sprint futuro |
| Monetización | Gratuita, sin ads | Proyecto personal/community |
| Widget | Sprint futuro | Prioridad a la app base funcional |
| Offline-first | Room como source of truth | Solo 1 llamada API por día, funciona sin red |
| API key | `local.properties` → `BuildConfig` | Estándar Android, token de lectura pública REE |

---

## Stack técnico detallado

### Lenguaje y UI
- **Kotlin 2.x** — lenguaje principal
- **Jetpack Compose** — toolkit UI declarativo
- **Material 3** — design system con dynamic theming

### Arquitectura: MVVM + Unidirectional Data Flow
- **3 capas**: UI → Domain (use cases) → Data (repositories)
- **ViewModels** exponen `StateFlow<UiState>`
- Eventos fluyen hacia abajo, datos fluyen hacia arriba via Kotlin Flows
- Room DB es el source of truth, sincronizado con la API

### Librerías

| Propósito | Librería | Versión |
|---|---|---|
| Networking | Retrofit + OkHttp | 2.11.x / 4.12.x |
| Serialización JSON | kotlinx-serialization | 1.8.x |
| DI | Hilt (Dagger) | 2.59.x |
| Base de datos local | Room | 2.8.x |
| Preferencias | DataStore (Proto) | 1.2.x |
| Background work | WorkManager | 2.10.x |
| Corrutinas | kotlinx-coroutines | 1.10.x |
| Fechas | kotlinx-datetime | 0.6.x |
| Navegación | Navigation Compose | 2.8.x |
| Gradle secrets | secrets-gradle-plugin | 2.0.x |

### SDK
- **minSdk**: 26 (Android 8.0) — cobertura ~95% del mercado
- **targetSdk**: 35 (Android 15)

---

## Integración API — REE ESIOS

Basado en la integración existente del [PrecioLuz Bot](https://github.com/hugopvigo/PrecioLuz).

### Endpoint

```
GET https://api.esios.ree.es/indicators/1001
    ?start_date=2026-05-30T00:00:00+02:00
    &end_date=2026-05-30T23:59:59+02:00
    &time_trunc=hour
```

### Autenticación

```
x-api-key: <ESIOS_API_TOKEN>
Accept: application/json
```

> La API key se obtiene solicitándola en `api_token@ree.es`. Es gratuita para uso personal.

### Respuesta (estructura)

```json
{
  "indicator": {
    "values": [
      {
        "geo_id": 8741,
        "value": 160.47,
        "datetime": "2026-05-30T00:00:00Z",
        "time_interval": {
          "start": "2026-05-30T00:00:00+02:00"
        }
      }
    ]
  }
}
```

### Reglas de parsing
- Filtrar por `geo_id == 8741` (Península)
- `value` está en EUR/MWh → dividir por 1000 para EUR/kWh
- Extraer hora de `time_interval.start` (preferido) o `datetime` (fallback)
- Convertir a hora local Madrid (Europe/Madrid)
- Ordenar por hora ascendente (0-23)

### Lógica hoy/mañana

| Hora actual (Madrid) | Fecha objetivo | Label |
|---|---|---|
| 00:00 — 19:59 | Hoy | "hoy" |
| 20:00 — 23:59 | Mañana | "mañana" |

Los datos de mañana suelen estar disponibles a partir de las ~20:30 CET.

### Caché (offline-first)

1. **Room DB** — source of truth. Si los precios de un día ya existen en DB, no se llama a la API.
2. **Solo 1 llamada API por día** — cumplimiento de los términos de uso de REE.

### Reintentos (notificaciones)

| Intento | Hora | Comportamiento |
|---|---|---|
| 1 | 20:15 (configurable) | Silencioso, reintentar |
| 2 | ~21:15 | Si falla, avisar y reintentar |
| 3 | ~22:00 | Si falla, notificar "no disponible esta noche" |

---

## Pantallas

### 1. Home (Precios Hoy/Mañana)
- Tabs o swipe: Hoy / Mañana
- Gráfico de barras horarias con colores por cuartil (verde → rojo)
- Tarjeta resumen: hora más barata, hora más cara, media del día
- Indicador de "datos no disponibles aún" para mañana antes de las 20:30
- Pull-to-refresh

### 2. Detalle de hora
- Al tocar una hora del gráfico
- Precio en EUR/kWh
- Comparación con la media del día (porcentaje above/below)
- Color según cuartil

### 3. Ajustes
- Toggle: Notificación "precios de mañana" (on/off)
- Toggle: Notificación "resumen del día" (on/off)
- Hora de notificación (time picker)
- Tema: Claro / Oscuro / Sistema
- Versión de la app

---

## Sistema de colores por cuartil

Mismo sistema que el bot de Telegram, adaptado a Material 3:

| Cuartil | Condición | Color Compose | Significado |
|---|---|---|---|
| Barato | precio ≤ P25 | `Green` | Hora más económica |
| Asequible | P25 < precio ≤ P50 | `Yellow` | Por debajo de la media |
| Medio | P50 < precio ≤ P75 | `Orange` | Por encima de la media |
| Caro | precio > P75 | `Red` | Hora más cara |

P25, P50, P75 = percentiles 25, 50 y 75 de los precios del día ordenados.

---

## Seguridad de la API key

```
local.properties  →  ESIOS_API_TOKEN=xxx
        ↓
build.gradle.kts  →  buildConfigField("String", "ESIOS_API_TOKEN", ...)
        ↓
Kotlin code       →  BuildConfig.ESIOS_API_TOKEN
```

- `local.properties` está en `.gitignore` (por defecto en Android)
- Se proporciona `local.properties.template` con el formato esperado
- El token es de lectura pública (PVPC es dato público), no es un secret de pago
- En el futuro, si se necesita más seguridad, se puede añadir un backend proxy

---

## Sprints

### Sprint 1 — Fundamentos (semana 1)

**Objetivo:** Proyecto base compilando con integración API funcional.

- [ ] Crear proyecto Android Studio (Kotlin, Compose, Hilt, Room, Retrofit)
- [ ] Configurar `build.gradle.kts` con todas las dependencias
- [ ] Configurar `secrets-gradle-plugin` para API key
- [ ] Crear modelos de datos: Room entities, network DTOs, domain models
- [ ] Implementar Retrofit API interface para `esios.ree.es/indicators/1001`
- [ ] Implementar `PriceRepository` con caché Room (offline-first)
- [ ] Implementar conversión EUR/MWh → EUR/kWh
- [ ] Implementar filtro geo_id 8741 (Península)
- [ ] Implementar lógica hoy/mañana (switch a las 20:00)
- [ ] Test unitario: parsing de respuesta JSON de ESIOS
- [ ] Test unitario: conversión de precios y cálculo de cuartiles

**Entregable:** App que arranca, llama a la API y almacena precios en Room.

---

### Sprint 2 — UI principal (semana 2)

**Objetivo:** Interfaz completa y funcional para ver precios.

- [ ] Configurar tema Material 3 (color scheme, tipografía, dark mode)
- [ ] `HomeScreen`: gráfico de barras horarias con colores por cuartil
- [ ] `HomeScreen`: tabs/swipe para Hoy / Mañana
- [ ] `HomeScreen`: tarjeta resumen (más barato, más caro, media)
- [ ] `HomeScreen`: indicador "datos no disponibles" para mañana
- [ ] `HomeScreen`: pull-to-refresh
- [ ] `HomeViewModel`: StateFlow con estados Loading/Success/Error/Empty
- [ ] Detalle de hora al tocar (bottom sheet o card expandible)
- [ ] Navegación: Home ↔ Ajustes (Navigation Compose)
- [ ] Preview de Compose para cada componente

**Entregable:** App visualmente completa, se pueden ver precios hoy y mañana.

---

### Sprint 3 — Notificaciones y ajustes (semana 3)

**Objetivo:** Notificaciones locales funcionales y pantalla de ajustes.

- [ ] `PriceSyncWorker`: WorkManager para sincronizar precios
- [ ] `NotificationWorker`: notificación "precios de mañana" (~20:30)
- [ ] Sistema de reintentos (3 intentos: 20:15, 21:15, 22:00)
- [ ] Notificación "resumen del día" (configurable, por defecto 8:00)
- [ ] Canal de notificaciones (NotificationChannel) para Android 8+
- [ ] `SettingsScreen`: toggles de notificaciones
- [ ] `SettingsScreen`: time picker para hora de notificación
- [ ] `SettingsScreen`: selector de tema (claro/oscuro/sistema)
- [ ] DataStore para persistir preferencias de usuario
- [ ] Permisos: `POST_NOTIFICATIONS` (Android 13+)
- [ ] Permisos: `SCHEDULE_EXACT_ALARM` (Android 12+)

**Entregable:** App con notificaciones configurables y ajustes completos.

---

### Sprint 4 — Pulido y publicación (semana 4)

**Objetivo:** App lista para Google Play.

- [ ] Animaciones de transición entre pantallas
- [ ] Animaciones en el gráfico de barras (entrada)
- [ ] Pantalla de error (sin red, API caída, datos no disponibles)
- [ ] Empty state (primera apertura sin datos)
- [ ] Splash screen (API 31+ SplashScreen)
- [ ] Icono de app (adaptive icon, todos los tamaños)
- [ ] Build release con ProGuard/R8
- [ ] Firmado de APK/AAB con keystore
- [ ] Preparación Google Play Console: ficha de Play Store, capturas, descripción
- [ ] Testing en múltiples tamaños de pantalla (phone, tablet)
- [ ] Testing en Android 8.0 (minSdk) y Android 15 (targetSdk)
- [ ] Accesibilidad: content descriptions, contraste de colores

**Entregable:** AAB firmado listo para subir a Google Play.

---

### Sprint 5 — Widget de inicio (futuro)

**Objetivo:** Widget de home screen con precio actual.

- [ ] Glance Compose widget (precio actual, hora, tendencia)
- [ ] Widget configurable: estilo (compacto/extendido), transparencia
- [ ] Actualización periódica del widget (WorkManager)
- [ ] Preview del widget en el selector de widgets
- [ ] Deep tap: abrir la app al tocar el widget

**Entregable:** Widget funcional publicado en actualización de la app.

---

### Sprint 6 — Selector de zona (futuro)

**Objetivo:** Soporte para todas las zonas geográficas de PVPC.

- [ ] Selector de zona en Ajustes (Península, Canarias, Baleares, Ceuta, Melilla)
- [ ] Mapeo geo_id → nombre de zona
- [ ] Persistir zona seleccionada en DataStore
- [ ] Actualizar repository para filtrar por zona seleccionada

**Entregable:** App soporta las 5 zonas geográficas de PVPC.

---

## Notas operativas

### Deploy en VPS 7 (referencia)

El bot de Telegram PrecioLuz ya corre en VPS 7 (`143.47.51.56`) en el contenedor `precioluz-precioluz-1`. La app Android es independiente y no requiere deploy en servidor — los usuarios la instalan desde Google Play.

### Rate limit REE

La política de caché offline-first (1 llamada API por día y tipo de dato) cumple con los términos de uso de REE. WorkManager respeta los períodos de reposo de la API.

### Zona horaria

Toda la lógica de fechas usa `Europe/Madrid` (CET/CEST). Los horarios de notificación y disponibilidad de datos están en hora peninsular española.
