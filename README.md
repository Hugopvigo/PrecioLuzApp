<div align="center">

# ⚡ PrecioLuz App

### *El precio de la luz en tu bolsillo*

![Android](https://img.shields.io/badge/Android_26%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin_2.x-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![License](https://img.shields.io/badge/License-CC%20BY--NC--SA%204.0-blue?style=for-the-badge)

---

*App Android nativa que muestra los precios horarios del PVPC*
*publicados por REE/ESIOS, con Material 3 y dynamic color.*

<br>

```
╭──────────────────────────────────────────╮
│ ⚡ PrecioLuz App                         │
│                                          │
│    €/kWh                                 │
│   ┌─────┐                                │
│   │0.142│  ● Caro · Punta                │
│   └─────┘  ↑ Sube a las 15:00            │
│                                          │
│   💰 Min: 0.061    💀 Max: 0.201         │
│                                          │
│   [ Hoy ]  [ Mañana ]                    │
╰──────────────────────────────────────────╯
```

</div>

---

## ✨ Características

| | |
|---|---|
| 📊 | **Gráfico 24h** — barras interactivas con colores por cuartil (verde → rojo) |
| 💰 | **Mejor hora** — te dice cuándo enchufar para ahorrar |
| ⚠️ | **Peor hora** — evita los picos de precio |
| 🔔 | **Notificaciones** — precios de mañana ~20:30 y resumen diario a las 08:00 |
| ⚡ | **Offline-first** — Room cache, solo 1 llamada API por día |
| 🌗 | **Dark / Light / Auto** — dynamic color (Material You) |
| 📱 | **Material 3** — diseño nativo con dynamic color |

---

## 🛠️ Tech Stack

```
╭──────────────────────────────────────────╮
│  🎨  Jetpack Compose   · UI declarativa  │
│  🟣  Kotlin 2.x        · Lenguaje        │
│  🏗️  MVVM + StateFlow  · Arquitectura    │
│  🗄️  Room              · SQLite cache    │
│  🌐  Retrofit + OkHttp  · HTTP client    │
│  🔧  Hilt              · DI              │
│  💾  DataStore          · Preferences    │
│  ⏰  WorkManager        · Background     │
│  📡  REE ESIOS API     · Datos oficiales │
╰──────────────────────────────────────────╯
```

---

## 📸 Vista previa

<div align="center">

| 🌙 Modo Oscuro | ☀️ Modo Claro |
|:--------------:|:-------------:|
| ![PrecioLuz Dark](src/PrecioLuzAppDark.jpg) | ![PrecioLuz Clear](src/PrecioLuzAppClear.jpg) |

</div>

---

## 🚀 Uso rápido

### Requisitos

- Android Studio Hedgehog o superior
- JDK 17+
- Android SDK 35

### API Key

```bash
# Copia el template
cp local.properties.template local.properties

# Añade tu token
echo "ESIOS_API_TOKEN=tu_token_aqui" >> local.properties
```

> 🔑 Solicítala en `api_token@ree.es`. `local.properties` está en `.gitignore`.

### Build

```bash
# Debug
./gradlew assembleDebug

# Release
./gradlew assembleRelease
```

---

## 📡 API — Red Eléctrica de España

La app usa el indicador **1001** (PVPC 2.0TD) de la API de REE ESIOS.

| | |
|---|---|
| Endpoint | `GET https://api.esios.ree.es/indicators/1001` |
| Auth | Header `x-api-key: <ESIOS_API_TOKEN>` |
| Zona | `geo_id = 8741` (Península) |
| Conversión | EUR/MWh ÷ 1000 = EUR/kWh |
| Datos mañana | Disponibles ~20:30 CET |

---

## 🔔 Notificaciones

| | |
|---|---|
| 🌙 | **Precios de mañana** — ~20:30 CET, con retries |
| ☀️ | **Resumen del día** — 08:00 CET, hora más barata + media |

Se activan/desactivan desde **Ajustes**. No requieren servidor ni Firebase.

---

## 📂 Estructura

```
app/src/main/java/com/precioluz/app/
├── 📁  data/
│   ├── 🗄️  local/          · Room DAOs, entities, DataStore
│   ├── 🌐  api/            · Retrofit interface, DTOs
│   └── 📦  repository/     · PrecioLuzRepository impl
├── 🎨  ui/
│   ├── 📱  viewmodel/      · PrecioLuzViewModel
│   ├── 🧩  components/     · PriceChart, HourList, SettingsDialog
│   └── 🎭  theme/          · Material 3 theme, color tiers
└── 🔧  di/                 · Hilt modules
```

---

## 🔗 Relación con PrecioLuz Web

Este proyecto es la **app Android** complementaria a [PrecioLuzWeb](https://github.com/Hugopvigo/PrecioLuzWeb).

La web muestra los precios en el navegador; la app los muestra en tu móvil con notificaciones push y cache offline.

Los colores, componentes y lógica de tiers son una traducción directa del código Kotlin.

---

## 🤝 Contribuir

1. 🍴 Fork el proyecto
2. 🌿 Crea una rama (`git checkout -b feature/nueva-feature`)
3. 💾 Commit (`git commit -m 'Add nueva feature'`)
4. 📤 Push (`git push origin feature/nueva-feature`)
5. 🔀 Abre un Pull Request

---

## 📄 Licencia

Este proyecto está bajo la licencia **CC BY-NC-SA 4.0** — véase el archivo [LICENSE](LICENSE) para más detalles.

---

<div align="center">

**CC BY-NC-SA 4.0** — Attribution-NonCommercial-ShareAlike

---

Desarrollado por **[Hugo Perez-Vigo](https://hugopvigo.es)** · [@hugopvigo](https://x.com/hugopvigo)

[![GitHub](https://img.shields.io/badge/GitHub-Hugopvigo-181717?style=for-the-badge&logo=github)](https://github.com/Hugopvigo)

</div>
