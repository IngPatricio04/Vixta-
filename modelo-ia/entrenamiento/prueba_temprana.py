"""Entrena la línea base de dos clases y evalúa sólo la partición test."""

from __future__ import annotations

import argparse
import csv
import json
import os
import time
from collections import Counter
from datetime import datetime
from pathlib import Path

os.environ.setdefault("TF_CPP_MIN_LOG_LEVEL", "2")
matplotlib_cache = Path(__file__).resolve().parent / "salidas" / ".matplotlib"
matplotlib_cache.mkdir(parents=True, exist_ok=True)
os.environ.setdefault("MPLCONFIGDIR", str(matplotlib_cache))

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import tensorflow as tf
from sklearn.metrics import accuracy_score, classification_report, confusion_matrix
from sklearn.utils.class_weight import compute_class_weight


CLASSES = ("integro", "empaque_danado")
LABELS = {name: index for index, name in enumerate(CLASSES)}
DEFAULT_DATA = Path(
    r"C:\Users\zenin\OneDrive\Documentos\PROYPER\UNI\DISMOV_CO\PIA\Vixta\04_CRUDO\dataset_publico\preparado"
)


def read_index(root: Path) -> dict[str, list[dict]]:
    with (root / "indice.csv").open(encoding="utf-8", newline="") as stream:
        rows = list(csv.DictReader(stream))
    by_split = {split: [] for split in ("train", "val", "test")}
    for row in rows:
        if row["particion"] not in by_split or row["clase"] not in LABELS:
            raise ValueError(f"Fila inesperada en indice.csv: {row}")
        path = root / row["archivo"]
        if not path.is_file():
            raise FileNotFoundError(path)
        row["ruta"] = str(path)
        by_split[row["particion"]].append(row)
    if any(not rows for rows in by_split.values()):
        raise ValueError("Las tres particiones deben tener imágenes")
    packages = {}
    for split, split_rows in by_split.items():
        for row in split_rows:
            key = row["fuente"], row["paquete"]
            if key in packages and packages[key] != split:
                raise AssertionError(f"Paquete {key} aparece en {packages[key]} y {split}")
            packages[key] = split
    return by_split


def dataset(rows: list[dict], training: bool, batch_size: int) -> tf.data.Dataset:
    paths = [row["ruta"] for row in rows]
    labels = np.asarray([LABELS[row["clase"]] for row in rows], dtype=np.int32)
    data = tf.data.Dataset.from_tensor_slices((paths, labels))
    if training:
        data = data.shuffle(len(rows), seed=42, reshuffle_each_iteration=True)

    def load(path: tf.Tensor, label: tf.Tensor) -> tuple[tf.Tensor, tf.Tensor]:
        image = tf.io.decode_jpeg(tf.io.read_file(path), channels=3)
        image = tf.image.resize(image, (224, 224))
        image.set_shape((224, 224, 3))
        return image, label

    return data.map(load, num_parallel_calls=tf.data.AUTOTUNE).batch(batch_size).prefetch(tf.data.AUTOTUNE)


def build_model() -> tf.keras.Model:
    backbone = tf.keras.applications.MobileNetV2(
        input_shape=(224, 224, 3), include_top=False, weights="imagenet"
    )
    backbone.trainable = False
    inputs = tf.keras.Input(shape=(224, 224, 3), name="imagen")
    augmentation = tf.keras.Sequential([
        tf.keras.layers.RandomFlip("horizontal", seed=42),
        tf.keras.layers.RandomRotation(0.03, fill_mode="nearest", seed=42),
    ], name="aumento_solo_entrenamiento")
    x = augmentation(inputs)
    x = tf.keras.applications.mobilenet_v2.preprocess_input(x)
    x = backbone(x, training=False)
    x = tf.keras.layers.GlobalAveragePooling2D()(x)
    x = tf.keras.layers.Dropout(0.2)(x)
    outputs = tf.keras.layers.Dense(2, activation="softmax", name="clase")(x)
    model = tf.keras.Model(inputs, outputs, name="vixta_prueba_temprana")
    model.compile(
        optimizer=tf.keras.optimizers.Adam(learning_rate=1e-3),
        loss="sparse_categorical_crossentropy", metrics=["accuracy"],
    )
    return model


def save_matrix(matrix: np.ndarray, path: Path) -> None:
    fig, ax = plt.subplots(figsize=(5.8, 5.1))
    ax.imshow(matrix, cmap="Blues")
    for i in range(2):
        for j in range(2):
            ax.text(j, i, str(matrix[i, j]), ha="center", va="center", fontsize=15,
                    color="white" if matrix[i, j] > matrix.max() / 2 else "black")
    ax.set_xticks(range(2), CLASSES, rotation=20, ha="right")
    ax.set_yticks(range(2), CLASSES)
    ax.set_xlabel("Predicción")
    ax.set_ylabel("Clase real")
    ax.set_title("Matriz de confusión · test")
    fig.tight_layout()
    fig.savefig(path, dpi=160)
    plt.close(fig)


def export_tflite(model: tf.keras.Model, output: Path) -> tuple[Path, str]:
    converter_method = "from_keras_model"
    try:
        converter = tf.lite.TFLiteConverter.from_keras_model(model)
        converter.optimizations = [tf.lite.Optimize.DEFAULT]
        result = converter.convert()
    except Exception as exc:
        print(f"Conversión directa falló ({type(exc).__name__}: {exc}); se usa SavedModel.", flush=True)
        saved_dir = output / "saved_model_conversion"
        model.export(str(saved_dir))
        converter = tf.lite.TFLiteConverter.from_saved_model(str(saved_dir))
        converter.optimizations = [tf.lite.Optimize.DEFAULT]
        result = converter.convert()
        converter_method = "from_saved_model"
    path = output / "vixta_prueba_temprana.tflite"
    path.write_bytes(result)
    return path, converter_method


def benchmark_tflite(path: Path, sample: str) -> float:
    interpreter = tf.lite.Interpreter(model_path=str(path), num_threads=1)
    interpreter.allocate_tensors()
    detail = interpreter.get_input_details()[0]
    with tf.io.gfile.GFile(sample, "rb") as stream:
        image = tf.io.decode_jpeg(stream.read(), channels=3)
    image = tf.image.resize(image, (224, 224))
    value = np.expand_dims(image.numpy(), axis=0).astype(np.float32)
    if detail["dtype"] != np.float32:
        scale, zero = detail["quantization"]
        value = np.round(value / scale + zero).astype(detail["dtype"])
    interpreter.set_tensor(detail["index"], value)
    for _ in range(5):
        interpreter.invoke()
    start = time.perf_counter()
    for _ in range(50):
        interpreter.invoke()
    return (time.perf_counter() - start) * 1000 / 50


def render_report(result: dict, path: Path) -> None:
    metrics = result["metricas"]
    rows = []
    for cls in CLASSES:
        values = metrics[cls]
        rows.append(
            f"| `{cls}` | {int(values['support'])} | {values['precision']:.4f} | "
            f"{values['recall']:.4f} | {values['f1-score']:.4f} |"
        )
    source_rows = []
    for source in ("corrugated", "tampar"):
        values = result["fuentes"][source]
        source_rows.append(
            f"| {source} | {values['total']} | {values['correctos']} | {values['exactitud']:.4f} | "
            f"{values['integro_total']} | {values['integro_correctos']} | "
            f"{values['integro_exactitud']:.4f} |"
        )
    corrugated = result["fuentes"]["corrugated"]
    tampar = result["fuentes"]["tampar"]
    if tampar["exactitud"] == 1 and corrugated["exactitud"] < .8:
        source_warning = (
            f"**Se observó la señal de sesgo de fuente A27:** TAMPAR fue perfecto "
            f"({tampar['correctos']}/{tampar['total']}), mientras Corrugated obtuvo "
            f"{corrugated['correctos']}/{corrugated['total']} "
            f"({corrugated['exactitud']:.1%}). El modelo parece aprender rasgos de la fuente "
            f"además del daño; la exactitud global de {result['exactitud']:.2%} "
            "no representa el rendimiento en una fuente nueva."
        )
    else:
        source_warning = "La diferencia entre fuentes puede indicar aprendizaje de rasgos de procedencia."
    matrix = result["matriz"]
    text = f"""# Prueba temprana · actividad 25

## Dataset y configuración

Se usó el [dataset preparado](../CONTEO_DATASET.md), con partición por paquete y fuente. `train`: {result['imagenes']['train']} imágenes; `val`: {result['imagenes']['val']}; `test`: {result['imagenes']['test']}. La semilla fue 42. TensorFlow {result['tensorflow']}.

MobileNetV2 con pesos ImageNet y base congelada; `GlobalAveragePooling2D` → `Dropout(0.2)` → `Dense(2, softmax)`. Entrada 224×224 con `preprocess_input`. Sólo durante entrenamiento hubo volteo horizontal y rotación leve (factor 0.03). Adam 1e-3, máximo 10 épocas, `EarlyStopping` por `val_loss`, paciencia 3 y restauración de los mejores pesos. Se completaron {result['epocas']} épocas en **{result['segundos_entrenamiento']:.1f} s**. Pesos calculados sólo con `train`: `integro` {result['pesos']['integro']:.4f}, `empaque_danado` {result['pesos']['empaque_danado']:.4f}. La corrida completa tardó {result['segundos_totales']:.1f} s. El detalle reproducible está en `salidas/corrida.log` (fuera de Git).

## Evaluación en `test` únicamente

**Exactitud global: {result['exactitud']:.4f} ({result['correctos']}/{result['imagenes']['test']}).**

| Clase | Soporte | Precisión | Exhaustividad | F1 |
|---|---:|---:|---:|---:|
{chr(10).join(rows)}

Matriz de confusión (filas: clase real; columnas: predicción):

| Real \\ predicción | `integro` | `empaque_danado` |
|---|---:|---:|
| `integro` | {matrix[0][0]} | {matrix[0][1]} |
| `empaque_danado` | {matrix[1][0]} | {matrix[1][1]} |

![Matriz de confusión](matriz_confusion.png)

| Fuente | Total `test` | Correctas | Exactitud global | `integro` | `integro` correctas | Exactitud de `integro` |
|---|---:|---:|---:|---:|---:|---:|
{chr(10).join(source_rows)}

TAMPAR sólo aporta ejemplos `integro`; por eso su exactitud global coincide con su exactitud de `integro`. {source_warning} La prueba no mide identificación de daño físico en TAMPAR.

## Exportación a TensorFlow Lite

Se exportó `salidas/vixta_prueba_temprana.tflite` con cuantización de rango dinámico mediante `{result['conversion']}`. **Tamaño: {result['tamano_bytes']:,} bytes ({result['tamano_mib']:.2f} MiB). Latencia media: {result['latencia_ms']:.2f} ms** por inferencia, medida en 50 invocaciones tras 5 de calentamiento, con el intérprete TFLite local y un hilo. Esta latencia describe esta computadora; no mide un teléfono Android.

## Alcance y siguiente paso

Ésta es una línea base de **2 de las 4 clases** con fotos públicas de paquetería, no de cadena de frío. La exactitud en `test` mide esas fuentes y ese corte por paquete; no demuestra rendimiento en los empaques reales de Vixta ni en contaminación o etiquetas ilegibles. La actividad 26 incorporará las cuatro clases con el dataset propio. El archivo TFLite abre la actividad 28 de integración y medición en el dispositivo.
"""
    path.write_text(text, encoding="utf-8")


def run(root: Path, script_dir: Path, batch_size: int) -> dict:
    overall_start = time.perf_counter()
    tf.keras.utils.set_random_seed(42)
    by_split = read_index(root)
    counts = {split: len(rows) for split, rows in by_split.items()}
    train_labels = np.asarray([LABELS[row["clase"]] for row in by_split["train"]])
    weights = compute_class_weight(class_weight="balanced", classes=np.array([0, 1]), y=train_labels)
    class_weight = {0: float(weights[0]), 1: float(weights[1])}
    print(f"TensorFlow {tf.__version__}; imágenes {counts}; clases {dict(Counter(train_labels))}; pesos {class_weight}", flush=True)
    print(f"Dispositivos GPU: {tf.config.list_physical_devices('GPU')}", flush=True)
    train_data = dataset(by_split["train"], True, batch_size)
    val_data = dataset(by_split["val"], False, batch_size)
    test_data = dataset(by_split["test"], False, batch_size)
    model = build_model()
    print(f"Parámetros: {model.count_params():,}", flush=True)
    start = time.perf_counter()
    history = model.fit(
        train_data, validation_data=val_data, epochs=10,
        class_weight=class_weight,
        callbacks=[tf.keras.callbacks.EarlyStopping(
            monitor="val_loss", patience=3, restore_best_weights=True
        )], verbose=2,
    )
    train_seconds = time.perf_counter() - start

    probabilities = model.predict(test_data, verbose=0)
    true = np.asarray([LABELS[row["clase"]] for row in by_split["test"]])
    predicted = probabilities.argmax(axis=1)
    report = classification_report(
        true, predicted, labels=[0, 1], target_names=CLASSES,
        output_dict=True, zero_division=0,
    )
    matrix = confusion_matrix(true, predicted, labels=[0, 1]).tolist()
    source_metrics = {}
    for source in ("corrugated", "tampar"):
        indices = [i for i, row in enumerate(by_split["test"]) if row["fuente"] == source]
        intact_indices = [i for i in indices if true[i] == LABELS["integro"]]
        correct = sum(predicted[i] == true[i] for i in indices)
        intact_correct = sum(predicted[i] == 0 for i in intact_indices)
        source_metrics[source] = {
            "total": len(indices), "correctos": int(correct),
            "exactitud": float(correct / len(indices)),
            "integro_total": len(intact_indices),
            "integro_correctos": int(intact_correct),
            "integro_exactitud": float(intact_correct / len(intact_indices)),
        }

    output = script_dir / "salidas"
    output.mkdir(exist_ok=True)
    model.save(output / "vixta_prueba_temprana.keras")
    tflite_path, conversion = export_tflite(model, output)
    latency = benchmark_tflite(tflite_path, by_split["test"][0]["ruta"])
    save_matrix(np.asarray(matrix), script_dir / "matriz_confusion.png")
    result = {
        "fecha": datetime.now().isoformat(timespec="seconds"),
        "tensorflow": tf.__version__, "imagenes": counts,
        "pesos": {name: class_weight[index] for index, name in enumerate(CLASSES)},
        "epocas": len(history.history["loss"]),
        "segundos_entrenamiento": train_seconds,
        "segundos_totales": time.perf_counter() - overall_start,
        "metricas": report, "matriz": matrix,
        "exactitud": float(accuracy_score(true, predicted)),
        "correctos": int(np.sum(true == predicted)),
        "fuentes": source_metrics, "conversion": conversion,
        "tamano_bytes": tflite_path.stat().st_size,
        "tamano_mib": tflite_path.stat().st_size / 1024**2,
        "latencia_ms": latency,
    }
    (output / "metricas.json").write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    render_report(result, script_dir / "METRICAS_prueba_temprana.md")
    print("RESULTADOS_JSON=" + json.dumps(result, ensure_ascii=False), flush=True)
    return result


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--datos", type=Path, default=DEFAULT_DATA)
    parser.add_argument("--batch", type=int, default=32)
    args = parser.parse_args()
    run(args.datos, Path(__file__).resolve().parent, args.batch)
