-- =====================================================================
-- VIXTA - Actividad 12: Autenticacion y roles
-- PostgreSQL / Supabase
-- Cristopher Wenceslao Palacios Colunga - 1726646 - 22 de septiembre de 2026
-- =====================================================================
-- Liga la tabla usuario con Supabase Auth y activa la seguridad a nivel
-- de fila en las 7 tablas. Las reglas por tabla salen de la matriz de
-- Anahi Limon (docs/manuales/MATRIZ_RLS.md) y se extienden a las cuatro
-- tablas que la matriz no cubria.
--
-- COMO SE CORRE
--   1. En Authentication > Users > Add user, crear las dos cuentas de
--      prueba con "Auto Confirm User" marcado.
--   2. Si se usaron otros correos, cambiarlos en la seccion 0.
--   3. Pegar este archivo completo en el SQL Editor y ejecutar.
--
-- Se puede correr mas de una vez. Todo va en una sola transaccion: si
-- algo falla, no se aplica nada y el mensaje dice que falto.
-- Correr DESPUES de esquema.sql (si esquema.sql se vuelve a correr,
-- borra las tablas, y este archivo se tiene que correr otra vez).
-- =====================================================================

begin;

-- ---------------------------------------------------------------------
-- 0. Las dos cuentas de prueba. EDITAR AQUI si se usaron otros correos.
-- ---------------------------------------------------------------------
create temporary table cuentas_prueba (correo text, nombre text, rol text) on commit drop;
insert into cuentas_prueba values
    ('operador@vixta.test',   'Operador de prueba',   'operador'),
    ('supervisor@vixta.test', 'Supervisor de prueba', 'supervisor');

-- Freno 1: las dos cuentas tienen que existir en Auth.
do $$
declare
    faltan text;
begin
    select string_agg(c.correo, ', ') into faltan
    from cuentas_prueba c
    where not exists (select 1 from auth.users a where lower(a.email) = lower(c.correo));
    if faltan is not null then
        raise exception 'Faltan estas cuentas en Authentication > Users: %', faltan;
    end if;
end $$;

-- Freno 2: ninguna fila de usuario puede quedar sin su cuenta de Auth.
do $$
declare
    sin_cuenta text;
begin
    select string_agg(u.correo, ', ') into sin_cuenta
    from public.usuario u
    where not exists (select 1 from auth.users a where a.id = u.id);
    if sin_cuenta is not null then
        raise exception 'Estas filas de usuario no tienen cuenta en Auth: %. Hay que crearles cuenta o quitarlas antes de ligar.', sin_cuenta;
    end if;
end $$;

-- ---------------------------------------------------------------------
-- 1. usuario.id ES el id de la cuenta de Supabase Auth
-- El id ya no se genera solo: lo pone la cuenta. Sin cascada, porque un
-- usuario se desactiva, no se borra (su historial lo referencia).
-- ---------------------------------------------------------------------
alter table public.usuario alter column id drop default;
alter table public.usuario drop constraint if exists usuario_id_es_cuenta_auth;
alter table public.usuario add constraint usuario_id_es_cuenta_auth
    foreign key (id) references auth.users (id);

-- ---------------------------------------------------------------------
-- 2. El perfil de las dos cuentas de prueba
-- ---------------------------------------------------------------------
insert into public.usuario (id, nombre, correo, rol)
select a.id, c.nombre, lower(c.correo), c.rol
from cuentas_prueba c
join auth.users a on lower(a.email) = lower(c.correo)
on conflict (id) do update
    set nombre = excluded.nombre,
        rol    = excluded.rol,
        activo = true;

-- ---------------------------------------------------------------------
-- 3. El rol de quien tiene la sesion
-- security definer: la funcion lee usuario sin pasar por sus politicas,
-- asi las politicas de usuario pueden preguntar el rol sin ciclarse.
-- search_path vacio: nadie puede colarle otra tabla con el mismo nombre.
-- ---------------------------------------------------------------------
create or replace function public.rol_actual()
returns text
language sql
stable
security definer
set search_path = ''
as $$
    select u.rol
    from public.usuario u
    where u.id = auth.uid() and u.activo
$$;

create or replace function public.es_supervisor()
returns boolean
language sql
stable
set search_path = ''
as $$
    select coalesce(public.rol_actual() = 'supervisor', false)
$$;

revoke all on function public.rol_actual()    from public, anon;
revoke all on function public.es_supervisor() from public, anon;
grant execute on function public.rol_actual()    to authenticated;
grant execute on function public.es_supervisor() to authenticated;

-- ---------------------------------------------------------------------
-- 4. Seguridad a nivel de fila en las 7 tablas
-- Sin politica no hay acceso: la llave publica sola no lee ni escribe
-- nada. Todo pasa por la sesion del usuario.
-- ---------------------------------------------------------------------
alter table public.usuario    enable row level security;
alter table public.punto_frio enable row level security;
alter table public.lote       enable row level security;
alter table public.ronda      enable row level security;
alter table public.paso_ronda enable row level security;
alter table public.inspeccion enable row level security;
alter table public.alerta     enable row level security;

-- usuario: cada quien ve su perfil; el supervisor ve al equipo.
-- Las altas y bajas se hacen desde el panel, no desde la app.
drop policy if exists usuario_ver on public.usuario;
create policy usuario_ver on public.usuario
    for select to authenticated
    using (id = (select auth.uid()) or (select public.es_supervisor()));

-- punto_frio y lote: catalogos. Todos los leen; solo el supervisor da
-- de alta o cambia. Nadie borra: se dan de baja con activo = false.
drop policy if exists punto_frio_ver    on public.punto_frio;
drop policy if exists punto_frio_alta   on public.punto_frio;
drop policy if exists punto_frio_cambio on public.punto_frio;
create policy punto_frio_ver on public.punto_frio
    for select to authenticated using (true);
create policy punto_frio_alta on public.punto_frio
    for insert to authenticated with check ((select public.es_supervisor()));
create policy punto_frio_cambio on public.punto_frio
    for update to authenticated
    using ((select public.es_supervisor())) with check ((select public.es_supervisor()));

drop policy if exists lote_ver    on public.lote;
drop policy if exists lote_alta   on public.lote;
drop policy if exists lote_cambio on public.lote;
create policy lote_ver on public.lote
    for select to authenticated using (true);
create policy lote_alta on public.lote
    for insert to authenticated with check ((select public.es_supervisor()));
create policy lote_cambio on public.lote
    for update to authenticated
    using ((select public.es_supervisor())) with check ((select public.es_supervisor()));

-- ronda: el operador crea y ve las suyas, y solo cambia las abiertas.
-- El supervisor ve todas.
drop policy if exists ronda_ver        on public.ronda;
drop policy if exists ronda_crear      on public.ronda;
drop policy if exists ronda_actualizar on public.ronda;
create policy ronda_ver on public.ronda
    for select to authenticated
    using (usuario_id = (select auth.uid()) or (select public.es_supervisor()));
create policy ronda_crear on public.ronda
    for insert to authenticated
    with check (usuario_id = (select auth.uid()));
create policy ronda_actualizar on public.ronda
    for update to authenticated
    using (usuario_id = (select auth.uid()) and estado = 'en_curso')
    with check (usuario_id = (select auth.uid()));

-- paso_ronda: sigue a su ronda.
drop policy if exists paso_ronda_ver    on public.paso_ronda;
drop policy if exists paso_ronda_crear  on public.paso_ronda;
drop policy if exists paso_ronda_marcar on public.paso_ronda;
create policy paso_ronda_ver on public.paso_ronda
    for select to authenticated
    using (exists (
        select 1 from public.ronda r
        where r.id = paso_ronda.ronda_id
          and (r.usuario_id = (select auth.uid()) or (select public.es_supervisor()))));
create policy paso_ronda_crear on public.paso_ronda
    for insert to authenticated
    with check (exists (
        select 1 from public.ronda r
        where r.id = paso_ronda.ronda_id and r.usuario_id = (select auth.uid())));
create policy paso_ronda_marcar on public.paso_ronda
    for update to authenticated
    using (exists (
        select 1 from public.ronda r
        where r.id = paso_ronda.ronda_id and r.usuario_id = (select auth.uid()) and r.estado = 'en_curso'))
    with check (exists (
        select 1 from public.ronda r
        where r.id = paso_ronda.ronda_id and r.usuario_id = (select auth.uid())));

-- inspeccion: la evidencia. Se crea dentro de una ronda propia y no se
-- reescribe: no hay politica de update ni de delete.
drop policy if exists inspeccion_ver   on public.inspeccion;
drop policy if exists inspeccion_crear on public.inspeccion;
create policy inspeccion_ver on public.inspeccion
    for select to authenticated
    using (exists (
        select 1 from public.ronda r
        where r.id = inspeccion.ronda_id
          and (r.usuario_id = (select auth.uid()) or (select public.es_supervisor()))));
create policy inspeccion_crear on public.inspeccion
    for insert to authenticated
    with check (exists (
        select 1 from public.ronda r
        where r.id = inspeccion.ronda_id and r.usuario_id = (select auth.uid())));

-- alerta: el operador ve y levanta las de sus inspecciones; el supervisor
-- ve todas y es el unico que las atiende, a su nombre.
drop policy if exists alerta_ver     on public.alerta;
drop policy if exists alerta_crear   on public.alerta;
drop policy if exists alerta_atender on public.alerta;
create policy alerta_ver on public.alerta
    for select to authenticated
    using ((select public.es_supervisor()) or exists (
        select 1 from public.inspeccion i
        join public.ronda r on r.id = i.ronda_id
        where i.id = alerta.inspeccion_id and r.usuario_id = (select auth.uid())));
create policy alerta_crear on public.alerta
    for insert to authenticated
    with check (exists (
        select 1 from public.inspeccion i
        join public.ronda r on r.id = i.ronda_id
        where i.id = alerta.inspeccion_id and r.usuario_id = (select auth.uid())));
create policy alerta_atender on public.alerta
    for update to authenticated
    using ((select public.es_supervisor()))
    with check ((select public.es_supervisor())
                and (estado = 'abierta' or atendida_por = (select auth.uid())));

commit;

-- =====================================================================
-- Resultado: esto es lo que se revisa. Deben salir las 7 tablas con
-- RLS activo, las dos cuentas con su rol y la liga con Auth.
-- =====================================================================
select 'tabla' as que,
       c.relname as nombre,
       case when c.relrowsecurity then 'RLS activo' else 'SIN RLS' end
       || ' - ' || (select count(*) from pg_policies p
                    where p.schemaname = 'public' and p.tablename = c.relname) || ' politicas' as detalle
from pg_class c
where c.relnamespace = 'public'::regnamespace
  and c.relkind = 'r'
  and c.relname in ('usuario', 'punto_frio', 'lote', 'ronda', 'paso_ronda', 'inspeccion', 'alerta')
union all
select 'usuario', u.correo, u.rol || case when u.activo then '' else ' (inactivo)' end
from public.usuario u
union all
select 'liga', conname, pg_get_constraintdef(oid)
from pg_constraint
where conname = 'usuario_id_es_cuenta_auth'
order by 1, 2;
