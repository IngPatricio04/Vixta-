# Consultas de trazabilidad — Vixta

Este documento reúne las consultas que necesitará la pantalla **Historial y trazabilidad**. Son consultas de lectura: no crean, editan ni borran datos.

## Qué datos devuelve el historial

Cada resultado une una `inspeccion` con el `punto_frio` de su ronda y, cuando exista, con el `lote` asociado.

## 1. Historial por lote

Reemplazar `LOTE-001` por el código buscado.

```sql
select
    i.id,
    i.capturada_en,
    l.codigo as lote_codigo,
    p.codigo as punto_codigo,
    p.nombre as punto_nombre,
    i.clase,
    i.confianza,
    i.origen,
    i.foto_url,
    i.lat,
    i.lon,
    i.precision_gps
from inspeccion i
join ronda r on r.id = i.ronda_id
join punto_frio p on p.id = r.punto_id
join lote l on l.id = i.lote_id
where l.codigo = 'LOTE-001'
order by i.capturada_en desc;
```

Responde: **“¿Qué evidencia existe de este lote?”**.

## 2. Historial por punto frío

Reemplazar `CAM-01` por el código contenido en el QR del punto.

```sql
select
    i.id,
    i.capturada_en,
    p.codigo as punto_codigo,
    p.nombre as punto_nombre,
    l.codigo as lote_codigo,
    i.clase,
    i.origen,
    i.foto_url
from inspeccion i
join ronda r on r.id = i.ronda_id
join punto_frio p on p.id = r.punto_id
left join lote l on l.id = i.lote_id
where p.codigo = 'CAM-01'
order by i.capturada_en desc;
```

Responde: **“¿Qué se inspeccionó en esta cámara, tarima o andén?”**.

## 3. Historial por rango de fechas

Reemplazar las fechas por el rango requerido. El límite final usa el día siguiente para incluir todo el último día.

```sql
select
    i.id,
    i.capturada_en,
    p.codigo as punto_codigo,
    p.nombre as punto_nombre,
    l.codigo as lote_codigo,
    i.clase,
    i.origen,
    i.foto_url
from inspeccion i
join ronda r on r.id = i.ronda_id
join punto_frio p on p.id = r.punto_id
left join lote l on l.id = i.lote_id
where i.capturada_en >= '2026-09-01T00:00:00Z'
  and i.capturada_en <  '2026-10-01T00:00:00Z'
order by i.capturada_en desc;
```

Responde: **“¿Qué evidencias se capturaron durante este periodo?”**.

## Por qué estas consultas son viables

El esquema ya incluye `idx_inspeccion_lote`, `idx_inspeccion_fecha`, `idx_inspeccion_ronda` e `idx_ronda_punto_fecha`, creados para estas búsquedas.

## Pendiente antes de conectarlas a la app

Las consultas deberán respetar las políticas RLS. Aún falta acordar cómo `auth.uid()` se enlaza con `usuario.id`.
