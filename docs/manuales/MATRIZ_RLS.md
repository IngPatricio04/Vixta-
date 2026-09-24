# Matriz de seguridad RLS — Vixta

Este documento define las reglas de acceso que se aplicarán en Supabase mediante **Row Level Security (RLS)**. No ejecuta políticas todavía.

## Objetivo

La aplicación no decide por sí sola qué puede leer cada persona. La base de datos verifica el usuario autenticado y su rol antes de devolver o aceptar datos.

## Roles

| Rol | Alcance |
|---|---|
| `operador` | Registra y consulta únicamente sus propias rondas y evidencias. |
| `supervisor` | Consulta la información del equipo y atiende alertas. |

## Reglas por tabla protegida

El esquema ya habilitó RLS en `ronda`, `inspeccion` y `alerta`.

| Tabla | Operador | Supervisor |
|---|---|---|
| `ronda` | Crear y consultar sólo las rondas donde `usuario_id` sea su usuario. Puede actualizar sólo sus rondas abiertas. | Consultar todas las rondas. |
| `inspeccion` | Crear y consultar evidencia ligada a una ronda propia. No puede cambiar evidencia ajena. | Consultar todas las evidencias para historial y trazabilidad. |
| `alerta` | Consultar alertas originadas en sus propias inspecciones. | Consultar todas las alertas y registrar su atención. |

## Cómo identifica la base a la persona

Las futuras políticas usarán `auth.uid()`, que devuelve el UUID de la cuenta que inició sesión en Supabase Auth.

Para que esto funcione, el UUID de Auth debe coincidir con `public.usuario.id`, o debe existir una relación explícita y documentada entre ambos. Este punto sigue pendiente de confirmación con el equipo.

## Verificaciones planeadas

1. Iniciar sesión como operador y crear una ronda propia.
2. Confirmar que el operador puede verla, pero no ve una ronda de otro operador.
3. Iniciar sesión como supervisor y confirmar que sí ve ambas rondas.
4. Confirmar que un supervisor puede atender una alerta y que se guardan `atendida_por` y `atendida_en`.

## Límites de esta primera versión

- No se ejecutará ninguna política hasta contar con un operador y un supervisor de prueba en Supabase Auth.
- No se utilizará la llave `service_role` dentro de la aplicación Android.
- La aplicación deberá usar la sesión del usuario; así RLS recibe el UUID correcto.
