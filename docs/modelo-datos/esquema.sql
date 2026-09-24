-- =====================================================================
-- VIXTA - Esquema de la base de datos remota
-- PostgreSQL / Supabase
-- Equipo 01 - Ingenieria de Dispositivos Moviles - Grupo 002
-- Cristopher Wenceslao Palacios Colunga - 1726646
-- 2 de septiembre de 2026
-- =====================================================================
-- Corresponde al modelo de datos de la actividad 7 del cronograma.
-- Adelanta la actividad 11, "Base de datos remota: creacion de tablas".
-- =====================================================================

-- ---------------------------------------------------------------------
-- LIMPIEZA PREVIA
-- Permite volver a ejecutar el script completo sin errores.
-- ATENCION: estas instrucciones ELIMINAN las tablas y todo su contenido.
-- Se usan durante el desarrollo del esquema, sobre una base sin datos
-- reales. No deben ejecutarse en una base en operacion.
-- El orden respeta las dependencias; cascade elimina tambien los
-- indices y las llaves foraneas asociadas.
-- ---------------------------------------------------------------------
drop table if exists alerta      cascade;
drop table if exists inspeccion  cascade;
drop table if exists paso_ronda  cascade;
drop table if exists ronda       cascade;
drop table if exists lote        cascade;
drop table if exists punto_frio  cascade;
drop table if exists usuario     cascade;

create extension if not exists "pgcrypto";   -- para gen_random_uuid()

-- ---------------------------------------------------------------------
-- 1. usuario
-- ---------------------------------------------------------------------
create table usuario (
    id          uuid primary key default gen_random_uuid(),
    nombre      text not null,
    correo      text not null unique,
    rol         text not null check (rol in ('operador','supervisor')),
    activo      boolean not null default true,
    creado_en   timestamptz not null default now()
);

-- ---------------------------------------------------------------------
-- 2. punto_frio
-- El campo codigo es el contenido del QR que lee la pantalla 3.
-- ---------------------------------------------------------------------
create table punto_frio (
    id          uuid primary key default gen_random_uuid(),
    codigo      text not null unique,
    nombre      text not null,
    tipo        text not null check (tipo in ('camara','tarima','anden')),
    ubicacion   text,
    activo      boolean not null default true
);

-- ---------------------------------------------------------------------
-- 3. lote
-- Habilita la trazabilidad por lote de la pantalla 7.
-- ---------------------------------------------------------------------
create table lote (
    id           uuid primary key default gen_random_uuid(),
    codigo       text not null unique,
    descripcion  text
);

-- ---------------------------------------------------------------------
-- 4. ronda
-- id_local lo genera el telefono antes de que exista conexion.
-- La restriccion unique sobre id_local es lo que impide que un
-- reintento de la cola de sincronizacion inserte el mismo registro
-- dos veces.
-- ---------------------------------------------------------------------
create table ronda (
    id            uuid primary key default gen_random_uuid(),
    id_local      uuid not null unique,
    punto_id      uuid not null references punto_frio(id),
    usuario_id    uuid not null references usuario(id),
    iniciada_en   timestamptz not null,
    cerrada_en    timestamptz,
    estado        text not null default 'en_curso'
                  check (estado in ('en_curso','completa','incompleta')),
    sincronizada  boolean not null default false
);

-- ---------------------------------------------------------------------
-- 5. paso_ronda
-- Los pasos del checklist se almacenan como datos. Modificar una ronda
-- no requiere recompilar la aplicacion.
-- ---------------------------------------------------------------------
create table paso_ronda (
    id            uuid primary key default gen_random_uuid(),
    ronda_id      uuid not null references ronda(id) on delete cascade,
    orden         integer not null,
    descripcion   text not null,
    completado    boolean not null default false,
    completado_en timestamptz,
    unique (ronda_id, orden)
);

-- ---------------------------------------------------------------------
-- 6. inspeccion
-- origen distingue si la clase la asigno el modelo o una persona.
-- Es la salida degradada declarada en la ficha tecnica: si el modelo
-- no alcanza la precision esperada, la aplicacion opera en manual.
-- precision_gps guarda el margen de error en metros. Una coordenada
-- sin su margen no acredita presencia en el punto.
-- ---------------------------------------------------------------------
create table inspeccion (
    id             uuid primary key default gen_random_uuid(),
    id_local       uuid not null unique,
    ronda_id       uuid not null references ronda(id) on delete cascade,
    lote_id        uuid references lote(id),
    foto_url       text,
    clase          text not null
                   check (clase in ('integro',
                                    'empaque_danado',
                                    'contaminacion',
                                    'etiqueta_ilegible')),
    confianza      numeric(4,3) check (confianza between 0 and 1),
    origen         text not null check (origen in ('modelo','manual')),
    capturada_en   timestamptz not null,
    lat            numeric(9,6),
    lon            numeric(9,6),
    precision_gps  numeric(6,2),
    sincronizada   boolean not null default false,
    -- Si la clase la asigno el modelo, debe existir su confianza.
    constraint confianza_requerida_si_modelo
        check (origen <> 'modelo' or confianza is not null)
);

-- ---------------------------------------------------------------------
-- 7. alerta
-- ---------------------------------------------------------------------
create table alerta (
    id             uuid primary key default gen_random_uuid(),
    inspeccion_id  uuid not null references inspeccion(id) on delete cascade,
    tipo           text not null,
    severidad      text not null check (severidad in ('baja','media','alta')),
    abierta_en     timestamptz not null default now(),
    atendida_por   uuid references usuario(id),
    atendida_en    timestamptz,
    nota_atencion  text,
    estado         text not null default 'abierta'
                   check (estado in ('abierta','atendida','cerrada')),
    -- Una alerta atendida debe registrar quien la atendio y cuando.
    constraint atencion_completa
        check (estado = 'abierta'
               or (atendida_por is not null and atendida_en is not null))
);

-- =====================================================================
-- Indices
-- Cada indice existe por una consulta concreta de la pantalla 7,
-- que filtra por lote, por camara y por rango de fechas.
-- =====================================================================
create index idx_inspeccion_lote    on inspeccion (lote_id);
create index idx_inspeccion_fecha   on inspeccion (capturada_en desc);
create index idx_inspeccion_ronda   on inspeccion (ronda_id);
create index idx_ronda_punto_fecha  on ronda (punto_id, iniciada_en desc);
create index idx_alerta_abiertas    on alerta (estado) where estado = 'abierta';

-- Cola de sincronizacion: son los registros con sincronizada = false.
-- El indice parcial mantiene la consulta barata aunque la tabla crezca.
create index idx_ronda_pendientes      on ronda (id)      where sincronizada = false;
create index idx_inspeccion_pendientes on inspeccion (id) where sincronizada = false;

-- =====================================================================
-- Seguridad a nivel de fila
-- El control de acceso se aplica en la base y no en la aplicacion.
-- Un operador solo consulta sus propios registros; un supervisor
-- consulta los de todo el equipo.
-- =====================================================================
alter table ronda      enable row level security;
alter table inspeccion enable row level security;
alter table alerta     enable row level security;

-- Las politicas concretas, el RLS de las otras cuatro tablas y la liga
-- de usuario con Supabase Auth estan en auth_roles.sql (actividad 12,
-- "Autenticacion y roles"). Se corre DESPUES de este archivo.
