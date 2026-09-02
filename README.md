# Vixta

App de inspección de cadena de frío con clasificación por IA en el dispositivo.
Ingeniería de Dispositivos Móviles · Equipo 1 · Grupo 002

## Carpetas del proyecto

**app/** → el proyecto de Android Studio. Adentro, cada pantalla tiene su propia carpeta:

| Carpeta | Qué es |
|---|---|
| `login` | Pantalla de inicio de sesión |
| `dashboard` | Tablero principal con los puntos fríos |
| `escaneo` | Escaneo de QR / código de barras |
| `inspeccion` | Inspección con IA (cámara + clasificación) |
| `revision` | Checklist de la ronda |
| `alertas` | Alertas y su atención |
| `historial` | Historial y reportes |
| `configuracion` | Ajustes y cola de sincronización |
| `datos` | Todo lo que guarda o trae información: `local` (Room), `remoto` (Supabase), `sincronizacion` (subir lo pendiente) |
| `modelo` | El código que usa el modelo de IA dentro de la app |

**modelo-ia/** → todo lo del entrenamiento del modelo, **no va dentro de la app**:
- `entrenamiento` → notebooks donde se entrena el modelo
- `dataset` → imágenes usadas para entrenar (no se sube a GitHub, pesa mucho)

**docs/** → documentos del equipo:
- `ficha-tecnica` → ficha técnica y cronograma
- `manuales` → manual técnico y manual de usuario
- `mockups` → mockups de las pantallas

## Quién trabaja en qué carpeta

| Integrante | Carpeta(s) |
|---|---|
| Cristopher Palacios | `datos/remoto`, `modelo-ia` |
| Patricio Loredo | `login`, `historial` |
| Anahi Limón | `datos` (reglas y endpoints), `docs/manuales` |
| Pablo Saavedra | `datos/local`, `datos/sincronizacion` |
| Cristian Bustamante | `dashboard`, `escaneo`, `revision`, `alertas` |

## Cómo trabajar con Git

1. `git pull` antes de empezar, para traer lo último de `main`.
2. Crea tu rama: `git checkout -b feature/login` (usa el nombre de tu carpeta).
3. Guarda tus cambios: `git commit -m "mensaje claro de lo que hiciste"`
4. Sube tu rama: `git push origin feature/login`
5. Abre un Pull Request hacia `main` para que la revisen antes de mezclarla.
6. Nadie hace push directo a `main`.

**Nunca subir:** `local.properties`, contraseñas o keys de Supabase, ni el dataset completo de imágenes.
