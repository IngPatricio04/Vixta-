package com.vixta.app.datos.local


import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "preferencias_usuario")

object PreferenciasUsuario {
    private val ULTIMO_USUARIO = stringPreferencesKey("ultimo_usuario")

    fun obtenerUltimoUsuario(context: Context): Flow<String> =
        context.dataStore.data.map { it[ULTIMO_USUARIO] ?: "" }

    suspend fun guardarUltimoUsuario(context: Context, usuario: String) {
        context.dataStore.edit { it[ULTIMO_USUARIO] = usuario }
    }
}