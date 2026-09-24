# Diseño de almacenamiento de evidencia — Vixta

> **Estado:** propuesta técnica. No crea todavía un bucket ni modifica Supabase Storage.

## Objetivo

Cada inspección puede incluir una fotografía como evidencia. La foto debe permanecer disponible desde el historial, sin impedir que el inspector trabaje cuando no hay señal.

## Flujo offline-first

```text
Inspector toma foto
        ↓
Se guarda localmente en el teléfono
        ↓
Se crea la inspección con sincronizada = false
        ↓
WorkManager detecta conexión
        ↓
Sube el archivo a Supabase Storage
        ↓
Guarda la ruta remota en inspeccion.foto_url
        ↓
Marca la inspección como sincronizada
```

La interfaz nunca debe esperar a que termine la subida. Si falla, el archivo y la inspección quedan pendientes para reintento.

## Bucket propuesto

| Elemento | Propuesta | Motivo |
|---|---|---|
| Nombre | `evidencias` | Expresa el contenido y evita mezclar fotos con otros recursos. |
| Visibilidad | Privado | Las fotografías forman parte de una inspección y no deben tener un enlace público permanente. |
| Tipo admitido | `image/jpeg` y `image/png` | Formatos habituales de CameraX. |
| Tamaño máximo | Pendiente de confirmar | Debe equilibrar calidad de evidencia, almacenamiento y sincronización móvil. |

El nombre y la configuración final del bucket deben confirmarse con el equipo antes de crearlo.

## Ruta propuesta para cada foto

```text
evidencias/<ronda_id>/<id_local_inspeccion>.jpg
```

Ejemplo:

```text
evidencias/7b9a.../a12f....jpg
```

Usar `id_local_inspeccion` hace que la ruta sea estable antes de sincronizar y ayuda a evitar subir dos veces la misma evidencia durante un reintento.

## Relación con la base de datos

La tabla `inspeccion` ya contiene la columna `foto_url`.

- Sin conexión: la app conserva una ruta local temporal.
- Después de subir: la app guarda en `foto_url` la ruta remota del archivo, no una URL pública.
- Al mostrar el historial: la app solicita una URL firmada o descarga autorizada usando la sesión del usuario.

## Seguridad esperada

Las reglas de Storage deben ser consistentes con RLS:

| Rol | Acceso esperado |
|---|---|
| Operador | Subir y consultar evidencia de sus propias rondas. |
| Supervisor | Consultar evidencia del equipo desde el historial. |

**Pendiente:** las políticas de Storage requieren el mismo enlace confirmado entre `auth.uid()` y `usuario.id` que las políticas RLS.

## Pruebas planeadas

1. Capturar una foto sin red y confirmar que queda guardada localmente.
2. Recuperar conexión y confirmar que se sube una sola vez.
3. Abrir el historial y recuperar la foto con una sesión autorizada.
4. Verificar que un operador no puede leer evidencia ajena.
5. Verificar que el supervisor puede consultar evidencia del equipo.

## Decisiones pendientes del equipo

- Confirmar el nombre del bucket.
- Definir el tamaño máximo y la compresión de las imágenes.
- Definir cuándo se eliminan las copias locales tras una sincronización exitosa.
