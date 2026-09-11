# AVANCE — Pablo Rafael Saavedra Contreras — semana 5

Proyecto: Vixta  
Fecha: 11 de septiembre de 2026  
Responsabilidad: persistencia local y sincronización

## Qué hice

- Configuré Room 2.8.4 y KSP 2.3.9 en el proyecto Android existente.
- Definí las entidades locales PuntoFrioEntity, UsuarioEntity, RondaEntity, LoteEntity e InspeccionEntity, tomando como referencia el esquema SQL del equipo.
- Creé los DAO de puntos, usuarios, rondas e inspecciones para insertar y recuperar registros.
- Creé VixtaDatabase y un proveedor que reutiliza una misma instancia de Room.
- Verifiqué la creación de vixta.db y de sus tablas en el emulador Pixel 7, API 35.
- Implementé una migración automática de la versión 1 a la 2 y comprobé que conservó el punto de prueba existente.
- Guardé una inspección manual simulada, relacionada con una ronda y un usuario simulados, y la recuperé mediante su id_local.
- Comprobé la lectura de esa inspección después de reiniciar el proceso de la app, sin volver a insertarla.
- Guardé y subí los dos avances de código a la rama feature/datos-local.

## Cómo lo hice

1. Instalé y configuré Git, cloné el repositorio, abrí el proyecto en Android Studio y comprobé que la aplicación base podía ejecutarse. Creé la rama feature/datos-local según el brief y el README del equipo.
2. Agregué las dependencias de Room y el complemento KSP. Room gestiona SQLite en el teléfono y KSP genera las implementaciones de la base y los DAO.
3. Primero definí punto_frio, inserté un punto simulado y verifiqué que seguía guardado al reabrir la app después de retirar la inserción.
4. Añadí usuario, ronda, lote e inspeccion. Las claves foráneas relacionan la ronda con un usuario y un punto, y la inspección con una ronda y, opcionalmente, un lote. Los índices únicos protegen los códigos, correos e identificadores locales correspondientes.
5. Conservé el esquema exportado 1.json y configuré AutoMigration de 1 a 2. Room generó 2.json y la actualización agregó las tablas sin borrar el punto existente.
6. Implementé una función temporal de prueba que inserta usuario, ronda e inspección y compara la inspección recuperada con el objeto original. Después sustituí su llamada por una consulta de solo lectura a la inspección existente y comprobé en Logcat el mensaje LECTURA CORRECTA.
7. Retiré de MainActivity la llamada que generaba registros y la comprobación dependiente de ese identificador específico. Permanece una consulta temporal para abrir la base. La función de prueba continúa en PruebaInspeccionLocal.kt, sin llamada automática desde MainActivity.

Usé asistencia de IA para entender los archivos del equipo, preparar cada entidad y DAO, explicar sus campos y guiar la configuración y las comprobaciones. Fui incorporando el código paso por paso en Android Studio y compartiendo los resultados para revisión.

Decisiones locales de esta implementación: UUID representados como texto, fechas como texto ISO 8601 en UTC y valores decimales como Double. Su conversión al backend y la correspondencia entre id e id_local todavía deben acordarse con el equipo. Los campos sincronizada empiezan en false; esto prepara los datos pendientes, pero no constituye todavía una cola operativa.

## Qué entrego

Rama: `feature/datos-local`  
Repositorio: https://github.com/IngPatricio04/Vixta-

Commits de código verificados:

- `f7bd6e2`: agrega persistencia local de puntos frios con Room.
- `7cde10e`: agrega persistencia de rondas e inspecciones y migracion a version 2.

Ubicación del código: `app/src/main/java/com/vixta/app/datos/local/`. Configuración en los archivos Gradle y el catálogo de versiones; esquemas exportados en `app/schemas/com.vixta.app.datos.local.VixtaDatabase/`.

### Cómo se verificó

| Comprobación | Resultado observado |
|---|---|
| Ejecutar la app y abrir Database Inspector | Se encontró vixta.db con punto_frio y sus seis columnas. |
| Insertar y consultar un punto simulado | Se encontró PRUEBA-CAMARA-01, Cámara de prueba. |
| Reiniciar con una consulta sin inserción | El punto permaneció en la tabla. |
| Migrar de versión 1 a 2 | Aparecieron usuario, ronda, lote e inspeccion; el punto anterior permaneció. |
| Ejecutar la prueba de inspección | Se observó una inspección con clase integro, origen manual y sincronizada = 0. |
| Reiniciar y consultar por id_local mediante el DAO | Logcat mostró LECTURA CORRECTA después de comprobar existencia, clase, origen y estado pendiente. |

Identificador local de la inspección utilizada: `bbb8fb46-569f-4502-9a72-3dcf44f096d4`. Este registro existe en el emulador de prueba; no se incluye en Git ni debe suponerse presente en otro dispositivo.

Para inspeccionar el resultado en ese emulador: ejecutar Vixta, abrir App Inspection, seleccionar el proceso com.vixta.app y revisar vixta.db en Database Inspector. Para repetir la inserción en otro entorno, primero hay que preparar el punto PRUEBA-CAMARA-01 y llamar temporalmente a probarInspeccionLocal; cada llamada genera un conjunto nuevo de datos simulados. No se dejó activada esa llamada en el inicio normal de la aplicación.

Evidencia visual compartida en la conversación, todavía no incorporada como archivos al repositorio:

- Captura de pantalla 2026-09-10 224638.png: nuevas tablas y conservación del punto tras la migración.
- Captura de pantalla 2026-09-10 225118.png: inspección almacenada.
- Captura de pantalla 2026-09-10 231020.png: reinicio del proceso y mensaje LECTURA CORRECTA.

La meta funcional prevista para el 11 de septiembre, guardar y leer una inspección, se comprobó con datos simulados. No se probó todavía la captura de foto o GPS, el envío remoto ni la sincronización. La inspección de prueba fue manual, sin lote, foto ni coordenadas; esos campos admiten null en el SQL proporcionado.

## Qué me bloqueó

- Al principio faltaba Git y el código del proyecto en mi computadora; quedó resuelto con la instalación y clonación.
- El emulador y Database Inspector perdieron conexión durante las pruebas; se recuperó la conexión sin borrar la base.
- No apareció inicialmente el mensaje de la primera prueba en Logcat. No se determinó la causa; se verificó la lectura con una consulta posterior de solo lectura y un mensaje visible.

Pendientes que requieren coordinación:

- Cristopher: confirmar el alcance local restante, especialmente paso_ronda y alerta, y el acuerdo de identificadores y relaciones para la sincronización.
- Anahi y Cristopher: acordar cómo se relacionan los id locales con los remotos, el tratamiento de reintentos y registros ya recibidos, los formatos de fecha y precisión decimal y el envío de fotos.
- Cristian y Patricio: integrar los DAO a través de la capa de acceso a datos que utilizarán las pantallas. La consulta de apertura en MainActivity sigue siendo temporal.
- Patricio: revisar la rama antes de integrar a main. Los commits están publicados en la rama; no se ha creado un Pull Request ni realizado la integración en este flujo.

Trabajo propio pendiente para el siguiente hito:

- Implementar la consulta de pendientes, la cola y WorkManager con reintentos.
- Probar que los envíos repetidos no crean duplicados en el servidor.
- Completar las validaciones locales pendientes, incluida la restricción de tipo de punto frío.
- Sustituir la apertura temporal y organizar las pruebas fuera del flujo normal de inicio.
- Completar las pruebas integrales sin conexión y la documentación de la entrega del 16 de septiembre.
