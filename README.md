# PrecioLuz App

App nativa Android para consultar los precios PVPC de la luz en España (Península), hoy y mañana.

## Características

- Precios horarios del día actual y del día siguiente
- Gráfico de barras con colores por cuartil (verde barato → rojo caro)
- Tarjeta resumen: hora más barata, más cara y media del día
- Notificaciones locales configurables (precios de mañana ~20:30, resumen diario)
- Tema Material 3 con soporte dark mode y dynamic color
- Offline-first: los datos se cachean en Room, solo 1 llamada API por día

## Stack técnico

| Capa | Tecnología |
|---|---|
| Lenguaje | Kotlin 2.x |
| UI | Jetpack Compose + Material 3 |
| Arquitectura | MVVM + StateFlow + offline-first (Room) |
| Red | Retrofit + OkHttp + kotlinx-serialization |
| DI | Hilt |
| Storage | Room (SQLite) + DataStore (prefs) |
| Background | WorkManager (notificaciones locales) |
| API | REE ESIOS `api.esios.ree.es/indicators/1001` |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 (Android 15) |

## API — Red Eléctrica de España (ESIOS)

La app usa el indicador **1001** (PVPC 2.0TD) de la API de REE ESIOS.

- **Endpoint:** `GET https://api.esios.ree.es/indicators/1001`
- **Auth:** header `x-api-key: <ESIOS_API_TOKEN>`
- **Params:** `start_date`, `end_date` (ISO 8601 Madrid), `time_trunc=hour`
- **Zona:** `geo_id = 8741` (Península)
- **Conversión:** EUR/MWh ÷ 1000 = EUR/kWh
- **Disponibilidad datos mañana:** ~20:30 CET

Se requiere una API key personal de REE. Solicítala en `api_token@ree.es`.

## Configuración del entorno

### Requisitos

- Android Studio Hedgehog o superior
- JDK 17+
- Android SDK 35

### API Key

1. Copia el template de propiedades:

```bash
cp local.properties.template local.properties
```

2. Edita `local.properties` y añade tu token:

```properties
ESIOS_API_TOKEN=tu_token_aqui
```

> `local.properties` está en `.gitignore` y **nunca** se commitea al repo.

### Build & Run

```bash
# Debug
./gradlew assembleDebug

# Release
./gradlew assembleRelease
```

## Estructura del proyecto

```
app/src/main/java/com/precioluz/app/
├── data/
│   ├── local/          # Room DAOs, entities, DataStore
│   ├── network/        # Retrofit interface, DTOs
│   └── repository/     # PriceRepository impl
├── domain/
│   ├── model/          # PriceHour, PriceDay
│   └── usecase/        # GetPricesUseCase
├── ui/
│   ├── home/           # HomeScreen, HomeViewModel
│   ├── settings/       # SettingsScreen, SettingsViewModel
│   └── theme/          # Material 3 theme, color tiers
├── worker/             # PriceSyncWorker, NotificationWorker
└── di/                 # Hilt modules
```

## Notificaciones locales

| Notificación | Hora por defecto | Descripción |
|---|---|---|
| Precios de mañana | ~20:30 CET | Aviso cuando se publican los precios del día siguiente (con retries a 21:15 y 22:00) |
| Resumen del día | 08:00 CET | Resumen con la hora más barata y la media del día |

Ambas se pueden activar/desactivar desde Ajustes. No requieren servidor ni Firebase.

## Pantallas

1. **Home** — Gráfico de barras horarias (hoy/mañana) con colores por cuartil. Tarjeta resumen.
2. **Detalle hora** — Precio desglosado y comparación con la media al tocar una hora.
3. **Ajustes** — Toggles de notificaciones, hora de notificación, tema (claro/oscuro/sistema).

## Proyecto relacionado

- [PrecioLuz Bot](https://github.com/hugopvigo/PrecioLuz) — Bot de Telegram con la misma lógica de API y formato de precios.

## Licencia

CC BY-NC-SA 4.0
