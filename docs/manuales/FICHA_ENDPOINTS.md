# Ficha de endpoints — Vixta

**Estado:** propuesta técnica para la actividad 19, “Reglas de negocio y endpoints”.  
**Alcance:** contrato de las operaciones que necesitará la aplicación. No crea tablas, no cambia políticas RLS y no contiene claves.

## Base de conexión

Supabase publica la API REST de las tablas bajo:

```text
{SUPABASE_URL}/rest/v1/
```

La app usará la URL del proyecto y una **publishable key** desde configuración local. Nunca se guardará ni se subirá una clave `sb_secret_` al repositorio. Cuando se habilite autenticación, las solicitudes deberán llevar el token de la sesión del usuario para que RLS aplique sus permisos.

## Resumen

| Operación | Recurso | Método REST | Estado |
|---|---|---|---|
| Consultar un punto por QR | `punto_frio` | `GET` | Lista para prueba de lectura |
| Listar puntos activos | `punto_frio` | `GET` | Lista para prueba de lectura |
| Iniciar ronda offline-first | `ronda` | `POST` | Requiere usuario autenticado y RLS |
| Guardar checklist de una ronda | `paso_ronda` | `POST` / `PATCH` | Requiere ronda propia |
| Sincronizar inspección | `inspeccion` | `POST` | Requiere ronda propia, Storage y RLS |
| Consultar historial | vista o función controlada | `GET` / `POST` | Pendiente de definir implementación segura |
| Atender alerta | `alerta` | `PATCH` | Sólo supervisor, pendiente de RLS |

Las rutas anteriores representan recursos de la API REST automática de Supabase. La operación de historial no debe ejecutar SQL libre desde la app; se resolverá con una vista o función de base de datos una vez acordados los permisos.

## 1. Consultar punto frío por código QR

La pantalla de escaneo recibe el texto del QR y busca un punto activo por su código único.

```http
GET /rest/v1/punto_frio?select=id,codigo,nombre,tipo,ubicacion,activo&codigo=eq.CAM-DEMO-01&activo=eq.true
```

Ejemplo de respuesta correcta:

```json
[
  {
    "id": "uuid-del-punto",
    "codigo": "CAM-DEMO-01",
    "nombre": "Cámara demo · Lácteos",
    "tipo": "camara",
    "ubicacion": "texto o null",
    "activo": true
  }
]
```

Reglas:

- `codigo` es único y corresponde al contenido del QR.
- Si la respuesta está vacía, la app muestra “Punto frío no encontrado o inactivo”.
- La app no debe iniciar una ronda en un punto con `activo = false`.

## 2. Listar puntos fríos activos

Sirve para seleccionar un punto manualmente si el escáner no está disponible.

```http
GET /rest/v1/punto_frio?select=id,codigo,nombre,tipo,ubicacion&activo=eq.true&order=nombre.asc
```

La aplicación sólo muestra `codigo`, `nombre`, `tipo` y `ubicacion`; no necesita exponer datos de otras tablas para esta pantalla.

## 3. Iniciar una ronda

El teléfono genera `id_local` antes de tener conexión. Esto permite reintentar una sincronización sin crear duplicados.

```http
POST /rest/v1/ronda
Content-Type: application/json
Prefer: return=representation
```

```json
{
  "id_local": "uuid-generado-por-el-dispositivo",
  "punto_id": "uuid-del-punto",
  "usuario_id": "uuid-del-usuario",
  "iniciada_en": "2026-09-22T16:00:00Z",
  "estado": "en_curso",
  "sincronizada": false
}
```

Reglas:

- `id_local` debe conservarse si la app reintenta el envío; es único.
- `punto_id` debe pertenecer a un punto activo.
- Cuando Auth esté enlazado con `usuario`, el servidor debe validar que `usuario_id` corresponde al usuario autenticado; no bastará con confiar en el valor enviado por la app.

## 4. Guardar pasos del checklist

Cada paso pertenece a una ronda y su orden no puede repetirse dentro de ella.

```json
{
  "ronda_id": "uuid-de-la-ronda",
  "orden": 1,
  "descripcion": "Verificar que la puerta esté cerrada",
  "completado": true,
  "completado_en": "2026-09-22T16:03:00Z"
}
```

Reglas:

- El par `ronda_id` + `orden` es único.
- Un operador sólo podrá modificar pasos de sus propias rondas, una vez que RLS esté configurado.

## 5. Sincronizar una inspección y evidencia

Primero se carga la foto al bucket acordado. Después `foto_url` guarda la ruta o URL resultante en `inspeccion`.

```json
{
  "id_local": "uuid-generado-por-el-dispositivo",
  "ronda_id": "uuid-de-la-ronda",
  "lote_id": "uuid-del-lote-o-null",
  "foto_url": "ruta-de-storage-aun-por-definir",
  "clase": "integro",
  "confianza": 0.941,
  "origen": "modelo",
  "capturada_en": "2026-09-22T16:05:00Z",
  "lat": 25.681234,
  "lon": -100.315678,
  "precision_gps": 8.5,
  "sincronizada": false
}
```

Reglas de validación del esquema:

- `id_local` es único para evitar duplicados al reintentar.
- `clase` sólo acepta `integro`, `empaque_danado`, `contaminacion` o `etiqueta_ilegible`.
- `origen` sólo acepta `modelo` o `manual`.
- Si `origen` es `modelo`, `confianza` es obligatoria y debe estar entre 0 y 1.
- Si se reporta ubicación, la app debe enviar también `precision_gps` para acreditar el margen de error.

## 6. Consultar historial y trazabilidad

Los filtros requeridos son por lote, punto frío y rango de fechas. Las consultas SQL ya están descritas en [`CONSULTAS_TRAZABILIDAD.md`](CONSULTAS_TRAZABILIDAD.md).

Antes de conectarlo a la app, el equipo deberá elegir una de estas alternativas seguras:

1. Crear una vista de sólo lectura y consultarla por REST.
2. Crear una función de Postgres (RPC) que reciba filtros específicos.

La app nunca enviará SQL arbitrario a Supabase.

## Pruebas mínimas

| Caso | Resultado esperado |
|---|---|
| Buscar `CAM-DEMO-01` | Devuelve Cámara demo · Lácteos si la sesión tiene permiso de lectura. |
| Buscar código inexistente | Devuelve una lista vacía y la app informa que no encontró el punto. |
| Reenviar la misma ronda sin conexión | No duplica la ronda porque `id_local` es único. |
| Inspección de modelo sin confianza | La base rechaza el registro. |
| Operador intenta atender una alerta | Debe ser rechazado cuando se active RLS. |

## Dependencias antes de implementar en Android

- Definir la relación entre `auth.uid()` y `public.usuario.id`.
- Crear políticas RLS y usuarios de prueba con rol operador y supervisor.
- Acordar el bucket de evidencia y sus reglas de acceso.
- Elegir vista o función RPC para el historial.
