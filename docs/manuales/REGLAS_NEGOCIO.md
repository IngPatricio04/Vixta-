# Reglas de negocio — Vixta

**Actividad relacionada:** 19, “Reglas de negocio y endpoints”.  
**Fuente:** esquema de datos y flujo offline-first aprobados para Vixta.  
**Alcance:** reglas funcionales de puntos fríos, rondas, inspecciones y alertas. No ejecuta SQL ni modifica políticas RLS.

## 1. Puntos fríos

1. Cada punto frío tiene un `codigo` único; el QR contiene ese mismo valor.
2. Sólo puede iniciarse una ronda en un punto con `activo = true`.
3. El tipo de punto se limita a `camara`, `tarima` o `anden`.
4. Si el QR no corresponde a un punto existente o activo, la app informa el resultado y no inicia la ronda.

## 2. Rondas y operación sin conexión

1. Una ronda pertenece a un punto frío y a un usuario.
2. Al iniciarse, su estado es `en_curso`.
3. Al terminar, sólo puede quedar como `completa` o `incompleta`.
4. El teléfono genera `id_local` antes de sincronizar; dicho valor es único.
5. Si la red falla, la app conserva la ronda localmente y reintenta más tarde con el mismo `id_local`.
6. Reintentar el envío no debe duplicar la ronda ni sus inspecciones.

## 3. Checklist

1. Los pasos del checklist pertenecen a una ronda.
2. Cada paso tiene un número de `orden` único dentro de esa ronda.
3. Un paso inicia como no completado.
4. Al completarlo se registra `completado_en`.
5. Si faltan pasos al cerrar una ronda, el flujo debe permitir marcarla como `incompleta` con su evidencia correspondiente.

## 4. Inspecciones y clasificación

1. Toda inspección pertenece a una ronda; puede asociarse opcionalmente a un lote.
2. Las únicas clases permitidas son: `integro`, `empaque_danado`, `contaminacion` y `etiqueta_ilegible`.
3. Una clasificación puede venir del `modelo` o ser `manual`.
4. Si el origen es `modelo`, `confianza` es obligatoria y debe estar entre 0 y 1.
5. Si el modelo no ofrece una clasificación fiable, la aplicación continúa con clasificación manual; la inspección nunca debe perderse por esa condición.
6. Cada inspección recibe un `id_local` único para evitar duplicados durante la sincronización.

## 5. Ubicación y evidencia

1. La foto es opcional hasta que el equipo defina el flujo y bucket de Storage, pero su ruta se registrará en `foto_url` cuando exista.
2. Si se registran coordenadas (`lat` y `lon`), se debe incluir `precision_gps` como margen de error en metros.
3. La evidencia se captura y conserva localmente si no hay conexión; se sube junto con la inspección cuando la red regrese.
4. Nunca se suben fotos, ubicaciones o claves reales a documentos, repositorios ni capturas de prueba.

## 6. Alertas

1. Una alerta proviene de una inspección y se crea inicialmente en estado `abierta`.
2. La severidad sólo puede ser `baja`, `media` o `alta`.
3. Una alerta sólo puede terminar en `atendida` o `cerrada` si registra quién la atendió y cuándo (`atendida_por` y `atendida_en`).
4. La atención de alertas corresponde al rol supervisor una vez que RLS esté habilitado.

## 7. Trazabilidad y seguridad

1. El historial se consulta por lote, punto frío o rango de fechas.
2. El historial vincula una inspección con su ronda, punto frío y, si existe, lote.
3. Un operador sólo debe consultar y modificar sus propios registros.
4. Un supervisor puede consultar el historial del equipo y atender alertas.
5. Estas restricciones se aplicarán con Supabase Auth y RLS; quedan pendientes de definir el vínculo entre `auth.uid()` y `usuario.id`.

## Estado de aplicación

| Regla | Protección actual |
|---|---|
| Códigos, correos e `id_local` únicos | Restricciones de la base de datos. |
| Valores permitidos de estado, tipo, clase, origen y severidad | Restricciones `CHECK` de la base de datos. |
| Confianza obligatoria para modelo | Restricción de la base de datos. |
| Atención completa de alertas | Restricción de la base de datos. |
| Permisos por operador/supervisor | Pendiente de Auth + RLS. |
| Evidencia fotográfica en Storage | Pendiente de definir bucket y reglas. |

## Referencias

- [Ficha de endpoints](FICHA_ENDPOINTS.md)
- [Diccionario de datos](DICCIONARIO_DATOS.md)
- [Matriz RLS](MATRIZ_RLS.md)
- [Plan de pruebas de datos](PLAN_PRUEBAS_DATOS.md)

