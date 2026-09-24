package com.vixta.app.datos.remoto

import com.vixta.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * La puerta de la app hacia Supabase: todo lo remoto pasa por aquí.
 *
 * - La URL y la llave pública salen de local.properties al compilar (supabase.url y supabase.key).
 *   Ese archivo no se sube a Git, y la llave nunca es la service_role: lo que protege los datos
 *   son las políticas RLS de la base (docs/modelo-datos/auth_roles.sql).
 * - Las pantallas no llaman a este objeto. Lo usan los repositorios: AuthRepositorio (actividad 12)
 *   y los de cada tabla (actividad 20). Contrato en 00_CONTEXTO/DATOS.md, decisión 4.
 */
object SupabaseCliente {

    private val TIPO_JSON = "application/json; charset=utf-8".toMediaType()

    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    /** true si el APK se compiló con la URL y la llave de Supabase. */
    val configurado: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_KEY.isNotBlank()

    class Respuesta(val codigo: Int, val cuerpo: String) {
        val exitosa: Boolean get() = codigo in 200..299
    }

    /** No hubo respuesta: sin red, sin DNS o el servidor no contestó a tiempo. */
    class SinConexion(causa: IOException) : IOException("No hay conexión con el servidor", causa)

    suspend fun get(ruta: String, token: String? = null): Respuesta =
        enviar(ruta, token, null) { get() }

    suspend fun post(ruta: String, json: String, token: String? = null, prefer: String? = null): Respuesta =
        enviar(ruta, token, prefer) { post(json.toRequestBody(TIPO_JSON)) }

    suspend fun patch(ruta: String, json: String, token: String? = null, prefer: String? = null): Respuesta =
        enviar(ruta, token, prefer) { patch(json.toRequestBody(TIPO_JSON)) }

    /** Para subir archivos, como las fotos de evidencia (actividad 21). */
    suspend fun postArchivo(ruta: String, cuerpo: RequestBody, token: String? = null, prefer: String? = null): Respuesta =
        enviar(ruta, token, prefer) { post(cuerpo) }

    private suspend fun enviar(
        ruta: String,
        token: String?,
        prefer: String?,
        metodo: Request.Builder.() -> Request.Builder,
    ): Respuesta = withContext(Dispatchers.IO) {
        check(configurado) { "Falta configurar supabase.url y supabase.key en local.properties" }
        val peticion = Request.Builder()
            .url(BuildConfig.SUPABASE_URL.trimEnd('/') + ruta)
            .header("apikey", BuildConfig.SUPABASE_KEY)
            .header("Accept", "application/json")
            .apply { if (token != null) header("Authorization", "Bearer $token") }
            .apply { if (prefer != null) header("Prefer", prefer) }
            .metodo()
            .build()
        try {
            http.newCall(peticion).execute().use { r -> Respuesta(r.code, r.body?.string().orEmpty()) }
        } catch (e: IOException) {
            throw SinConexion(e)
        }
    }
}
