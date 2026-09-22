# Documentos de Vixta

Todo lo que se ha entregado del proyecto, en el repositorio, para que cualquiera del equipo
lo encuentre sin pedirlo por WhatsApp.

## Qué hay aquí

| Carpeta / archivo | Qué es | Cuándo se entregó |
|---|---|---|
| `AVANCE_01_Modelo_y_Arquitectura.pdf` | El avance 1: modelo de datos, arquitectura offline-first y sus tres diagramas | 2-sep-2026 |
| `ficha-tecnica/AF1_DISPMOV_Ficha_Tecnica.pdf` | Ficha técnica, logo y nombre | 28-ago-2026 |
| `ficha-tecnica/GANTT_Vixta_actualizado.xlsx` | **El cronograma vigente.** Las columnas son sesiones de brigada, no días naturales | 2-sep-2026 |
| `ficha-tecnica/GANTT_Vixta_Cronograma.pdf` | El mismo cronograma, para leerlo sin Excel | 2-sep-2026 |
| `mockups/Vixta_Mockups_e_Mapa.pdf` | **Los mockups y el mapa de navegación.** De aquí sale la paleta | 7-sep-2026 |
| `mockups/Vixta_Mockups_1er_Avance.pdf` | La versión anterior de los mockups | ago-2026 |
| `modelo-datos/esquema.sql` | **El esquema real**, ya ejecutado en Supabase. 7 tablas | 2-sep-2026 |
| `modelo-datos/modelo_datos_mermaid.png` | Diagrama entidad-relación | 2-sep-2026 |
| `modelo-datos/arquitectura_capas_mermaid.png` | Diagrama de capas | 2-sep-2026 |
| `modelo-datos/flujo_sincronizacion_mermaid.png` | Flujo de sincronización offline-first | 2-sep-2026 |
| `manuales/MANUAL_TECNICO.md` | Borrador del manual técnico: arquitectura, datos, seguridad, sincronización y pruebas | 22-sep-2026 |
| `manuales/CONSULTAS_TRAZABILIDAD.md` | Consultas de historial por lote, punto frío y rango de fechas | 21-sep-2026 |
| `manuales/MATRIZ_RLS.md` | Matriz de permisos para operador y supervisor | 21-sep-2026 |
| `manuales/DISENO_ALMACENAMIENTO_EVIDENCIA.md` | Diseño offline-first para fotos y evidencia en Supabase Storage | 22-sep-2026 |
| `manuales/` | Manual de usuario | pendiente, noviembre |

Los archivos `.mmd` son el código fuente de los diagramas: se editan como texto y se regeneran,
no hay que redibujarlos.

## Antes de escribir código, lee esto

| Si vas a tocar | Abre primero |
|---|---|
| Pantallas | `mockups/Vixta_Mockups_e_Mapa.pdf` y `app/src/main/java/com/vixta/app/ui/theme/Color.kt` |
| Base de datos, local o remota | `modelo-datos/esquema.sql` y el diagrama entidad-relación |
| Sincronización | `modelo-datos/flujo_sincronizacion_mermaid.png` |
| Cualquier cosa, para saber cuándo se entrega | `ficha-tecnica/GANTT_Vixta_actualizado.xlsx` |

## La paleta

Sale del mockup y ya está escrita en `app/src/main/java/com/vixta/app/ui/theme/Color.kt`.
**No se escriben colores en hex dentro de las pantallas** — se usan los nombres de ese archivo.

| Color | Hex | Para qué |
|---|---|---|
| Azul marino | `#0E1A45` | Primario: barras, encabezados, texto principal |
| Turquesa | `#14B8A6` | Acento: botones principales, elementos activos |
| Fondo | `#F6F8FC` | Fondo de pantalla |
| Superficie | `#E2E7F1` | Tarjetas y campos |
| Texto suave | `#4A5568` | Subtítulos y etiquetas |
| Verde | `#22B07D` | Estado correcto |
| Ámbar claro | `#FEF3DD` | Advertencia |
