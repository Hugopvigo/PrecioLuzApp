# PrecioLuz — Web App + API + Android Cache

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Crear PrecioLuzWeb (backend FastAPI + frontend React) en el servidor VPS 7 que obtiene precios PVPC de ESIOS, los almacena en SQLite y los sirve via `/api/precios` con rate limiting. La app Android consume ese endpoint con caché Room local, eliminando la API key del dispositivo.

**Architecture:**
- **Servidor**: FastAPI (port 8000, interno) + Nginx (port 8081, expuesto) + APScheduler (reintentos 20:15/20:45/21:30 Madrid). Nginx sirve el frontend React y hace proxy de `/api/` al backend. Apache añade vhost `precioluz.hugopvigo.es → 8081`.
- **App Android**: `PrecioLuzRepository` cache-first en Room (PK compuesta `date+hour`). Si no hay datos, llama a `https://precioluz.hugopvigo.es/api/precios` — una sola llamada devuelve hoy y mañana. Sin API key.

**Tech Stack:** Python 3.12 · FastAPI · APScheduler · aiosqlite · slowapi (rate limiting) · React 19 · Vite · Tailwind CSS 4 · Kotlin · Room 2.x · OkHttp · Hilt

---

## Mapa de archivos

### Servidor — `/opt/PrecioLuzWeb/`
| Archivo | Acción | Rol |
|---------|--------|-----|
| `server/main.py` | Crear | FastAPI app + rate limiting + endpoints |
| `server/esios.py` | Crear | Fetch ESIOS (adaptado del bot) |
| `server/db.py` | Crear | SQLite via aiosqlite |
| `server/scheduler.py` | Crear | APScheduler: cron 06:05 + reintentos nocturnos |
| `server/requirements.txt` | Crear | Dependencias Python |
| `web/src/App.tsx` | Crear | Layout principal + tabs Hoy/Mañana |
| `web/src/types.ts` | Crear | HourPrice, DayPrices, PriceTier (réplica del dominio Android) |
| `web/src/components/AuroraBackground.tsx` | Crear | Fondo animado (réplica app) |
| `web/src/components/GlassCard.tsx` | Crear | Glass morphism card (réplica app) |
| `web/src/components/HeroPriceCard.tsx` | Crear | Precio actual + tier glow (réplica app) |
| `web/src/components/StatRow.tsx` | Crear | Min/Avg/Max cards (réplica app) |
| `web/src/components/HourList.tsx` | Crear | 24 filas con barra, emojis, colores (réplica app) |
| `web/src/components/LiveIndicator.tsx` | Crear | Punto verde pulsante (réplica app) |
| `web/src/hooks/usePrices.ts` | Crear | React Query fetch + auto-refresh |
| `web/package.json` | Crear | Dependencias frontend |
| `web/vite.config.ts` | Crear | Proxy `/api` → backend en dev |
| `docker/docker-compose.yml` | Crear | Servicios: api + web |
| `docker/Dockerfile.api` | Crear | Python FastAPI |
| `docker/Dockerfile.web` | Crear | Build React → Nginx |
| `docker/Dockerfile.dev-api` | Crear | Python uvicorn --reload |
| `docker/Dockerfile.dev-web` | Crear | Vite dev server (port 3001) |
| `docker/nginx.conf` | Crear | Static files + proxy /api/ → api:8000 |
| `.env.example` | Crear | Variables requeridas |
| `package.json` | Crear | Workspace root |

### Apache VPS 7
| Archivo | Acción |
|---------|--------|
| `/etc/apache2/sites-available/precioluz.hugopvigo.conf` | Crear |
| `/etc/apache2/sites-enabled/precioluz.hugopvigo.conf` | Symlink |

### Android app (`app/src/main/java/com/precioluz/app/`)
| Archivo | Acción | Rol |
|---------|--------|-----|
| `data/local/PriceEntity.kt` | Modificar | PK compuesta `(date, hour)` |
| `data/local/PrecioLuzDatabase.kt` | Modificar | version = 2 + fallbackToDestructiveMigration |
| `data/local/PriceDao.kt` | Modificar | Añadir `deletePricesOlderThan` |
| `data/api/PrecioLuzJsonDto.kt` | Crear | DTOs del JSON del servidor |
| `data/api/PrecioLuzJsonApi.kt` | Crear | Cliente OkHttp → `/api/precios` |
| `data/repository/PrecioLuzRepository.kt` | Modificar | Cache-first Room + fallback servidor |
| `ui/viewmodel/PrecioLuzViewModel.kt` | Modificar | Eliminar obligatoriedad de API key |
| `ui/components/SettingsDialog.kt` | Modificar | API key opcional, no bloquea la app |
| `MainActivity.kt` | Modificar | Limpiar parámetros de API key |

---

## Subsistema A — Servidor PrecioLuzWeb

### Task 1: Estructura base del proyecto

**Files:**
- Create: `/opt/PrecioLuzWeb/` (directorio completo)

- [ ] **Paso 1: Crear estructura en el servidor**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "
  mkdir -p /opt/PrecioLuzWeb/{server,web/src/{components,hooks},docker,data} &&
  echo 'Estructura creada OK'
"
```

- [ ] **Paso 2: Crear `.env.example` y `.env`**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "cat > /opt/PrecioLuzWeb/.env.example << 'EOF'
ESIOS_API_TOKEN=tu_token_aqui
TZ=Europe/Madrid
EOF
# Copiar .env real tomando token del bot
grep ESIOS_API_TOKEN /opt/PrecioLuz/.env > /opt/PrecioLuzWeb/.env
echo 'TZ=Europe/Madrid' >> /opt/PrecioLuzWeb/.env
cat /opt/PrecioLuzWeb/.env
"
```

Salida esperada: dos líneas con `ESIOS_API_TOKEN=...` y `TZ=Europe/Madrid`

- [ ] **Paso 3: Crear `server/requirements.txt`**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "cat > /opt/PrecioLuzWeb/server/requirements.txt << 'EOF'
fastapi>=0.115,<1
uvicorn[standard]>=0.32,<1
aiohttp>=3.9,<4
aiosqlite>=0.20,<1
apscheduler>=3.10,<4
pytz>=2024.1
slowapi>=0.1.9,<1
EOF"
```

---

### Task 2: Backend Python — db.py + esios.py

**Files:**
- Create: `/opt/PrecioLuzWeb/server/db.py`
- Create: `/opt/PrecioLuzWeb/server/esios.py`

- [ ] **Paso 1: Crear `db.py`**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "cat > /opt/PrecioLuzWeb/server/db.py << 'PYEOF'
import aiosqlite
from pathlib import Path

DB_PATH = Path(\"/data/precioluz.db\")


async def init_db():
    async with aiosqlite.connect(DB_PATH) as db:
        await db.execute(\"\"\"
            CREATE TABLE IF NOT EXISTS prices (
                date     TEXT NOT NULL,
                hour     INTEGER NOT NULL,
                price_kwh REAL NOT NULL,
                PRIMARY KEY (date, hour)
            )
        \"\"\")
        await db.commit()


async def save_prices(date: str, prices: list[float]) -> None:
    \"\"\"Guarda lista de 24 precios €/kWh (índice = hora).\"\"\"
    async with aiosqlite.connect(DB_PATH) as db:
        await db.executemany(
            \"INSERT OR REPLACE INTO prices (date, hour, price_kwh) VALUES (?, ?, ?)\",
            [(date, hour, price) for hour, price in enumerate(prices)],
        )
        await db.commit()


async def get_prices(date: str) -> list[float] | None:
    \"\"\"Devuelve lista de 24 precios o None si no están completos.\"\"\"
    async with aiosqlite.connect(DB_PATH) as db:
        async with db.execute(
            \"SELECT price_kwh FROM prices WHERE date = ? ORDER BY hour ASC\", (date,)
        ) as cursor:
            rows = await cursor.fetchall()
    if len(rows) != 24:
        return None
    return [row[0] for row in rows]


async def delete_old_prices(before_date: str) -> None:
    async with aiosqlite.connect(DB_PATH) as db:
        await db.execute(\"DELETE FROM prices WHERE date < ?\", (before_date,))
        await db.commit()
PYEOF
echo OK"
```

- [ ] **Paso 2: Crear `esios.py`**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "cat > /opt/PrecioLuzWeb/server/esios.py << 'PYEOF'
import logging
import os
from datetime import datetime

import aiohttp
from pytz import timezone

logger = logging.getLogger(\"precioluz.esios\")

ESIOS_URL = \"https://api.esios.ree.es/indicators/1001\"
GEO_PENINSULA = 8741
MADRID_TZ = timezone(\"Europe/Madrid\")
MAX_RETRIES = 3
RETRY_DELAY_S = 8


async def fetch_day_prices(date: str) -> list[float] | None:
    \"\"\"Devuelve lista de 24 precios €/kWh (índice = hora) o None si fallan todos los reintentos.\"\"\"
    token = os.getenv(\"ESIOS_API_TOKEN\")
    if not token:
        logger.critical(\"ESIOS_API_TOKEN no configurado\")
        return None

    local_start = MADRID_TZ.localize(
        datetime.strptime(date, \"%Y-%m-%d\").replace(hour=0, minute=0, second=0)
    )
    local_end = local_start.replace(hour=23, minute=59, second=59)
    params = {
        \"start_date\": local_start.isoformat(),
        \"end_date\":   local_end.isoformat(),
        \"time_trunc\": \"hour\",
    }
    headers = {\"x-api-key\": token, \"Accept\": \"application/json\"}

    for attempt in range(1, MAX_RETRIES + 1):
        try:
            async with aiohttp.ClientSession() as session:
                async with session.get(
                    ESIOS_URL, headers=headers, params=params,
                    timeout=aiohttp.ClientTimeout(total=20),
                ) as resp:
                    if resp.status == 200:
                        data = await resp.json()
                        prices = _parse(data)
                        if prices:
                            return prices
                        logger.warning(\"Respuesta incompleta para %s (intento %d/%d)\", date, attempt, MAX_RETRIES)
                    else:
                        logger.warning(\"ESIOS HTTP %d para %s (intento %d/%d)\", resp.status, date, attempt, MAX_RETRIES)
        except Exception:
            logger.exception(\"Error de red para %s (intento %d/%d)\", date, attempt, MAX_RETRIES)

        if attempt < MAX_RETRIES:
            import asyncio
            await asyncio.sleep(RETRY_DELAY_S)

    return None


def _parse(data: dict) -> list[float] | None:
    try:
        values = data[\"indicator\"][\"values\"]
    except (KeyError, TypeError):
        return None

    bucket: dict[int, float] = {}
    for v in values:
        if v.get(\"geo_id\") != GEO_PENINSULA:
            continue
        try:
            hour_str = v.get(\"time_interval\", {}).get(\"start\", \"\") or v.get(\"datetime\", \"\")
            hour_num = datetime.fromisoformat(hour_str).astimezone(MADRID_TZ).hour
            bucket[hour_num] = round(v[\"value\"] / 1000, 5)   # €/MWh → €/kWh
        except Exception:
            continue

    if len(bucket) != 24:
        return None
    return [bucket[h] for h in range(24)]
PYEOF
echo OK"
```

---

### Task 3: Backend Python — scheduler.py + main.py

**Files:**
- Create: `/opt/PrecioLuzWeb/server/scheduler.py`
- Create: `/opt/PrecioLuzWeb/server/main.py`

- [ ] **Paso 1: Crear `scheduler.py`**

Los precios de hoy se actualizan a las 06:05 (por si cambian por ajustes de liquidación). Los de mañana se intentan a las 20:15, 20:45 y 21:30 hora Madrid.

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "cat > /opt/PrecioLuzWeb/server/scheduler.py << 'PYEOF'
import logging
from datetime import datetime, timedelta

from apscheduler.schedulers.asyncio import AsyncIOScheduler
from pytz import timezone

from .db import save_prices, delete_old_prices
from .esios import fetch_day_prices

logger = logging.getLogger(\"precioluz.scheduler\")
MADRID_TZ = timezone(\"Europe/Madrid\")


async def _fetch_and_store(date: str, label: str) -> bool:
    prices = await fetch_day_prices(date)
    if prices:
        await save_prices(date, prices)
        logger.info(\"%s: precios de %s almacenados\", label, date)
        return True
    logger.warning(\"%s: precios de %s no disponibles\", label, date)
    return False


async def job_today():
    today = datetime.now(MADRID_TZ).strftime(\"%Y-%m-%d\")
    yesterday = (datetime.now(MADRID_TZ) - timedelta(days=2)).strftime(\"%Y-%m-%d\")
    await _fetch_and_store(today, \"job_today\")
    await delete_old_prices(yesterday)


async def job_tomorrow(attempt: int):
    tomorrow = (datetime.now(MADRID_TZ) + timedelta(days=1)).strftime(\"%Y-%m-%d\")
    ok = await _fetch_and_store(tomorrow, f\"job_tomorrow(attempt={attempt})\")
    if not ok:
        logger.info(\"Intento %d/%d fallido para precios de mañana\", attempt, 3)


def setup_scheduler() -> AsyncIOScheduler:
    scheduler = AsyncIOScheduler(timezone=MADRID_TZ)

    # Actualiza precios de hoy cada mañana
    scheduler.add_job(job_today, \"cron\", hour=6, minute=5, id=\"today\")

    # 3 intentos para precios de mañana
    scheduler.add_job(job_tomorrow, \"cron\", hour=20, minute=15, args=[1], id=\"tomorrow_1\")
    scheduler.add_job(job_tomorrow, \"cron\", hour=20, minute=45, args=[2], id=\"tomorrow_2\")
    scheduler.add_job(job_tomorrow, \"cron\", hour=21, minute=30, args=[3], id=\"tomorrow_3\")

    return scheduler
PYEOF
echo OK"
```

- [ ] **Paso 2: Crear `main.py`**

Rate limiting: 20 req/min por IP en `/api/precios`. Límite más estricto (5/min) en `/api/health` para evitar abuso. Se usa `X-Real-IP` (Nginx lo inyecta) para que el rate limit funcione con la IP real del cliente, no la interna de Docker.

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "cat > /opt/PrecioLuzWeb/server/main.py << 'PYEOF'
import logging
import os
from contextlib import asynccontextmanager
from datetime import datetime, timedelta

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from pytz import timezone
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.errors import RateLimitExceeded

from .db import init_db, get_prices
from .esios import fetch_day_prices
from .db import save_prices
from .scheduler import setup_scheduler

logging.basicConfig(level=os.getenv(\"LOG_LEVEL\", \"INFO\"),
                    format=\"%(asctime)s [%(levelname)s] %(name)s: %(message)s\")
logger = logging.getLogger(\"precioluz\")

MADRID_TZ = timezone(\"Europe/Madrid\")


def _real_ip(request: Request) -> str:
    \"\"\"Lee X-Real-IP inyectado por Nginx; fallback a la IP de conexión directa.\"\"\"
    return (
        request.headers.get(\"X-Real-IP\")
        or request.headers.get(\"X-Forwarded-For\", \"\").split(\",\")[0].strip()
        or (request.client.host if request.client else \"unknown\")
    )


limiter = Limiter(key_func=_real_ip)


@asynccontextmanager
async def lifespan(app: FastAPI):
    await init_db()
    # Precarga precios de hoy al arrancar si no están en DB
    today = datetime.now(MADRID_TZ).strftime(\"%Y-%m-%d\")
    if not await get_prices(today):
        prices = await fetch_day_prices(today)
        if prices:
            await save_prices(today, prices)
    scheduler = setup_scheduler()
    scheduler.start()
    yield
    scheduler.shutdown()


app = FastAPI(title=\"PrecioLuz API\", docs_url=None, redoc_url=None)
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)


def _build_day(date: str, prices: list[float]) -> dict:
    return {\"date\": date, \"prices\": prices}


@app.get(\"/api/precios\")
@limiter.limit(\"20/minute\")
async def get_precios(request: Request):
    now = datetime.now(MADRID_TZ)
    today_str    = now.strftime(\"%Y-%m-%d\")
    tomorrow_str = (now + timedelta(days=1)).strftime(\"%Y-%m-%d\")

    today_prices    = await get_prices(today_str)
    tomorrow_prices = await get_prices(tomorrow_str)

    if not today_prices:
        return JSONResponse({\"error\": \"datos no disponibles\"}, status_code=503)

    published_at = now.replace(hour=20, minute=15, second=0, microsecond=0)
    show_tomorrow = now >= published_at and tomorrow_prices is not None

    payload = {
        \"updated_at\": now.isoformat(),
        \"today\":    _build_day(today_str, today_prices),
        \"tomorrow\": _build_day(tomorrow_str, tomorrow_prices) if show_tomorrow else None,
    }
    response = JSONResponse(payload)
    response.headers[\"Cache-Control\"] = \"public, max-age=1800\"
    return response


@app.get(\"/api/health\")
@limiter.limit(\"5/minute\")
async def health(request: Request):
    return {\"status\": \"ok\"}
PYEOF
echo OK"
```

- [ ] **Paso 3: Crear `__init__.py` del paquete server**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "touch /opt/PrecioLuzWeb/server/__init__.py && echo OK"
```

---

### Task 4: Docker — Dockerfiles + nginx + compose

**Files:**
- Create: `/opt/PrecioLuzWeb/docker/`

- [ ] **Paso 1: Crear `Dockerfile.api` (producción)**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "cat > /opt/PrecioLuzWeb/docker/Dockerfile.api << 'EOF'
FROM python:3.12-slim
WORKDIR /app
COPY server/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY server/ ./server/
EXPOSE 8000
CMD ["uvicorn", "server.main:app", "--host", "0.0.0.0", "--port", "8000", "--proxy-headers", "--forwarded-allow-ips=*"]
EOF
echo OK"
```

- [ ] **Paso 2: Crear `nginx.conf`**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "cat > /opt/PrecioLuzWeb/docker/nginx.conf << 'EOF'
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml image/svg+xml;
    gzip_min_length 256;

    # Inyectar IP real del cliente para rate limiting en FastAPI
    location /api/ {
        proxy_pass         http://api:8000;
        proxy_set_header   Host              $host;
        proxy_set_header   X-Real-IP         $remote_addr;
        proxy_set_header   X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_read_timeout 30s;
    }

    location ~* \.(js|css|png|jpg|svg|ico|woff2)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
EOF
echo OK"
```

- [ ] **Paso 3: Crear `Dockerfile.web` (producción)**

Primero construye React, luego copia a Nginx. El build necesita la URL de API vacía — en producción el frontend usa rutas relativas `/api/`.

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "cat > /opt/PrecioLuzWeb/docker/Dockerfile.web << 'EOF'
FROM node:20-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json* ./
COPY web/package.json ./web/
RUN npm ci
COPY web/ ./web/
RUN npm run build -w web

FROM nginx:alpine
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/web/dist /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost/ || exit 1
CMD ["nginx", "-g", "daemon off;"]
EOF
echo OK"
```

- [ ] **Paso 4: Crear `docker-compose.yml`**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "cat > /opt/PrecioLuzWeb/docker/docker-compose.yml << 'EOF'
services:
  api:
    build:
      context: ..
      dockerfile: docker/Dockerfile.api
    env_file: ../.env
    volumes:
      - ../data:/data
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "python3 -c \"import urllib.request; urllib.request.urlopen('http://localhost:8000/api/health')\""]
      interval: 30s
      timeout: 5s
      retries: 3

  web:
    build:
      context: ..
      dockerfile: docker/Dockerfile.web
    ports:
      - "8081:80"
    depends_on:
      api:
        condition: service_healthy
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "wget -qO- http://127.0.0.1/ || exit 1"]
      interval: 30s
      timeout: 3s
      retries: 3
EOF
echo OK"
```

---

### Task 5: Frontend React — tipos + componentes de diseño

**Files:**
- Create: `/opt/PrecioLuzWeb/web/src/types.ts`
- Create: `/opt/PrecioLuzWeb/web/src/components/AuroraBackground.tsx`
- Create: `/opt/PrecioLuzWeb/web/src/components/GlassCard.tsx`
- Create: `/opt/PrecioLuzWeb/web/src/components/LiveIndicator.tsx`

Los colores replican exactamente los de la app (Color.kt).

- [ ] **Paso 1: Crear `web/package.json` y `package.json` raíz**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "
cat > /opt/PrecioLuzWeb/package.json << 'EOF'
{
  \"name\": \"precioluz-web\",
  \"version\": \"1.0.0\",
  \"private\": true,
  \"workspaces\": [\"web\"],
  \"scripts\": {
    \"dev\": \"npm run dev -w web\",
    \"build\": \"npm run build -w web\"
  }
}
EOF

cat > /opt/PrecioLuzWeb/web/package.json << 'EOF'
{
  \"name\": \"@precioluz/web\",
  \"private\": true,
  \"version\": \"1.0.0\",
  \"type\": \"module\",
  \"scripts\": {
    \"dev\": \"vite\",
    \"build\": \"tsc -b && vite build\",
    \"preview\": \"vite preview\"
  },
  \"dependencies\": {
    \"@tanstack/react-query\": \"^5.100.6\",
    \"lucide-react\": \"^1.14.0\",
    \"react\": \"^19.2.5\",
    \"react-dom\": \"^19.2.5\"
  },
  \"devDependencies\": {
    \"@tailwindcss/vite\": \"^4.1.8\",
    \"@types/react\": \"^19.2.14\",
    \"@types/react-dom\": \"^19.2.3\",
    \"@vitejs/plugin-react\": \"^6.0.1\",
    \"tailwindcss\": \"^4.1.8\",
    \"typescript\": \"~6.0.2\",
    \"vite\": \"^8.0.10\"
  }
}
EOF
echo OK"
```

- [ ] **Paso 2: Crear `vite.config.ts`**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "cat > /opt/PrecioLuzWeb/web/vite.config.ts << 'EOF'
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      '/api': 'http://localhost:8000',
    },
  },
})
EOF
echo OK"
```

- [ ] **Paso 3: Crear `web/src/types.ts`**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "cat > /opt/PrecioLuzWeb/web/src/types.ts << 'EOF'
export type PriceTier = 'GREEN' | 'YELLOW' | 'ORANGE' | 'RED'
export type Tramo = 'VALLE' | 'LLANO' | 'PUNTA'

export interface HourPrice {
  hour: number
  priceKwh: number
  tier: PriceTier
  tramo: Tramo
  isMin: boolean
  isMax: boolean
}

export interface DayPrices {
  date: string
  hours: HourPrice[]
  avg: number
  min: HourPrice
  max: HourPrice
  bestWindow: [number, number]
  worstWindow: [number, number]
}

// Respuesta cruda del servidor
export interface ApiResponse {
  updated_at: string
  today: { date: string; prices: number[] }
  tomorrow: { date: string; prices: number[] } | null
}
EOF
echo OK"
```

- [ ] **Paso 4: Crear utilidades de dominio `web/src/utils.ts`**

Replica la lógica de `PrecioLuzRepository.buildDayPrices` y `PriceTier.colorDark/Light`.

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "cat > /opt/PrecioLuzWeb/web/src/utils.ts << 'EOF'
import type { DayPrices, HourPrice, PriceTier, Tramo } from './types'

export const TIER_COLORS_DARK: Record<PriceTier, string> = {
  GREEN:  '#30D158',
  YELLOW: '#FFE433',
  ORANGE: '#FF7020',
  RED:    '#FF3B30',
}

export const TIER_COLORS_LIGHT: Record<PriceTier, string> = {
  GREEN:  '#15A34A',
  YELLOW: '#B89500',
  ORANGE: '#D95200',
  RED:    '#CC1624',
}

export const TIER_LABELS: Record<PriceTier, string> = {
  GREEN:  'Barato',
  YELLOW: 'Moderado',
  ORANGE: 'Caro',
  RED:    'Muy caro',
}

export const TRAMO_LABELS: Record<Tramo, string> = {
  VALLE: 'Valle',
  LLANO: 'Llano',
  PUNTA: 'Punta',
}

function tramoForHour(hour: number, dateStr: string): Tramo {
  const dow = new Date(dateStr + 'T12:00:00').getDay() // 0=Dom, 6=Sab
  if (dow === 0 || dow === 6) return 'VALLE'
  if ((hour >= 10 && hour <= 13) || (hour >= 18 && hour <= 21)) return 'PUNTA'
  if (hour < 8) return 'VALLE'
  return 'LLANO'
}

export function buildDayPrices(date: string, rawPrices: number[]): DayPrices {
  const sorted = [...rawPrices.map((p, h) => ({ h, p }))].sort((a, b) => a.p - b.p)
  const tierMap = new Map<number, PriceTier>()
  const tiers: PriceTier[] = ['GREEN', 'GREEN', 'GREEN', 'GREEN', 'GREEN', 'GREEN',
                               'YELLOW', 'YELLOW', 'YELLOW', 'YELLOW', 'YELLOW', 'YELLOW',
                               'ORANGE', 'ORANGE', 'ORANGE', 'ORANGE', 'ORANGE', 'ORANGE',
                               'RED', 'RED', 'RED', 'RED', 'RED', 'RED']
  sorted.forEach(({ h }, rank) => tierMap.set(h, tiers[rank]))

  const hours: HourPrice[] = rawPrices.map((priceKwh, hour) => ({
    hour,
    priceKwh,
    tier: tierMap.get(hour)!,
    tramo: tramoForHour(hour, date),
    isMin: false,
    isMax: false,
  }))

  const minH = hours.reduce((a, b) => a.priceKwh < b.priceKwh ? a : b)
  const maxH = hours.reduce((a, b) => a.priceKwh > b.priceKwh ? a : b)
  hours[minH.hour].isMin = true
  hours[maxH.hour].isMax = true

  const bestStart = Array.from({ length: 23 }, (_, i) => i)
    .reduce((best, i) => hours[i].priceKwh + hours[i + 1].priceKwh < hours[best].priceKwh + hours[best + 1].priceKwh ? i : best, 0)
  const worstStart = Array.from({ length: 23 }, (_, i) => i)
    .reduce((worst, i) => hours[i].priceKwh + hours[i + 1].priceKwh > hours[worst].priceKwh + hours[worst + 1].priceKwh ? i : worst, 0)

  return {
    date,
    hours,
    avg: rawPrices.reduce((s, p) => s + p, 0) / 24,
    min: hours[minH.hour],
    max: hours[maxH.hour],
    bestWindow:  [bestStart, bestStart + 1],
    worstWindow: [worstStart, worstStart + 1],
  }
}
EOF
echo OK"
```

- [ ] **Paso 5: Crear `AuroraBackground.tsx`**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "cat > /opt/PrecioLuzWeb/web/src/components/AuroraBackground.tsx << 'EOF'
interface Props { dark: boolean }

export default function AuroraBackground({ dark }: Props) {
  return (
    <div className=\"fixed inset-0 -z-10 overflow-hidden\" style={{ background: dark ? '#080912' : '#E9EDF6' }}>
      {dark ? (
        <>
          <div className=\"aurora-blob\" style={{ background: 'radial-gradient(ellipse, #7C3AFF55 0%, transparent 70%)', top: '5%',  left: '20%', width: '55vw', height: '45vw', animationDelay: '0s' }} />
          <div className=\"aurora-blob\" style={{ background: 'radial-gradient(ellipse, #1E6BFF44 0%, transparent 70%)', top: '40%', left: '55%', width: '50vw', height: '40vw', animationDelay: '-5s' }} />
          <div className=\"aurora-blob\" style={{ background: 'radial-gradient(ellipse, #00D3A433 0%, transparent 70%)', top: '60%', left: '5%',  width: '45vw', height: '35vw', animationDelay: '-10s' }} />
          <div className=\"aurora-blob\" style={{ background: 'radial-gradient(ellipse, #FF2D8430 0%, transparent 70%)', top: '15%', left: '65%', width: '40vw', height: '30vw', animationDelay: '-15s' }} />
        </>
      ) : (
        <>
          <div className=\"aurora-blob\" style={{ background: 'radial-gradient(ellipse, #FFB3D166 0%, transparent 70%)', top: '5%',  left: '20%', width: '55vw', height: '45vw', animationDelay: '0s' }} />
          <div className=\"aurora-blob\" style={{ background: 'radial-gradient(ellipse, #B4D2FF55 0%, transparent 70%)', top: '40%', left: '55%', width: '50vw', height: '40vw', animationDelay: '-5s' }} />
          <div className=\"aurora-blob\" style={{ background: 'radial-gradient(ellipse, #B2FFE444 0%, transparent 70%)', top: '60%', left: '5%',  width: '45vw', height: '35vw', animationDelay: '-10s' }} />
          <div className=\"aurora-blob\" style={{ background: 'radial-gradient(ellipse, #FFECB055 0%, transparent 70%)', top: '15%', left: '65%', width: '40vw', height: '30vw', animationDelay: '-15s' }} />
        </>
      )}
    </div>
  )
}
EOF
echo OK"
```

Añadir la animación en el CSS global (`web/src/index.css`):

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "cat > /opt/PrecioLuzWeb/web/src/index.css << 'EOF'
@import 'tailwindcss';

@keyframes aurora-drift {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33%       { transform: translate(3vw, -4vh) scale(1.05); }
  66%       { transform: translate(-2vw, 3vh) scale(0.97); }
}

.aurora-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  animation: aurora-drift 25s ease-in-out infinite;
}
EOF
echo OK"
```

- [ ] **Paso 6: Crear `GlassCard.tsx`**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "cat > /opt/PrecioLuzWeb/web/src/components/GlassCard.tsx << 'EOF'
import { ReactNode } from 'react'

interface Props {
  children: ReactNode
  dark: boolean
  className?: string
  style?: React.CSSProperties
}

export default function GlassCard({ children, dark, className = '', style }: Props) {
  return (
    <div
      className={`rounded-2xl border backdrop-blur-md ${className}`}
      style={{
        background: dark ? 'rgba(42,30,76,0.40)' : 'rgba(255,255,255,0.33)',
        borderColor: dark ? 'rgba(255,255,255,0.13)' : 'rgba(255,255,255,0.85)',
        ...style,
      }}
    >
      {children}
    </div>
  )
}
EOF
echo OK"
```

- [ ] **Paso 7: Crear `LiveIndicator.tsx`**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "cat > /opt/PrecioLuzWeb/web/src/components/LiveIndicator.tsx << 'EOF'
export default function LiveIndicator() {
  return (
    <span className=\"inline-flex items-center gap-1.5\">
      <span className=\"relative flex h-2.5 w-2.5\">
        <span className=\"animate-ping absolute inline-flex h-full w-full rounded-full opacity-60\" style={{ background: '#30D158' }} />
        <span className=\"relative inline-flex rounded-full h-2.5 w-2.5\" style={{ background: '#30D158' }} />
      </span>
      <span className=\"text-xs opacity-60\">En directo</span>
    </span>
  )
}
EOF
echo OK"
```

---

### Task 6: Frontend React — componentes de precios

**Files:**
- Create: `/opt/PrecioLuzWeb/web/src/components/HeroPriceCard.tsx`
- Create: `/opt/PrecioLuzWeb/web/src/components/StatRow.tsx`
- Create: `/opt/PrecioLuzWeb/web/src/components/HourList.tsx`
- Create: `/opt/PrecioLuzWeb/web/src/hooks/usePrices.ts`

- [ ] **Paso 1: Crear `HeroPriceCard.tsx`**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "cat > /opt/PrecioLuzWeb/web/src/components/HeroPriceCard.tsx << 'EOF'
import GlassCard from './GlassCard'
import { DayPrices } from '../types'
import { TIER_COLORS_DARK, TIER_COLORS_LIGHT, TIER_LABELS, TRAMO_LABELS } from '../utils'

interface Props { day: DayPrices; isToday: boolean; dark: boolean }

export default function HeroPriceCard({ day, isToday, dark }: Props) {
  const currentHour = new Date().getHours()
  const hourData = isToday ? day.hours[currentHour] : null
  const price    = hourData?.priceKwh ?? day.avg
  const tier     = hourData?.tier ?? day.min.tier
  const colors   = dark ? TIER_COLORS_DARK : TIER_COLORS_LIGHT
  const color    = colors[tier]

  const label = isToday
    ? `Ahora · ${String(currentHour).padStart(2,'0')}–${String(currentHour+1).padStart(2,'0')}h`
    : 'Precio medio · mañana'

  const nextHour = (currentHour + 1) % 24
  const trendUp  = isToday && day.hours[nextHour].priceKwh >= day.hours[currentHour].priceKwh
  const trendColor = dark
    ? (trendUp ? TIER_COLORS_DARK.RED : TIER_COLORS_DARK.GREEN)
    : (trendUp ? TIER_COLORS_LIGHT.RED : TIER_COLORS_LIGHT.GREEN)

  return (
    <GlassCard dark={dark} className=\"relative overflow-hidden w-full\">
      {/* Glow radial por tier */}
      <div className=\"absolute inset-0 pointer-events-none\" style={{
        background: `radial-gradient(ellipse at 88% 0%, ${color}44 0%, transparent 70%)`,
      }} />
      <div className=\"relative p-5\">
        <p className=\"text-sm opacity-60\">{label}</p>
        <div className=\"flex items-end gap-2 mt-1\">
          <span className=\"text-5xl font-bold tabular-nums\" style={{ color }}>
            {price.toFixed(4).replace('.', ',')}
          </span>
          <span className=\"text-lg opacity-60 mb-1\">€/kWh</span>
        </div>
        <div className=\"flex items-center gap-2 mt-3 flex-wrap\">
          {/* Chip tier */}
          <span className=\"flex items-center gap-1.5 text-xs px-2.5 py-1 rounded-full\" style={{ background: `${color}22`, color }}>
            <span className=\"w-1.5 h-1.5 rounded-full inline-block\" style={{ background: color }} />
            {TIER_LABELS[tier]}
          </span>
          {/* Chip tramo */}
          {hourData && (
            <span className=\"text-xs px-2.5 py-1 rounded-full opacity-70\" style={{ background: 'rgba(128,128,128,0.15)' }}>
              {TRAMO_LABELS[hourData.tramo]}
            </span>
          )}
          {/* Tendencia */}
          {isToday && hourData && (
            <span className=\"text-xs font-medium\" style={{ color: trendColor }}>
              {trendUp ? '↑' : '↓'} {trendUp ? 'Sube' : 'Baja'} a las {String(nextHour).padStart(2,'0')}:00
            </span>
          )}
          {!isToday && (
            <span className=\"text-xs opacity-60\">
              Mín {day.min.priceKwh.toFixed(4).replace('.', ',')} · Máx {day.max.priceKwh.toFixed(4).replace('.', ',')}
            </span>
          )}
        </div>
      </div>
    </GlassCard>
  )
}
EOF
echo OK"
```

- [ ] **Paso 2: Crear `StatRow.tsx`**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "cat > /opt/PrecioLuzWeb/web/src/components/StatRow.tsx << 'EOF'
import GlassCard from './GlassCard'
import { DayPrices } from '../types'
import { TIER_COLORS_DARK, TIER_COLORS_LIGHT } from '../utils'

interface Props { day: DayPrices; dark: boolean }

export default function StatRow({ day, dark }: Props) {
  const colors = dark ? TIER_COLORS_DARK : TIER_COLORS_LIGHT
  const stats = [
    { label: 'Mínima 💰', price: day.min.priceKwh, sub: `${String(day.min.hour).padStart(2,'0')}–${String(day.min.hour+1).padStart(2,'0')}h`, color: colors[day.min.tier] },
    { label: 'Media',     price: day.avg,           sub: '€/kWh',  color: dark ? 'rgba(255,255,255,0.8)' : 'rgba(0,0,0,0.75)' },
    { label: 'Máxima 💀', price: day.max.priceKwh,  sub: `${String(day.max.hour).padStart(2,'0')}–${String(day.max.hour+1).padStart(2,'0')}h`, color: colors[day.max.tier] },
  ]
  return (
    <div className=\"grid grid-cols-3 gap-2.5\">
      {stats.map(s => (
        <GlassCard key={s.label} dark={dark} className=\"p-3\">
          <p className=\"text-xs opacity-40 mb-1\">{s.label}</p>
          <p className=\"text-lg font-bold tabular-nums\" style={{ color: s.color }}>
            {s.price.toFixed(3).replace('.', ',')}
          </p>
          <p className=\"text-xs opacity-40 mt-0.5\">{s.sub}</p>
        </GlassCard>
      ))}
    </div>
  )
}
EOF
echo OK"
```

- [ ] **Paso 3: Crear `HourList.tsx`**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "cat > /opt/PrecioLuzWeb/web/src/components/HourList.tsx << 'EOF'
import GlassCard from './GlassCard'
import { DayPrices } from '../types'
import { TIER_COLORS_DARK, TIER_COLORS_LIGHT, TIER_LABELS, TRAMO_LABELS } from '../utils'

interface Props { day: DayPrices; isToday: boolean; dark: boolean }

export default function HourList({ day, isToday, dark }: Props) {
  const colors     = dark ? TIER_COLORS_DARK : TIER_COLORS_LIGHT
  const currentHour = isToday ? new Date().getHours() : -1
  const maxPrice   = day.max.priceKwh
  const cheapest3  = new Set([...day.hours].sort((a,b) => a.priceKwh - b.priceKwh).slice(0,3).map(h => h.hour))
  const dearest3   = new Set([...day.hours].sort((a,b) => b.priceKwh - a.priceKwh).slice(0,3).map(h => h.hour))

  return (
    <GlassCard dark={dark} className=\"w-full\">
      <div className=\"p-4\">
        <p className=\"font-semibold mb-3\" style={{ color: dark ? 'rgba(255,255,255,0.9)' : 'rgba(0,0,0,0.85)' }}>
          Todas las horas
        </p>
        {/* Leyenda */}
        <div className=\"flex gap-3 mb-3 flex-wrap\">
          {(['GREEN','YELLOW','ORANGE','RED'] as const).map(t => (
            <span key={t} className=\"flex items-center gap-1 text-xs opacity-60\">
              <span className=\"w-1.5 h-1.5 rounded-full\" style={{ background: colors[t] }} />
              {TIER_LABELS[t]}
            </span>
          ))}
        </div>
        {day.hours.map((h, i) => {
          const color  = colors[h.tier]
          const isNow  = h.hour === currentHour
          const pct    = (h.priceKwh / maxPrice) * 100
          const mark   = cheapest3.has(h.hour) ? '💰' : dearest3.has(h.hour) ? '💀' : ''
          const priceColor = (cheapest3.has(h.hour) || dearest3.has(h.hour)) ? color : (dark ? 'rgba(255,255,255,0.85)' : 'rgba(0,0,0,0.8)')
          return (
            <div key={h.hour}>
              <div
                className=\"flex items-center gap-2 py-2 px-2 rounded-xl text-sm\"
                style={{ background: isNow ? (dark ? 'rgba(255,255,255,0.06)' : 'rgba(0,0,0,0.05)') : 'transparent' }}
              >
                <span className=\"w-8 shrink-0 tabular-nums opacity-80\">{String(h.hour).padStart(2,'0')}h</span>
                <span className=\"flex items-center gap-1 w-16 shrink-0\">
                  <span className=\"w-1.5 h-1.5 rounded-full shrink-0\" style={{ background: color }} />
                  <span className=\"text-xs opacity-60 truncate\">{TRAMO_LABELS[h.tramo]}</span>
                </span>
                <div className=\"flex-1 h-1.5 rounded-full\" style={{ background: dark ? 'rgba(255,255,255,0.08)' : 'rgba(0,0,0,0.08)' }}>
                  <div className=\"h-full rounded-full\" style={{ width: `${pct}%`, background: color }} />
                </div>
                <span className=\"w-5 text-center shrink-0\">{mark}</span>
                <span className=\"w-16 text-right tabular-nums shrink-0 font-medium\" style={{ color: priceColor }}>
                  {h.priceKwh.toFixed(4).replace('.', ',')} €
                </span>
              </div>
              {i < 23 && <hr style={{ borderColor: dark ? 'rgba(255,255,255,0.04)' : 'rgba(0,0,0,0.05)' }} />}
            </div>
          )
        })}
      </div>
    </GlassCard>
  )
}
EOF
echo OK"
```

- [ ] **Paso 4: Crear `hooks/usePrices.ts`**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "cat > /opt/PrecioLuzWeb/web/src/hooks/usePrices.ts << 'EOF'
import { useQuery } from '@tanstack/react-query'
import { ApiResponse, DayPrices } from '../types'
import { buildDayPrices } from '../utils'

interface PricesResult {
  today: DayPrices | null
  tomorrow: DayPrices | null
  updatedAt: string | null
  isLoading: boolean
  error: string | null
}

export function usePrices(): PricesResult {
  const { data, isLoading, error } = useQuery<ApiResponse>({
    queryKey: ['precios'],
    queryFn: async () => {
      const res = await fetch('/api/precios')
      if (!res.ok) throw new Error(`HTTP ${res.status}`)
      return res.json()
    },
    staleTime: 30 * 60 * 1000,   // 30 min — precios cambian 4 veces al día máx
    retry: 2,
  })

  return {
    today:     data ? buildDayPrices(data.today.date, data.today.prices) : null,
    tomorrow:  data?.tomorrow ? buildDayPrices(data.tomorrow.date, data.tomorrow.prices) : null,
    updatedAt: data?.updated_at ?? null,
    isLoading,
    error:     error ? (error as Error).message : null,
  }
}
EOF
echo OK"
```

---

### Task 7: Frontend React — App.tsx + main.tsx

**Files:**
- Create: `/opt/PrecioLuzWeb/web/src/App.tsx`
- Create: `/opt/PrecioLuzWeb/web/src/main.tsx`
- Create: `/opt/PrecioLuzWeb/web/index.html`

- [ ] **Paso 1: Crear `App.tsx`**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "cat > /opt/PrecioLuzWeb/web/src/App.tsx << 'EOF'
import { useState, useEffect } from 'react'
import AuroraBackground from './components/AuroraBackground'
import GlassCard from './components/GlassCard'
import HeroPriceCard from './components/HeroPriceCard'
import StatRow from './components/StatRow'
import HourList from './components/HourList'
import LiveIndicator from './components/LiveIndicator'
import { usePrices } from './hooks/usePrices'
import { Zap } from 'lucide-react'

type Tab = 'today' | 'tomorrow'
type Theme = 'auto' | 'light' | 'dark'

export default function App() {
  const [tab, setTab]     = useState<Tab>('today')
  const [theme, setTheme] = useState<Theme>('auto')
  const { today, tomorrow, updatedAt, isLoading, error } = usePrices()

  const systemDark = window.matchMedia('(prefers-color-scheme: dark)').matches
  const dark = theme === 'auto' ? systemDark : theme === 'dark'

  const textMain = dark ? 'rgba(255,255,255,0.9)'  : 'rgba(0,0,0,0.85)'
  const textSub  = dark ? 'rgba(255,255,255,0.44)' : 'rgba(0,0,0,0.44)'
  const themeIcons: Record<Theme, string> = { auto: '✦', light: '☀️', dark: '🌙' }
  const nextTheme: Record<Theme, Theme>   = { auto: 'light', light: 'dark', dark: 'auto' }

  const day = tab === 'today' ? today : tomorrow

  return (
    <div className=\"min-h-screen\" style={{ color: textMain }}>
      <AuroraBackground dark={dark} />

      <div className=\"max-w-lg mx-auto px-4 pb-28\">
        {/* Header */}
        <div className=\"flex items-center gap-2.5 pt-10 pb-4\">
          <div className=\"w-9 h-9 rounded-xl flex items-center justify-center shrink-0\"
               style={{ background: 'linear-gradient(135deg, #FFC24D, #FF7A00)' }}>
            <Zap size={18} color=\"white\" fill=\"white\" />
          </div>
          <div>
            <p className=\"font-semibold leading-tight\">PrecioLuz</p>
            <p className=\"text-xs\" style={{ color: textSub }}>Precio de la luz · PVPC</p>
          </div>
          <div className=\"ml-auto\">
            <GlassCard dark={dark}>
              <button
                onClick={() => setTheme(nextTheme[theme])}
                className=\"w-10 h-10 flex items-center justify-center text-lg\"
                aria-label=\"Cambiar tema\"
              >
                {themeIcons[theme]}
              </button>
            </GlassCard>
          </div>
        </div>

        {/* Error */}
        {error && (
          <GlassCard dark={dark} className=\"p-4 mb-4\">
            <p className=\"text-sm text-red-400\">No se pudieron cargar los precios. Inténtalo de nuevo.</p>
          </GlassCard>
        )}

        {/* Loading */}
        {isLoading && (
          <div className=\"flex justify-center py-20\">
            <div className=\"w-8 h-8 rounded-full border-2 border-orange-400 border-t-transparent animate-spin\" />
          </div>
        )}

        {/* Content */}
        {!isLoading && today && (
          <>
            {/* Título + fecha */}
            <div className=\"flex items-start justify-between mb-3\">
              <div>
                <h1 className=\"text-4xl font-bold\">{tab === 'today' ? 'Hoy' : 'Mañana'}</h1>
                <p className=\"text-sm mt-0.5\" style={{ color: textSub }}>
                  {new Date((tab === 'today' ? today.date : tomorrow?.date ?? today.date) + 'T12:00:00')
                    .toLocaleDateString('es-ES', { weekday: 'long', day: 'numeric', month: 'long' })
                    .replace(/^\w/, c => c.toUpperCase())}
                </p>
              </div>
              {tab === 'today' && <LiveIndicator />}
            </div>

            {day && (
              <div className=\"flex flex-col gap-3.5\">
                <HeroPriceCard day={day} isToday={tab === 'today'} dark={dark} />
                <StatRow day={day} dark={dark} />
                <HourList day={day} isToday={tab === 'today'} dark={dark} />
              </div>
            )}

            {tab === 'tomorrow' && !tomorrow && (
              <GlassCard dark={dark} className=\"p-5 text-center\">
                <p className=\"text-sm opacity-60\">Los precios de mañana se publican a partir de las 20:15h</p>
              </GlassCard>
            )}

            {/* Footer */}
            {updatedAt && (
              <p className=\"text-xs mt-4 pb-4\" style={{ color: textSub }}>
                Datos PVPC · precios con impuestos incluidos · actualizado {new Date(updatedAt).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' })}h
              </p>
            )}
          </>
        )}
      </div>

      {/* Tab bar flotante */}
      {today && (
        <div className=\"fixed bottom-6 left-1/2 -translate-x-1/2 z-50\">
          <GlassCard dark={dark} className=\"flex p-1 gap-1\">
            {(['today', 'tomorrow'] as Tab[]).map(t => (
              <button
                key={t}
                onClick={() => setTab(t)}
                disabled={t === 'tomorrow' && !tomorrow}
                className=\"px-5 py-2 rounded-xl text-sm font-medium transition-all\"
                style={{
                  background: tab === t ? (dark ? 'rgba(255,255,255,0.15)' : 'rgba(0,0,0,0.08)') : 'transparent',
                  color: tab === t ? textMain : textSub,
                  cursor: (t === 'tomorrow' && !tomorrow) ? 'not-allowed' : 'pointer',
                  opacity: (t === 'tomorrow' && !tomorrow) ? 0.4 : 1,
                }}
              >
                {t === 'today' ? 'Hoy' : 'Mañana'}
              </button>
            ))}
          </GlassCard>
        </div>
      )}
    </div>
  )
}
EOF
echo OK"
```

- [ ] **Paso 2: Crear `main.tsx` e `index.html`**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "
cat > /opt/PrecioLuzWeb/web/src/main.tsx << 'EOF'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import './index.css'
import App from './App'

const queryClient = new QueryClient()
createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <App />
    </QueryClientProvider>
  </StrictMode>
)
EOF

cat > /opt/PrecioLuzWeb/web/index.html << 'EOF'
<!doctype html>
<html lang=\"es\">
<head>
  <meta charset=\"UTF-8\" />
  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />
  <title>PrecioLuz — PVPC España</title>
  <meta name=\"description\" content=\"Precio de la luz PVPC en tiempo real para la Península Ibérica\" />
</head>
<body>
  <div id=\"root\"></div>
  <script type=\"module\" src=\"/src/main.tsx\"></script>
</body>
</html>
EOF
echo OK"
```

- [ ] **Paso 3: Crear `tsconfig.json` para el workspace web**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "cat > /opt/PrecioLuzWeb/web/tsconfig.json << 'EOF'
{
  \"compilerOptions\": {
    \"target\": \"ES2020\",
    \"lib\": [\"ES2020\", \"DOM\", \"DOM.Iterable\"],
    \"module\": \"ESNext\",
    \"moduleResolution\": \"bundler\",
    \"jsx\": \"react-jsx\",
    \"strict\": true,
    \"noEmit\": true
  },
  \"include\": [\"src\"]
}
EOF
echo OK"
```

---

### Task 8: Deploy y Apache vhost

**Files:**
- Create: `/etc/apache2/sites-available/precioluz.hugopvigo.conf`

- [ ] **Paso 1: Instalar dependencias Node en el servidor y hacer el build inicial**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "
cd /opt/PrecioLuzWeb && npm install 2>&1 | tail -5
"
```

Salida esperada: `added X packages` sin errores

- [ ] **Paso 2: Levantar con Docker Compose**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "
cd /opt/PrecioLuzWeb && docker compose -f docker/docker-compose.yml up -d --build 2>&1 | tail -10
"
```

- [ ] **Paso 3: Verificar que la API responde**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "
curl -s http://localhost:8081/api/precios | python3 -m json.tool | head -15
"
```

Salida esperada: JSON con `today.prices` (array de 24 valores)

- [ ] **Paso 4: Crear vhost Apache para `precioluz.hugopvigo.es`**

```bash
ssh -i /tmp/agente_smpp.pem -p 2220 ubuntu@143.47.51.56 "
sudo tee /etc/apache2/sites-available/precioluz.hugopvigo.conf << 'EOF'
<VirtualHost *:80>
    ServerName precioluz.hugopvigo.es
    Redirect permanent / https://precioluz.hugopvigo.es/
</VirtualHost>

<VirtualHost *:443>
    ServerName precioluz.hugopvigo.es

    SSLEngine on
    SSLCertificateFile    /etc/apache2/ssl/cloudflare.pem
    SSLCertificateKeyFile /etc/apache2/ssl/cloudflare-key.pem

    ProxyPreserveHost On
    ProxyPass        / http://127.0.0.1:8081/
    ProxyPassReverse / http://127.0.0.1:8081/

    ErrorLog  \${APACHE_LOG_DIR}/precioluz_error.log
    CustomLog \${APACHE_LOG_DIR}/precioluz_access.log combined
</VirtualHost>
EOF
sudo a2ensite precioluz.hugopvigo.conf &&
sudo apache2ctl configtest && sudo systemctl reload apache2
"
```

Salida esperada: `Syntax OK`

- [ ] **Paso 5: Verificar HTTPS público**

```bash
curl -s https://precioluz.hugopvigo.es/api/precios | python3 -m json.tool | head -8
```

Salida esperada: JSON con precios del día.

> **Nota:** Requiere que `precioluz.hugopvigo.es` esté añadido como registro CNAME/A en Cloudflare apuntando al servidor. Si aún no existe, crear el DNS antes de este paso.

- [ ] **Paso 6: Verificar rate limiting**

```bash
for i in $(seq 1 22); do curl -s -o /dev/null -w "%{http_code} " https://precioluz.hugopvigo.es/api/precios; done
```

Salida esperada: 21 respuestas `200` seguidas de `429` (Too Many Requests) al pasar de 20/min.

- [ ] **Paso 7: Actualizar CLAUDE.md global con la nueva entrada**

Añadir en la sección de inventario de servidores de `~/.claude/CLAUDE.md`:

```
Bot PrecioLuzWeb: compose en `/opt/PrecioLuzWeb/docker/`. Puerto 8081. Actualizar: `cd /opt/PrecioLuzWeb && git pull && docker compose -f docker/docker-compose.yml up -d --build`. API en `https://precioluz.hugopvigo.es/api/precios`. DB SQLite en `/opt/PrecioLuzWeb/data/precioluz.db`.
```

---

## Subsistema B — Android App

### Task 9: Fix PriceEntity — clave primaria compuesta

**Files:**
- Modify: `app/src/main/java/com/precioluz/app/data/local/PriceEntity.kt`
- Modify: `app/src/main/java/com/precioluz/app/data/local/PrecioLuzDatabase.kt`
- Modify: `app/src/main/java/com/precioluz/app/data/local/PriceDao.kt`
- Modify: `app/src/main/java/com/precioluz/app/di/DatabaseModule.kt`

- [ ] **Paso 1: Corregir `PriceEntity.kt`**

```kotlin
package com.precioluz.app.data.local

import androidx.room.Entity

@Entity(tableName = "prices", primaryKeys = ["date", "hour"])
data class PriceEntity(
    val date: String,
    val hour: Int,
    val priceKwh: Double,
    val priceMwh: Double,
)
```

- [ ] **Paso 2: Añadir `deletePricesOlderThan` en `PriceDao.kt`**

Añadir este método al final de la interfaz:

```kotlin
@Query("DELETE FROM prices WHERE date < :date")
suspend fun deletePricesOlderThan(date: String)
```

- [ ] **Paso 3: Actualizar `PrecioLuzDatabase.kt` a versión 2**

```kotlin
@Database(entities = [PriceEntity::class], version = 2, exportSchema = false)
abstract class PrecioLuzDatabase : RoomDatabase() {
    abstract fun priceDao(): PriceDao
}
```

- [ ] **Paso 4: Añadir `fallbackToDestructiveMigration` en `DatabaseModule.kt`**

```kotlin
Room.databaseBuilder(context, PrecioLuzDatabase::class.java, "precioluz.db")
    .fallbackToDestructiveMigration()
    .build()
```

- [ ] **Paso 5: Compilar**

```bash
cd /home/hugo/Dev/PrecioLuzApp && ./gradlew assembleDebug 2>&1 | tail -5
```

Salida esperada: `BUILD SUCCESSFUL`

- [ ] **Paso 6: Commit**

```bash
git add app/src/main/java/com/precioluz/app/data/local/
git commit -m "fix(db): composite primary key (date,hour) in PriceEntity, Room v2"
```

---

### Task 10: DTOs del servidor

**Files:**
- Create: `app/src/main/java/com/precioluz/app/data/api/PrecioLuzJsonDto.kt`

- [ ] **Paso 1: Crear DTOs**

```kotlin
package com.precioluz.app.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrecioLuzJsonResponse(
    @SerialName("updated_at") val updatedAt: String,
    val today: DayJson,
    val tomorrow: DayJson? = null,
)

@Serializable
data class DayJson(
    val date: String,
    val prices: List<Double>,   // 24 valores €/kWh, índice = hora
)
```

- [ ] **Paso 2: Commit**

```bash
git add app/src/main/java/com/precioluz/app/data/api/PrecioLuzJsonDto.kt
git commit -m "feat(data): DTOs for PrecioLuzWeb JSON endpoint"
```

---

### Task 11: PrecioLuzJsonApi — cliente del servidor

**Files:**
- Create: `app/src/main/java/com/precioluz/app/data/api/PrecioLuzJsonApi.kt`

- [ ] **Paso 1: Crear cliente OkHttp**

```kotlin
package com.precioluz.app.data.api

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

private const val PRECIOS_URL = "https://precioluz.hugopvigo.es/api/precios"

@Singleton
class PrecioLuzJsonApi @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json,
) {
    fun fetch(): PrecioLuzJsonResponse {
        val request = Request.Builder().url(PRECIOS_URL).build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: throw Exception("Respuesta vacía")
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
            return json.decodeFromString(body)
        }
    }
}
```

- [ ] **Paso 2: Compilar**

```bash
./gradlew assembleDebug 2>&1 | tail -5
```

- [ ] **Paso 3: Commit**

```bash
git add app/src/main/java/com/precioluz/app/data/api/PrecioLuzJsonApi.kt
git commit -m "feat(data): PrecioLuzJsonApi client for server endpoint"
```

---

### Task 12: Refactorizar PrecioLuzRepository — cache-first

**Files:**
- Modify: `app/src/main/java/com/precioluz/app/data/repository/PrecioLuzRepository.kt`

Lógica:
1. Room cache → si hay 24 registros para la fecha → devolver sin red
2. Llamar al servidor → una respuesta contiene hoy + mañana
3. Guardar ambos días en Room
4. Limpiar fechas anteriores a ayer

- [ ] **Paso 1: Reescribir `PrecioLuzRepository.kt`**

```kotlin
package com.precioluz.app.data.repository

import com.precioluz.app.data.api.DayJson
import com.precioluz.app.data.api.PrecioLuzJsonApi
import com.precioluz.app.data.local.PriceDao
import com.precioluz.app.data.local.PriceEntity
import com.precioluz.app.domain.model.DayPrices
import com.precioluz.app.domain.model.HourPrice
import com.precioluz.app.domain.model.PriceTier
import com.precioluz.app.domain.model.Tramo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrecioLuzRepository @Inject constructor(
    private val jsonApi: PrecioLuzJsonApi,
    private val dao: PriceDao,
) {
    fun getTodayPrices(): Flow<Result<DayPrices>> = flow {
        emit(runCatching { getPricesForDate(LocalDate.now()) })
    }

    fun getTomorrowPrices(): Flow<Result<DayPrices?>> = flow {
        emit(runCatching {
            val publishedAt = LocalDate.now().atTime(20, 15)
                .atZone(ZoneId.of("Europe/Madrid"))
            if (ZonedDateTime.now(ZoneId.of("Europe/Madrid")).isBefore(publishedAt)) null
            else getPricesForDate(LocalDate.now().plusDays(1))
        })
    }

    private suspend fun getPricesForDate(date: LocalDate): DayPrices =
        withContext(Dispatchers.IO) {
            val dateStr = date.toString()

            // Cache Room: si hay 24 registros, devolver sin llamada de red
            val cached = dao.getPricesForDate(dateStr)
            if (cached.size == 24) {
                return@withContext buildDayPrices(date, cached.map { it.hour to it.priceKwh })
            }

            // Fetch servidor (hoy + mañana en una sola llamada)
            val response = jsonApi.fetch()

            // Guardar todos los días recibidos
            listOfNotNull(response.today, response.tomorrow).forEach { saveToRoom(it) }

            // Limpiar fechas viejas
            dao.deletePricesOlderThan(LocalDate.now().minusDays(1).toString())

            val dayJson = when (dateStr) {
                response.today.date    -> response.today
                response.tomorrow?.date -> response.tomorrow
                else                   -> throw Exception("NO_DATA")
            }
            if (dayJson.prices.size != 24) throw Exception("NO_DATA")

            buildDayPrices(date, dayJson.prices.mapIndexed { idx, p -> idx to p })
        }

    private suspend fun saveToRoom(dayJson: DayJson) {
        if (dayJson.prices.size != 24) return
        dao.insertPrices(dayJson.prices.mapIndexed { hour, price ->
            PriceEntity(date = dayJson.date, hour = hour, priceKwh = price, priceMwh = price * 1000)
        })
    }

    private fun buildDayPrices(date: LocalDate, prices: List<Pair<Int, Double>>): DayPrices {
        val sorted  = prices.sortedBy { it.second }
        val tierMap = sorted.mapIndexed { rank, (hour, _) ->
            hour to PriceTier.entries[minOf(3, rank / 6)]
        }.toMap()

        val hours = prices.map { (hour, price) ->
            HourPrice(
                hour  = hour,
                price = price,
                tramo = tramoForHour(hour, date),
                tier  = tierMap[hour] ?: PriceTier.GREEN,
            )
        }

        val minH = hours.minBy { it.price }
        val maxH = hours.maxBy { it.price }
        val decorated = hours.map { h ->
            h.copy(isMin = h.hour == minH.hour, isMax = h.hour == maxH.hour)
        }
        val bestStart  = (0..22).minBy { i -> decorated[i].price + decorated[i+1].price }
        val worstStart = (0..22).maxBy { i -> decorated[i].price + decorated[i+1].price }

        return DayPrices(
            date        = date,
            hours       = decorated,
            avg         = hours.map { it.price }.average(),
            min         = hours[minH.hour],
            max         = hours[maxH.hour],
            bestWindow  = bestStart..bestStart + 1,
            worstWindow = worstStart..worstStart + 1,
        )
    }

    private fun tramoForHour(hour: Int, date: LocalDate): Tramo {
        val dow = date.dayOfWeek.value
        if (dow >= 6) return Tramo.VALLE
        if ((hour in 10..13) || (hour in 18..21)) return Tramo.PUNTA
        if (hour < 8) return Tramo.VALLE
        return Tramo.LLANO
    }
}
```

- [ ] **Paso 2: Compilar**

```bash
./gradlew assembleDebug 2>&1 | tail -10
```

Salida esperada: `BUILD SUCCESSFUL`

- [ ] **Paso 3: Commit**

```bash
git add app/src/main/java/com/precioluz/app/data/repository/PrecioLuzRepository.kt
git commit -m "feat(repo): cache-first Room + PrecioLuzWeb server, no API key needed"
```

---

### Task 13: Eliminar bloqueo por API key en ViewModel y UI

**Files:**
- Modify: `app/src/main/java/com/precioluz/app/ui/viewmodel/PrecioLuzViewModel.kt`
- Modify: `app/src/main/java/com/precioluz/app/ui/components/SettingsDialog.kt`
- Modify: `app/src/main/java/com/precioluz/app/MainActivity.kt`

- [ ] **Paso 1: Simplificar `PrecioLuzViewModel.kt`**

Eliminar la guarda de API key en `init` y `refresh`. Eliminar `saveApiKey`, `clearApiKey` y el `StateFlow<String?> apiKey`. El `noApiKey` del `UiState` también desaparece.

`PrecioLuzUiState` queda:
```kotlin
data class PrecioLuzUiState(
    val today: DayPrices?         = null,
    val tomorrow: DayPrices?      = null,
    val isLoading: Boolean        = true,
    val error: String?            = null,
    val tomorrowNotReady: Boolean = false,
)
```

`init` queda:
```kotlin
init {
    refresh()
}
```

`refresh` queda (sin la guarda de apiKey):
```kotlin
fun refresh() {
    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        try {
            getTodayPrices().collect { result ->
                result.onSuccess { day -> _uiState.update { it.copy(today = day) } }
                      .onFailure { e  -> _uiState.update { it.copy(error = mensajeError(e)) } }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = mensajeError(e)) }
        }
        try {
            getTomorrowPrices().collect { result ->
                result.onSuccess { day -> _uiState.update { it.copy(tomorrow = day, tomorrowNotReady = day == null, isLoading = false) } }
                      .onFailure { _uiState.update { it.copy(isLoading = false) } }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
```

- [ ] **Paso 2: Simplificar `SettingsDialog.kt`**

Eliminar los campos `currentApiKey`, `onSave`, `onClear` y toda la UI de API key. El diálogo solo muestra el selector de tema (ya lo tiene) u otras opciones futuras.

- [ ] **Paso 3: Actualizar `MainActivity.kt`**

Eliminar los parámetros `apiKey`, `onSaveApiKey`, `onClearApiKey` de `PrecioLuzApp()` y sus propiedades en el composable.

- [ ] **Paso 4: Eliminar `GetPricesUseCase` y `HomeViewModel` si son dead code**

```bash
grep -r "HomeViewModel\|GetPricesUseCase\b" /home/hugo/Dev/PrecioLuzApp/app/src --include="*.kt" -l
```

Si solo aparecen en sus propios archivos → eliminarlos:
```bash
rm app/src/main/java/com/precioluz/app/ui/home/HomeViewModel.kt
rm app/src/main/java/com/precioluz/app/domain/usecase/GetPricesUseCase.kt
# También data/network/EsiosApi.kt y data/network/EsiosDto.kt si no se usan
```

- [ ] **Paso 5: Compilar**

```bash
./gradlew assembleDebug 2>&1 | tail -10
```

Salida esperada: `BUILD SUCCESSFUL`

- [ ] **Paso 6: Commit final**

```bash
git add app/src/main/java/com/precioluz/app/
git commit -m "feat(app): remove mandatory API key — consumes PrecioLuzWeb endpoint"
```

---

## Verificación final

- [ ] **Servidor — API:** `curl -s https://precioluz.hugopvigo.es/api/precios | python3 -m json.tool` devuelve JSON con 24 precios.
- [ ] **Servidor — Rate limit:** 21ª llamada en menos de 1 min devuelve HTTP 429.
- [ ] **Servidor — Web:** Abrir `https://precioluz.hugopvigo.es` en el navegador → carga la UI con precios.
- [ ] **App — sin API key:** Abrir la app sin token configurado → muestra precios correctamente.
- [ ] **App — modo avión:** Activar modo avión, cerrar app, abrir app → muestra datos del caché Room sin error.
- [ ] **Scheduler:** `docker logs precioluz-web-api-1 2>&1 | grep scheduler` → confirma que los 4 jobs están registrados al arrancar.
- [ ] **Log de mañana a las 21:30:** `docker logs precioluz-web-api-1 2>&1 | grep tomorrow` → muestra intentos 1/2/3.
