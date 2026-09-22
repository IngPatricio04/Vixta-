# Diccionario de datos — Vixta

**Fuente:** `docs/modelo-datos/esquema.sql`  
**Propósito:** referencia para la app Android, las consultas de trazabilidad y el manual técnico. Describe la estructura actual; no modifica la base de datos.

## Convenciones generales

- Todas las claves primarias son UUID y se generan por defecto en la base.
- Las fechas se almacenan como `timestamptz` y la app las debe enviar en formato ISO 8601 con zona horaria.
- `id_local` se genera en el teléfono para poder reintentar sincronizaciones sin duplicar datos.
- Un campo `activo = false` conserva el historial, pero no debe aparecer como opción operativa nueva.

## `usuario`

Representa a cada operador o supervisor del equipo.

| Campo | Tipo | Regla / significado |
|---|---|---|
| `id` | UUID | Identificador único. |
| `nombre` | texto | Obligatorio. Nombre visible de la persona. |
| `correo` | texto | Obligatorio y único. |
| `rol` | texto | Obligatorio: `operador` o `supervisor`. |
| `activo` | booleano | Obligatorio; inicia en `true`. |
| `creado_en` | fecha con zona | Se asigna al crear el registro. |

**Pendiente de equipo:** definir de forma verificable la relación entre este `id` y `auth.uid()` de Supabase Auth antes de crear políticas RLS.

## `punto_frio`

Catálogo de cámaras, tarimas o andenes. El campo `codigo` es el valor leído por QR.

| Campo | Tipo | Regla / significado |
|---|---|---|
| `id` | UUID | Identificador único. |
| `codigo` | texto | Obligatorio y único; contenido del QR. |
| `nombre` | texto | Obligatorio; nombre visible del punto. |
| `tipo` | texto | Obligatorio: `camara`, `tarima` o `anden`. |
| `ubicacion` | texto | Opcional. Referencia física del punto. |
| `activo` | booleano | Obligatorio; inicia en `true`. |

Ejemplo de prueba existente: `CAM-DEMO-01`, Cámara demo · Lácteos.

## `lote`

Identifica el lote de producto que se inspecciona.

| Campo | Tipo | Regla / significado |
|---|---|---|
| `id` | UUID | Identificador único. |
| `codigo` | texto | Obligatorio y único. |
| `descripcion` | texto | Opcional. |

## `ronda`

Registra una visita de inspección a un punto frío.

| Campo | Tipo | Regla / significado |
|---|---|---|
| `id` | UUID | Identificador remoto único. |
| `id_local` | UUID | Obligatorio y único; lo crea el teléfono. |
| `punto_id` | UUID | Obligatorio; referencia a `punto_frio.id`. |
| `usuario_id` | UUID | Obligatorio; referencia a `usuario.id`. |
| `iniciada_en` | fecha con zona | Obligatoria. |
| `cerrada_en` | fecha con zona | Opcional hasta cerrar la ronda. |
| `estado` | texto | `en_curso`, `completa` o `incompleta`; inicia en `en_curso`. |
| `sincronizada` | booleano | Inicia en `false`; identifica registros pendientes de subir. |

El índice por `punto_id` e `iniciada_en` soporta el historial por cámara y fecha.

## `paso_ronda`

Almacena el checklist como datos, sin recompilar la aplicación cuando cambie un paso.

| Campo | Tipo | Regla / significado |
|---|---|---|
| `id` | UUID | Identificador único. |
| `ronda_id` | UUID | Obligatorio; referencia a `ronda.id`. Se elimina si se elimina la ronda. |
| `orden` | entero | Obligatorio; único dentro de la ronda. |
| `descripcion` | texto | Obligatoria; texto del paso. |
| `completado` | booleano | Inicia en `false`. |
| `completado_en` | fecha con zona | Se llena cuando el paso se completa. |

## `inspeccion`

Guarda el resultado, evidencia y ubicación de una inspección. Es el centro de la trazabilidad.

| Campo | Tipo | Regla / significado |
|---|---|---|
| `id` | UUID | Identificador remoto único. |
| `id_local` | UUID | Obligatorio y único; evita duplicados en reintentos. |
| `ronda_id` | UUID | Obligatorio; referencia a `ronda.id`. |
| `lote_id` | UUID | Opcional; referencia a `lote.id`. |
| `foto_url` | texto | Opcional; ruta o URL de la evidencia en Storage. |
| `clase` | texto | Obligatoria: `integro`, `empaque_danado`, `contaminacion` o `etiqueta_ilegible`. |
| `confianza` | decimal | Entre 0 y 1; obligatoria si `origen = modelo`. |
| `origen` | texto | Obligatorio: `modelo` o `manual`. |
| `capturada_en` | fecha con zona | Obligatoria. |
| `lat`, `lon` | decimal | Coordenadas opcionales. |
| `precision_gps` | decimal | Margen de error de ubicación en metros. |
| `sincronizada` | booleano | Inicia en `false`. |

Los índices por lote, fecha y ronda soportan las consultas de historial.

## `alerta`

Registra una alerta generada a partir de una inspección y su atención por un supervisor.

| Campo | Tipo | Regla / significado |
|---|---|---|
| `id` | UUID | Identificador único. |
| `inspeccion_id` | UUID | Obligatorio; referencia a `inspeccion.id`. Se elimina si se elimina la inspección. |
| `tipo` | texto | Obligatorio; categoría de la alerta. |
| `severidad` | texto | Obligatoria: `baja`, `media` o `alta`. |
| `abierta_en` | fecha con zona | Se registra automáticamente al crearla. |
| `atendida_por` | UUID | Usuario que atendió la alerta; opcional mientras esté abierta. |
| `atendida_en` | fecha con zona | Fecha de atención; obligatoria si cambia de abierta. |
| `nota_atencion` | texto | Comentario opcional de la atención. |
| `estado` | texto | `abierta`, `atendida` o `cerrada`; inicia en `abierta`. |

Una alerta con estado distinto de `abierta` debe guardar tanto `atendida_por` como `atendida_en`.

## Relaciones para trazabilidad

```text
usuario ──< ronda >── punto_frio
                 └──< paso_ronda
ronda ──< inspeccion >── lote
inspeccion ──< alerta >── usuario (atendida_por)
```

La pantalla de historial parte de `inspeccion`, se une con `ronda` para obtener el punto frío y opcionalmente con `lote` para filtrar el producto.

