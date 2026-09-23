# Registro de pruebas de WorkManager — Pablo

Proyecto: Vixta  
Fecha: 22 de septiembre de 2026  
Rama: `feature/datos-local`  
Entorno: emulador Pixel 7, Android 15, API 35.

## Qué hice

Agregué consultas de rondas e inspecciones pendientes y sus variantes con Flow. Incorporé WorkManager y una tarea temporal, PruebaColaWorker, que consulta Room. Probé ejecución en segundo plano, reintento simulado, recuperación después de terminar el proceso y espera por conexión.

## Cómo lo hice

- Los DAO seleccionan registros con sincronizada = 0, ordenados por fecha y por id_local. La lectura inicial de Flow se comprobó con first(); no se comprobó todavía la emisión continua ante cambios.
- MainActivity programa una tarea única llamada prueba-lectura-cola con ExistingWorkPolicy.KEEP. Evita otra tarea simultánea con ese nombre; no impide duplicados en el servidor.
- La tarea tiene un retraso inicial mínimo de 30 segundos. El primer intento devuelve Result.retry() de manera intencional. El segundo consulta Room y devuelve Result.success().
- Se configuró una espera de reintento lineal de dos minutos. Los tiempos son mínimos y dependen de la planificación de Android.
- Para comprobar recuperación, dejé Vixta en segundo plano y ejecuté adb shell am kill com.vixta.app después del primer intento, sin volver a abrir la aplicación.
- Añadí Constraints con NetworkType.CONNECTED. Ejecuté la aplicación con modo avión y Wi-Fi apagado; luego restauré la conexión sin volver a ejecutar la app.

## Qué entrego

| Prueba | Evidencia y resultado |
|---|---|
| Consulta de pendientes | Se recuperaron 1 ronda y 1 inspección. |
| Lectura inicial mediante Flow | Se recuperaron los mismos pendientes. |
| Ejecución en segundo plano | Se observó la lectura tras dejar la aplicación en el escritorio del emulador. |
| Reintento simulado | Logcat mostró intento 1, fallo simulado e intento 2 con lectura correcta. |
| Recuperación tras terminar el proceso | La tarea 29dde28a-8f30-4431-93fa-d492384aef82 pasó del intento 1 en el proceso 8731 al intento 2 en el proceso nuevo 8893. |
| Espera por conexión | Pablo confirmó que el intento 1 apareció después de activar Wi-Fi. Logcat mostró el intento 2 de la tarea 70069ed9-36d2-43dd-be9d-b8987a0b124a y la lectura correcta. La captura por sí sola no acredita el intervalo sin red. |

Commits comprobados en las capturas:

- 5f89883: consultas de pendientes y observación con Flow.
- 08695c0: WorkManager y lectura de pendientes en segundo plano.
- bc9b50b: prueba de reintento y recuperación de WorkManager.

La condición de conexión también fue guardada y subida, según el estado limpio y actualizado mostrado por git status; este registro no incluye su hash porque no se mostró en la captura final.

Evidencias compartidas en la conversación, aún no incorporadas como imágenes a este documento:

- Captura de pantalla 2026-09-22 002828.png: lectura de pendientes.
- Captura de pantalla 2026-09-22 190031.png: reintento simulado.
- Captura de pantalla 2026-09-22 191532.png: misma tarea en dos procesos distintos.
- Captura de pantalla 2026-09-22 193030.png: ejecución y reintento de la prueba de conexión.

Para reproducir, se requieren datos locales pendientes en el emulador. La tarea solo consulta; no crea los registros de prueba. El número de resultados puede variar en otro dispositivo. En Logcat se usa tag:PruebaWorker. Para la recuperación se debe terminar el proceso con am kill mientras la app está en segundo plano; no se probó forzar detención, reiniciar el dispositivo ni desinstalar la aplicación.

## Qué me bloqueó

La revisión de las ramas remotas encontró documentación del esquema, flujo y permisos, pero no un cliente remoto implementado. El manual del equipo deja pendientes autenticación, relación entre Auth y usuario, políticas RLS y configuración de Supabase.

Pendientes:

- Implementar el envío real, acordando identificadores, relaciones y respuesta del backend.
- Marcar registros sincronizados solo al confirmar recepción; conservar pendientes ante fallos.
- Probar reenvíos sin duplicados en el servidor mediante id_local.
- Integrar la programación al guardado de datos y retirar las pruebas temporales del inicio normal.
- Retirar el fallo simulado de la implementación final y probar errores reales de comunicación.
- Completar la integración y revisión con el equipo.

Result.success() en esta prueba significa que terminó la lectura local, no que se sincronizaron datos. NetworkType.CONNECTED no garantiza que Supabase esté accesible. Los resultados documentados validan esta tarea de prueba, no una sincronización completa.
