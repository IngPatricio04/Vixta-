# Dataset público preparado · actividad 23

Preparado con `entrenamiento/preparar_dataset.py` (semilla 42). Las imágenes y `indice.csv` están fuera del repositorio, en `04_CRUDO/dataset_publico/preparado/`. Cada archivo del índice tiene su fuente, paquete y ruta de origen. La partición se hizo por paquete y por fuente: 70/15/15 aproximado, con redondeo a paquetes enteros. No se repite un paquete de una fuente en dos particiones.

## Fuentes y reglas de etiquetado

| Fuente | Cita | Licencia | Mapeo a Vixta |
|---|---|---|---|
| Corrugated Cardboard Boxes Dataset, 990 fotos de 52 paquetes | Z. Chen *et al.*, «Deformation and penetration hybrid detection-net for parcels inspection in industrial supply chain», *ICASSP 2024*, IEEE, pp. 5815–5819. [Repositorio](https://github.com/chanllon/corrugated-cardboard-boxes-dataset) | Sin licencia explícita; el repositorio pide citar. Uso académico. | Anotación `damage` → `empaque_danado`; sólo `box` → `integro`. Se descarta si falta `box` o éste toca el borde. |
| TAMPAR, 724 recortes utilizables de 30 paquetes | A. Naumann, F. Hertlein, L. Dörr y K. Furmans, «TAMPAR: Visual Tampering Detection for Parcels Logistics in Postal Supply Chains», *WACV 2024*. [DOI 10.5281/zenodo.10057090](https://doi.org/10.5281/zenodo.10057090) | CC BY 4.0 | Recorte con `estado` que empieza por `ok` → `integro` difícil, según el encargo de esta prueba. |

Corrugated se recortó al rectángulo `box` con 3 % de margen y lado mayor de 640 px, para concentrar el modelo en el paquete. Los recortes TAMPAR se copiaron sin alterar. Se quitaron duplicados exactos por SHA-256 antes de asignar paquetes a particiones.

## Imágenes por clase, partición y fuente

| Clase | Partición | Corrugated | TAMPAR | Total |
|---|---:|---:|---:|---:|
| `integro` | train | 440 | 504 | 944 |
| `integro` | val | 99 | 119 | 218 |
| `integro` | test | 80 | 101 | 181 |
| `empaque_danado` | train | 243 | 0 | 243 |
| `empaque_danado` | val | 61 | 0 | 61 |
| `empaque_danado` | test | 56 | 0 | 56 |
| **Total** | **train** | **683** | **504** | **1187** |
| **Total** | **val** | **160** | **119** | **279** |
| **Total** | **test** | **136** | **101** | **237** |
| **Total** | **todas** | **979** | **724** | **1703** |

| Partición | Paquetes Corrugated | Paquetes TAMPAR | Total |
|---|---:|---:|---:|
| train | 36 | 21 | 57 |
| val | 8 | 5 | 13 |
| test | 8 | 4 | 12 |
| **Total** | **52** | **30** | **82** |

## Descartes

| Fuente | Motivo | Archivos |
|---|---|---:|
| Corrugated | `box` a menos de 8 px del borde | 7 |
| Corrugated | Duplicado exacto (SHA-256) | 4 |
| TAMPAR | Descartado durante la descarga: cortado por el borde, según `recortes_indice.csv` | 8 |

De los 990 pares JPG/JSON de Corrugated quedaron 979 imágenes. El índice TAMPAR registra 732 filas: 724 `ok` y 8 descartadas antes de esta preparación. No hubo imágenes ilegibles ni otros motivos de descarte en esta corrida.

`integro` tiene 1343 imágenes y `empaque_danado` 360. No se eliminaron imágenes para equilibrar las clases; el entrenamiento utiliza pesos por clase.

Las clases 3 y 4 no están en fuentes públicas; salen del dataset propio (actividad 24).
