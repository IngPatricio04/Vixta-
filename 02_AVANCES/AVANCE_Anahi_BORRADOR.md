# AVANCE — Anahi — semana pendiente de confirmar

> Renombrar este archivo como `AVANCE_Anahi_S<n>.md` cuando el equipo confirme el número de semana.

## Qué hice

- Instalé y configuré Git en mi computadora con mi identidad de GitHub.
- Cloné el repositorio de Vixta, creé la rama `feature/datos-endpoints` y abrí el proyecto en Android Studio.
- Obtuve acceso al proyecto de Supabase y verifiqué las siete tablas del esquema remoto.
- Documenté consultas de trazabilidad por lote, punto frío y rango de fechas.
- Documenté la matriz de permisos RLS para operador y supervisor.
- Creé la estructura inicial del manual técnico.
- Documenté el diseño offline-first para el almacenamiento de fotos y evidencia.
- Actualicé el índice de `docs/README.md` para enlazar los documentos creados.

## Cómo lo hice

- Revisé el esquema SQL real y los documentos de arquitectura antes de proponer cambios.
- Mantuve las consultas como lecturas, sin modificar datos de Supabase.
- Documenté RLS antes de ejecutarlo porque aún falta confirmar cómo `auth.uid()` se enlaza con `usuario.id`.
- Usé una rama propia y commits en español para no trabajar directamente sobre `main`.
- Con apoyo de Codex, preparé los documentos, verifiqué el estado del repositorio y registré los pendientes técnicos.

## Qué entrego

- Rama `feature/datos-endpoints`, disponible en GitHub.
- `docs/manuales/CONSULTAS_TRAZABILIDAD.md`: contiene tres consultas de sólo lectura para el historial.
  Se verifica revisando que cubren lote, punto frío y rango de fechas, usando las relaciones del esquema.
- `docs/manuales/MATRIZ_RLS.md`: define permisos de operador y supervisor.
  Se verifica revisando las reglas por tabla y los casos de prueba planeados.
- `docs/manuales/MANUAL_TECNICO.md`: documenta arquitectura, datos, sincronización, seguridad y pruebas.
  Se verifica abriendo el archivo y comprobando sus secciones.
- `docs/manuales/DISENO_ALMACENAMIENTO_EVIDENCIA.md`: define el flujo de fotos sin conexión y su posterior sincronización.
  Se verifica revisando el flujo offline-first, la ruta propuesta y las pruebas planeadas.

## Qué me bloqueó

- Falta confirmar con Cristopher cómo se relacionan las cuentas de Supabase Auth con `public.usuario.id`.
- La tabla `usuario` aún no tiene un operador ni un supervisor de prueba; por ello no se pueden ejecutar ni probar las políticas RLS.
- Falta confirmar el alcance definitivo entre `datos/remoto` y las responsabilidades de backend/reglas/endpoints antes de integrar código remoto en la app.
- El emulador Android no se pudo instalar porque la descarga de la imagen del sistema se interrumpió; se reintentará con una conexión más estable o una imagen más ligera.
