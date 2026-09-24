"""Prepara el dataset público de Vixta, separando siempre por paquete."""

from __future__ import annotations

import argparse
import csv
import hashlib
import json
import math
import random
import shutil
from collections import Counter, defaultdict
from pathlib import Path

from PIL import Image, UnidentifiedImageError


CLASSES = ("integro", "empaque_danado")
SPLITS = ("train", "val", "test")
SOURCES = ("corrugated", "tampar")
DEFAULT_ROOT = Path(
    r"C:\Users\zenin\OneDrive\Documentos\PROYPER\UNI\DISMOV_CO\PIA\Vixta\04_CRUDO\dataset_publico"
)


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def box_bounds(shape: dict) -> tuple[float, float, float, float] | None:
    try:
        points = shape["points"]
        xs = [float(point[0]) for point in points]
        ys = [float(point[1]) for point in points]
        if len(xs) < 2 or not all(math.isfinite(v) for v in xs + ys):
            return None
        bounds = min(xs), min(ys), max(xs), max(ys)
        return bounds if bounds[2] > bounds[0] and bounds[3] > bounds[1] else None
    except (KeyError, TypeError, ValueError, IndexError):
        return None


def inspect_corrugated(root: Path, dropped: Counter) -> list[dict]:
    candidates = []
    for image_path in sorted((root / "corrugated" / "dataset3_resize").glob("*.jpg")):
        annotation_path = image_path.with_suffix(".json")
        if not annotation_path.is_file():
            dropped["corrugated: sin_anotacion"] += 1
            continue
        try:
            annotation = json.loads(annotation_path.read_text(encoding="utf-8"))
            shapes = annotation["shapes"]
            boxes = [bounds for item in shapes if item.get("label") == "box"
                     if (bounds := box_bounds(item)) is not None]
        except (OSError, ValueError, TypeError, KeyError, AttributeError):
            dropped["corrugated: anotacion_invalida"] += 1
            continue
        if not boxes:
            dropped["corrugated: sin_box"] += 1
            continue
        bounds = max(boxes, key=lambda b: (b[2] - b[0]) * (b[3] - b[1]))
        try:
            with Image.open(image_path) as image:
                image.load()
                width, height = image.size
        except (OSError, ValueError, UnidentifiedImageError):
            dropped["corrugated: imagen_ilegible"] += 1
            continue
        left, top, right, bottom = bounds
        if left < 8 or top < 8 or width - right < 8 or height - bottom < 8:
            dropped["corrugated: box_en_borde"] += 1
            continue
        if right > width or bottom > height:
            dropped["corrugated: box_fuera_de_imagen"] += 1
            continue
        margin_x, margin_y = (right - left) * .03, (bottom - top) * .03
        crop = (
            max(0, math.floor(left - margin_x)),
            max(0, math.floor(top - margin_y)),
            min(width, math.ceil(right + margin_x)),
            min(height, math.ceil(bottom + margin_y)),
        )
        labels = {item.get("label") for item in shapes if isinstance(item, dict)}
        candidates.append({
            "source": "corrugated", "path": image_path,
            "package": image_path.stem.split("_")[0],
            "class": "empaque_danado" if "damage" in labels else "integro",
            "crop": crop,
        })
    return candidates


def inspect_tampar(root: Path, dropped: Counter) -> list[dict]:
    candidates = []
    index_path = root / "tampar" / "recortes_indice.csv"
    with index_path.open(encoding="utf-8-sig", newline="") as stream:
        rows = list(csv.DictReader(stream))
    for row in rows:
        if not row["estado"].lower().startswith("ok"):
            dropped[f"tampar: {row['estado']}"] += 1
            continue
        image_path = root / "tampar" / "recortes" / row["recorte"]
        if not image_path.is_file():
            dropped["tampar: archivo_ausente"] += 1
            continue
        try:
            with Image.open(image_path) as image:
                image.load()
        except (OSError, ValueError, UnidentifiedImageError):
            dropped["tampar: imagen_ilegible"] += 1
            continue
        candidates.append({
            "source": "tampar", "path": image_path,
            "package": row["paquete"], "class": "integro", "crop": None,
        })
    return candidates


def assign_splits(candidates: list[dict]) -> dict[tuple[str, str], str]:
    assignment = {}
    for source in SOURCES:
        packages = sorted({item["package"] for item in candidates if item["source"] == source})
        random.Random(42).shuffle(packages)
        total = len(packages)
        quotas = [total * .70, total * .15, total * .15]
        counts = [math.floor(value) for value in quotas]
        remainder = total - sum(counts)
        for index in sorted(range(3), key=lambda i: (-(quotas[i] - counts[i]), i))[:remainder]:
            counts[index] += 1
        cursor = 0
        for split, count in zip(SPLITS, counts):
            for package in packages[cursor:cursor + count]:
                assignment[(source, package)] = split
            cursor += count
        assert cursor == total
    assert len(assignment) == len({(item["source"], item["package"]) for item in candidates})
    return assignment


def prepare(root: Path, output: Path) -> dict:
    dropped = Counter()
    candidates = inspect_corrugated(root, dropped) + inspect_tampar(root, dropped)
    unique = []
    hashes = set()
    for item in candidates:
        digest = sha256(item["path"])
        if digest in hashes:
            dropped[f"{item['source']}: duplicado_exacto"] += 1
            continue
        hashes.add(digest)
        unique.append(item)
    assignment = assign_splits(unique)
    package_splits = defaultdict(set)
    for item in unique:
        package_splits[(item["source"], item["package"])].add(
            assignment[(item["source"], item["package"])]
        )
    assert all(len(splits) == 1 for splits in package_splits.values()), "Paquete en varias particiones"

    output.mkdir(parents=True, exist_ok=True)
    records = []
    for item in unique:
        split = assignment[(item["source"], item["package"])]
        relative = Path(split) / item["class"] / f"{item['source']}__{item['path'].name}"
        destination = output / relative
        destination.parent.mkdir(parents=True, exist_ok=True)
        if item["source"] == "corrugated":
            with Image.open(item["path"]) as image:
                crop = image.convert("RGB").crop(item["crop"])
                crop.thumbnail((640, 640), Image.Resampling.LANCZOS)
                crop.save(destination, format="JPEG", quality=92, subsampling=0)
        else:
            shutil.copyfile(item["path"], destination)
        records.append({
            "archivo": relative.as_posix(), "clase": item["class"],
            "particion": split, "fuente": item["source"],
            "paquete": item["package"], "origen": str(item["path"]),
        })
    records.sort(key=lambda row: row["archivo"])
    with (output / "indice.csv").open("w", encoding="utf-8", newline="") as stream:
        writer = csv.DictWriter(stream, fieldnames=("archivo", "clase", "particion", "fuente", "paquete", "origen"))
        writer.writeheader()
        writer.writerows(records)

    counts = Counter((row["clase"], row["particion"], row["fuente"]) for row in records)
    packages = Counter((split, source) for (source, _), split in assignment.items())
    summary = {
        "semilla": 42, "total": len(records),
        "conteo": [{"clase": cls, "particion": split, "fuente": source,
                    "imagenes": counts[(cls, split, source)]}
                   for cls in CLASSES for split in SPLITS for source in SOURCES],
        "paquetes": [{"particion": split, "fuente": source,
                      "cantidad": packages[(split, source)]}
                     for split in SPLITS for source in SOURCES],
        "descartes": dict(sorted(dropped.items())),
    }
    (output / "resumen.json").write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(summary, ensure_ascii=False, indent=2))
    return summary


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--raiz", type=Path, default=DEFAULT_ROOT)
    parser.add_argument("--salida", type=Path)
    args = parser.parse_args()
    prepare(args.raiz, args.salida or args.raiz / "preparado")
