# Changelog.md — PrecioLuz App

Todos los cambios notables del proyecto se documentan aquí.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-1.1.0/),
y el proyecto sigue [Semantic Versioning](https://semver.org/lang/es/).

---

## [Unreleased]

### Cambiado
- Versión 1.1.0 (`versionCode` 3): permite actualizar sobre la build anterior firmada con la misma clave.
- Rediseño de la interfaz alineado con PrecioLuzWeb: tokens de color claros/oscuros,
  superficies sólidas con borde de 1 px, radio de 16 dp y tipografía Bold sobria.
- Aurora reducida a un halo muy suave (opacidad ~0,3 y ciclo de 28 s) en ambos temas.
- Colores de tramo con significado de precio: valle verde, llano ámbar, punta rojo.
- Gráfico con línea de media, franja de la mejor ventana, tira de tramos,
  arrastre para inspeccionar horas y mínima/máxima al pie.
- La píldora flotante Hoy/Mañana y las tarjetas de mínima/media/máxima se conservan.

### Añadido
- Selector de duración 1 h / 2 h / 3 h para la franja más barata, con el ahorro
  porcentual frente a empezar ahora.
- Aviso «% por encima/debajo de la media de hoy» en la tarjeta principal.
- Notificaciones locales con WorkManager: precios de mañana (~20:15 h) y resumen
  del día (08:00 h), con permiso `POST_NOTIFICATIONS` y ajustes persistentes.
- Panel de ajustes real en un `ModalBottomSheet` (sustituye al diálogo vacío).
- Funciones puras y tests para ventanas baratas, textos de media/ahorro y avisos.

### Corregido
- El valle en tema oscuro era gris y apenas se veía: ahora es verde vivo.
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
