package com.vixta.app.datos.remoto

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/** Quién tiene la sesión. El rol viene de usuario.rol en la base, nunca de la pantalla. */
data class SesionUsuario(
    val usuarioId: String,
    val nombre: String,
    val correo: String,
    val rol: String,
    val tokenAcceso: String,
    val tokenRenovacion: String,
    /** Cuándo vence el token de acceso, en segundos desde 1970. */
    val expiraEn: Long,
) {
    val esSupervisor: Boolean get() = rol == "supervisor"
    val rolLegible: String get() = if (esSupervisor) "Supervisor" else "Operador"
}

/** Un error que se le puede mostrar tal cual a quien usa la app. */
class ErrorDeSesion(mensaje: String) : Exception(mensaje)

private val Context.almacenSesion by preferencesDataStore(name = "sesion_vixta")

/**
 * Actividad 12 · Autenticación y roles.
 *
 * 1. Supabase Auth valida el correo y la contraseña, y entrega los tokens.
 * 2. Con ese token se lee el perfil en la tabla usuario. La política RLS sólo le entrega a cada
 *    quien su propia fila, así que el rol no se puede falsear desde el teléfono.
 * 3. La sesión se guarda en el teléfono: si ya entraste una vez, la app abre sin red.
 */
object AuthRepositorio {

    private object Claves {
        val ID = stringPreferencesKey("usuario_id")
        val NOMBRE = stringPreferencesKey("nombre")
        val CORREO = stringPreferencesKey("correo")
        val ROL = stringPreferencesKey("rol")
        val TOKEN = stringPreferencesKey("token_acceso")
        val RENOVACION = stringPreferencesKey("token_renovacion")
        val EXPIRA = longPreferencesKey("expira_en")
    }

    private val _sesion = MutableStateFlow<SesionUsuario?>(null)

    /** La sesión actual. Cualquier pantalla la puede leer sin cambiar su firma. */
    val sesion: StateFlow<SesionUsuario?> = _sesion.asStateFlow()

    /** Al abrir la app: recupera la sesión guardada, si la hay. No usa la red. */
    suspend fun cargarSesionGuardada(context: Context): SesionUsuario? {
        val p = context.almacenSesion.data.first()
        val guardada = p[Claves.ID]?.let { id ->
            SesionUsuario(
                usuarioId = id,
                nombre = p[Claves.NOMBRE].orEmpty(),
                correo = p[Claves.CORREO].orEmpty(),
                rol = p[Claves.ROL].orEmpty(),
                tokenAcceso = p[Claves.TOKEN].orEmpty(),
                tokenRenovacion = p[Claves.RENOVACION].orEmpty(),
                expiraEn = p[Claves.EXPIRA] ?: 0L,
            )
        }
        _sesion.value = guardada
        return guardada
    }

    suspend fun iniciarSesion(context: Context, correo: String, contrasena: String): Result<SesionUsuario> =
        try {
            if (!SupabaseCliente.configurado) {
                throw ErrorDeSesion("La app no tiene configurado el servidor. Revisa local.properties")
            }
            val correoLimpio = correo.trim().lowercase()

            // 1 · Supabase Auth valida la cuenta
            val credenciales = JSONObject().put("email", correoLimpio).put("password", contrasena).toString()
            val auth = SupabaseCliente.post("/auth/v1/token?grant_type=password", credenciales)
            if (!auth.exitosa) throw ErrorDeSesion(mensajeDeAuth(auth))
            val tokens = JSONObject(auth.cuerpo)
            val usuarioId = tokens.getJSONObject("user").getString("id")
            val tokenAcceso = tokens.getString("access_token")

            // 2 · El perfil y el rol, desde la tabla usuario (RLS: sólo su propia fila)
            val perfil = SupabaseCliente.get(
                "/rest/v1/usuario?id=eq.$usuarioId&select=nombre,correo,rol,activo", tokenAcceso
            )
            if (!perfil.exitosa) throw ErrorDeSesion("No se pudo leer tu perfil (código ${perfil.codigo})")
            val filas = JSONArray(perfil.cuerpo)
            if (filas.length() == 0) {
                throw ErrorDeSesion("Tu cuenta no tiene perfil en Vixta. Pide al supervisor que te dé de alta")
            }
            val fila = filas.getJSONObject(0)
            if (!fila.optBoolean("activo", true)) throw ErrorDeSesion("Tu cuenta está desactivada")

            val sesion = SesionUsuario(
                usuarioId = usuarioId,
                nombre = fila.getString("nombre"),
                correo = fila.getString("correo"),
                rol = fila.getString("rol"),
                tokenAcceso = tokenAcceso,
                tokenRenovacion = tokens.getString("refresh_token"),
                expiraEn = vencimiento(tokens),
            )

            // 3 · Se guarda en el teléfono
            guardar(context, sesion)
            _sesion.value = sesion
            Result.success(sesion)
        } catch (e: SupabaseCliente.SinConexion) {
            Result.failure(ErrorDeSesion("Sin conexión. La primera vez necesitas red para entrar; después la app abre sin señal"))
        } catch (e: ErrorDeSesion) {
            Result.failure(e)
        } catch (e: JSONException) {
            Result.failure(ErrorDeSesion("El servidor respondió algo inesperado"))
        }

    /**
     * Un token vigente para llamar a la base. Lo usan los repositorios de cada tabla (actividad 20).
     * Si ya venció, lo renueva. Sin red devuelve null: la cola de sincronización espera y reintenta.
     */
    suspend fun tokenVigente(context: Context): String? {
        val actual = _sesion.value ?: cargarSesionGuardada(context) ?: return null
        if (actual.expiraEn - ahora() > 60) return actual.tokenAcceso
        return try {
            val cuerpo = JSONObject().put("refresh_token", actual.tokenRenovacion).toString()
            val r = SupabaseCliente.post("/auth/v1/token?grant_type=refresh_token", cuerpo)
            if (!r.exitosa) return null
            val t = JSONObject(r.cuerpo)
            val renovada = actual.copy(
                tokenAcceso = t.getString("access_token"),
                tokenRenovacion = t.getString("refresh_token"),
                expiraEn = vencimiento(t),
            )
            guardar(context, renovada)
            _sesion.value = renovada
            renovada.tokenAcceso
        } catch (e: SupabaseCliente.SinConexion) {
            null
        } catch (e: JSONException) {
            null
        }
    }

    /** Cierra la sesión en el teléfono. Avisarle al servidor es cortesía: sin red, igual se cierra. */
    suspend fun cerrarSesion(context: Context) {
        val actual = _sesion.value
        if (actual != null && SupabaseCliente.configurado) {
            try {
                SupabaseCliente.post("/auth/v1/logout", "{}", actual.tokenAcceso)
            } catch (e: SupabaseCliente.SinConexion) {
                // Sin red: el token vence solo. Lo que importa es borrarlo del teléfono.
            }
        }
        context.almacenSesion.edit { it.clear() }
        _sesion.value = null
    }

    private suspend fun guardar(context: Context, s: SesionUsuario) {
        context.almacenSesion.edit {
            it[Claves.ID] = s.usuarioId
            it[Claves.NOMBRE] = s.nombre
            it[Claves.CORREO] = s.correo
            it[Claves.ROL] = s.rol
            it[Claves.TOKEN] = s.tokenAcceso
            it[Claves.RENOVACION] = s.tokenRenovacion
            it[Claves.EXPIRA] = s.expiraEn
        }
    }

    private fun vencimiento(tokens: JSONObject): Long =
        tokens.optLong("expires_at", ahora() + tokens.optLong("expires_in", 3600))

    private fun ahora(): Long = System.currentTimeMillis() / 1000

    private fun mensajeDeAuth(r: SupabaseCliente.Respuesta): String {
        val codigo = try {
            JSONObject(r.cuerpo).optString("error_code")
        } catch (e: JSONException) {
            ""
        }
        return when {
            codigo == "email_not_confirmed" -> "Tu cuenta no está confirmada"
            codigo == "invalid_credentials" || r.codigo == 400 -> "Correo o contraseña incorrectos"
            r.codigo == 429 -> "Demasiados intentos. Espera un minuto"
            else -> "El servidor no respondió bien (código ${r.codigo})"
        }
    }
}
