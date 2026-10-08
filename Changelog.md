# Changelog.md — PrecioLuz App

Todos los cambios notables del proyecto se documentan aquí.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-1.1.0/),
y el proyecto sigue [Semantic Versioning](https://semver.org/lang/es/).

---

## [Unreleased]

### Corregido
- Placeholder todo-ceros de REE ("día no publicado"): ya no se sirve ni se guarda en caché; se trae el día real en cuanto se publica (`isPlaceholderDay` + tests)

### Añadido
- Planificación inicial del proyecto (Plan.md, Sprints 1-6)
- Definición de stack técnico: Kotlin + Jetpack Compose + Material 3
- Diseño de arquitectura: MVVM + StateFlow + offline-first (Room)
- Integración API REE ESIOS (indicador 1001, PVPC 2.0TD)
- Diseño de pantallas: Home, Detalle hora, Ajustes
- Diseño de notificaciones locales vía WorkManager
- Sistema de colores por cuartil (verde → rojo)
- README.md con instrucciones de setup y build
