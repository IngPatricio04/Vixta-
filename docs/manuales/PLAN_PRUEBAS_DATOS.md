# Plan de pruebas de datos y endpoints — Vixta

**Actividad relacionada:** 19, Reglas de negocio y endpoints.  
**Estado:** plan listo; las pruebas que requieran Auth, RLS, Storage o la app Android permanecen pendientes.

## Objetivo

Comprobar que la aplicación pueda consultar puntos fríos, sincronizar información sin duplicados y conservar trazabilidad de las inspecciones. Este documento no ejecuta pruebas ni inserta datos adicionales en Supabase.

## Preparación segura

- Usar únicamente datos simulados, nunca nombres, correos, fotos o ubicaciones reales.
- La clave publishable se conserva sólo en configuración local; ninguna clave se registra en capturas, Git o documentación.
- No ejecutar `esquema.sql`, pues contiene instrucciones que eliminan las tablas existentes.
- Antes de pruebas de escritura, usar una cuenta de operador y otra de supervisor creadas por el equipo.

## Datos de prueba disponibles

| Dato | Valor | Uso |
|---|---|---|
| Punto frío | `CAM-DEMO-01` | Consulta por código QR. |
| Nombre esperado | Cámara demo · Lácteos | Confirmar que la lectura devuelve el punto correcto. |

## Casos de prueba

| ID | Caso | Precondición | Acción | Resultado esperado | Estado actual |
|---|---|---|---|---|---|
| DAT-01 | QR válido | Sesión con permiso de lectura. | Consultar `CAM-DEMO-01`. | Se devuelve el punto Cámara demo · Lácteos. | Dato preparado; pendiente cliente REST/Android. |
| DAT-02 | QR inexistente | Sesión con permiso de lectura. | Consultar un código que no exista. | Lista vacía; la app informa “Punto frío no encontrado o inactivo”. | Pendiente. |
| DAT-03 | Punto inactivo | Punto de prueba inactivo autorizado por el equipo. | Consultar su código. | No se permite iniciar una ronda. | Pendiente; no crear sin acuerdo. |
| DAT-04 | Inicio de ronda | Operador autenticado y punto activo. | Enviar una ronda con `id_local` nuevo. | Se crea una ronda `en_curso`. | Pendiente Auth/RLS. |
| DAT-05 | Reintento de ronda | Ronda creada con `id_local` conocido. | Reenviar el mismo `id_local`. | No se duplica el registro. | Pendiente Auth/RLS. |
| DAT-06 | Orden único del checklist | Ronda existente. | Registrar dos pasos con mismo `ronda_id` y `orden`. | La base rechaza el duplicado. | Pendiente. |
| DAT-07 | Inspección manual | Ronda existente. | Guardar una inspección con `origen = manual`. | Se guarda sin requerir `confianza`. | Pendiente Auth/RLS. |
| DAT-08 | Modelo sin confianza | Ronda existente. | Guardar `origen = modelo` sin `confianza`. | La base rechaza el registro. | Pendiente Auth/RLS. |
| DAT-09 | Historial por punto | Al menos una inspección existente. | Filtrar historial por `CAM-DEMO-01`. | Devuelve inspecciones ordenadas por fecha descendente. | Pendiente vista/RPC. |
| DAT-10 | Restricción de operador | Operador y supervisor de prueba. | Operador intenta atender una alerta. | RLS rechaza la operación. | Pendiente Auth/RLS. |

## Evidencia que se debe guardar al ejecutar cada caso

1. Captura de la solicitud o de la pantalla Android, sin claves visibles.
2. Resultado de éxito o error esperado.
3. Fecha, caso ID y responsable de la prueba.
4. Si falla: mensaje exacto, pasos para reproducirlo y si bloquea el flujo.

## Criterios de salida para esta etapa

- DAT-01 y DAT-02 funcionan desde el cliente elegido.
- DAT-04 a DAT-08 se prueban después de que el equipo active Auth/RLS.
- DAT-09 sólo se marca como realizado cuando exista una vista o función RPC aprobada para trazabilidad.
- DAT-10 se ejecuta con dos roles de prueba y deja evidencia de que los permisos se aplican en la base.

## Dependencias

- Vínculo definido entre Supabase Auth y `public.usuario`.
- Políticas RLS acordadas y ejecutadas.
- Datos simulados de lote, ronda, inspección y alerta autorizados por el equipo.
- Para evidencia fotográfica: bucket y reglas de Storage definidos.

## Registro de ejecución

| Fecha | Caso | Resultado | Evidencia / observación |
|---|---|---|---|
| Pendiente | DAT-01 a DAT-10 | — | El plan se preparó antes de integrar Auth y Android. |

