# Manual técnico — Vixta

> **Estado:** borrador inicial · septiembre de 2026
>
> Este manual describe la arquitectura implementada y las decisiones técnicas del proyecto. Los apartados marcados como **pendientes** se completarán conforme se integren y prueben los módulos.

## 1. Propósito del sistema

Vixta es una aplicación Android para inspecciones de cadena de frío. Permite registrar rondas en puntos fríos, capturar evidencia de una inspección y consultar su trazabilidad.

La aplicación se diseña para funcionar aun sin señal: el usuario registra primero en el teléfono y la información se sincroniza con el servidor cuando vuelve a haber red.

## 2. Arquitectura general

```text
Inspector
   ↓
Aplicación Android (Kotlin + Jetpack Compose)
   ↓ escribe siempre primero
Base local Room / SQLite
   ↓ cuando hay red, mediante WorkManager
Supabase (PostgreSQL + Storage + Auth)
```

El modelo de visión se ejecuta en el teléfono con TensorFlow Lite. La foto y su clasificación forman parte de la evidencia de la inspección.

## 3. Tecnologías

| Capa | Tecnología | Uso |
|---|---|---|
| Aplicación | Kotlin | Lenguaje principal de Android. |
| Interfaz | Jetpack Compose + Material 3 | Pantallas y componentes visuales. |
| Arquitectura de interfaz | MVVM + StateFlow | Separación entre pantalla, estado y lógica. |
| Persistencia local | Room sobre SQLite | Operación sin conexión. |
| Base remota | Supabase / PostgreSQL | Historial, trazabilidad y datos compartidos. |
| Cámara | CameraX | Captura de fotos de evidencia. |
| Escaneo | ML Kit Barcode | Lectura de QR y códigos de barras sin red. |
| Sincronización | WorkManager | Envío de pendientes cuando hay red. |
| Clasificación | TensorFlow Lite | Modelo de visión en el dispositivo. |

## 4. Estructura del repositorio

| Ruta | Responsabilidad |
|---|---|
| `app/` | Proyecto Android. |
| `app/src/main/java/com/vixta/app/` | Código Kotlin, pantallas y futuras capas de datos. |
| `docs/modelo-datos/` | Esquema SQL y diagramas de base de datos. |
| `docs/manuales/` | Manual técnico, manual de usuario y documentos de trazabilidad. |
| `modelo-ia/` | Entrenamiento y recursos del modelo de clasificación. |

## 5. Modelo de datos remoto

Supabase contiene siete tablas en el esquema `public`:

| Tabla | Función |
|---|---|
| `usuario` | Inspector o supervisor identificado por rol. |
| `punto_frio` | Cámara, tarima o andén identificado por QR. |
| `lote` | Producto/lote consultable en trazabilidad. |
| `ronda` | Visita de un usuario a un punto frío. |
| `paso_ronda` | Pasos del checklist de una ronda. |
| `inspeccion` | Evidencia: clase, foto, fecha, GPS y lote. |
| `alerta` | Evento originado por una inspección y su atención. |

Las referencias completas y el SQL de creación se encuentran en [`../modelo-datos/esquema.sql`](../modelo-datos/esquema.sql).

### Trazabilidad e idempotencia

Las tablas `ronda` e `inspeccion` incluyen `id_local`, un UUID generado por el teléfono. Es único para evitar registros duplicados si la cola vuelve a enviar un elemento tras una conexión inestable.

La evidencia guarda también `precision_gps`, ya que una ubicación sin margen de error no acredita la presencia del inspector en el punto.

## 6. Sincronización offline-first

1. El usuario realiza una acción en la aplicación.
2. La información se guarda primero en Room.
3. El elemento queda con `sincronizada = false`.
4. WorkManager detecta una conexión disponible.
5. El registro se envía a Supabase usando su `id_local`.
6. Si el envío es exitoso, se marca como sincronizado; si falla, permanece pendiente para reintento.

**Pendiente:** integrar y probar la capa de Room, WorkManager y el cliente remoto en la aplicación.

## 7. Seguridad y roles

Supabase tiene RLS habilitado en `ronda`, `inspeccion` y `alerta`.

- Un **operador** sólo podrá crear y consultar sus propias rondas y evidencias.
- Un **supervisor** podrá consultar la información del equipo y atender alertas.

La matriz de permisos y el plan de pruebas se encuentran en [`MATRIZ_RLS.md`](MATRIZ_RLS.md).

**Pendiente:** definir y probar cómo el UUID de Supabase Auth (`auth.uid()`) se enlaza con `usuario.id` antes de ejecutar políticas RLS.

## 8. Consultas de trazabilidad

La pantalla Historial podrá consultar evidencias por lote, punto frío y rango de fechas. Las consultas iniciales están documentadas en [`CONSULTAS_TRAZABILIDAD.md`](CONSULTAS_TRAZABILIDAD.md).

El esquema ya cuenta con índices para lote, fecha, ronda y punto frío.

## 9. Configuración local y ejecución

1. Clonar el repositorio de Vixta.
2. Abrir la carpeta raíz del proyecto en Android Studio.
3. Esperar la sincronización de Gradle.
4. Usar una rama de trabajo; para este módulo: `feature/datos-endpoints`.
5. No subir `local.properties`, contraseñas ni llaves de Supabase a GitHub.

**Pendiente:** documentar el mecanismo aprobado para proporcionar la URL y clave pública de Supabase a la app sin exponer secretos.

## 10. Pruebas técnicas planeadas

| Caso | Resultado esperado |
|---|---|
| Registrar una ronda sin red | La ronda se conserva localmente y queda pendiente de sincronización. |
| Reintentar un envío | `id_local` evita un registro remoto duplicado. |
| Consultar por lote | Se devuelve sólo la evidencia del lote buscado. |
| Consultar por punto frío | Se devuelve la evidencia de ese punto. |
| Rol operador | No puede consultar registros ajenos. |
| Rol supervisor | Puede consultar el historial del equipo y atender alertas. |

## 11. Estado actual y próximos pasos

Completado:

- Esquema remoto de siete tablas disponible en Supabase.
- Consultas de trazabilidad documentadas.
- Matriz preliminar de permisos RLS documentada.

Pendiente:

- Cuentas de prueba de operador y supervisor en Supabase Auth.
- Enlace verificable entre Auth y `usuario`.
- Políticas RLS ejecutadas y probadas.
- Integración del cliente Supabase, Room y WorkManager en Android.
- Almacenamiento de fotos en Supabase Storage.
