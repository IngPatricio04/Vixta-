package com.vixta.app.configuracion

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vixta.app.datos.local.RepositorioRondas
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn

/**
 * Los números de verdad para la pantalla de Patricio (ConfiguracionUiState), como él la dejó preparada:
 * la cola sale de la base local y «Conectado / Sin señal» del estado real de la red.
 */
class ConfiguracionViewModel(
    repositorio: RepositorioRondas,
    contexto: Context
) : ViewModel() {

    private val aviso = MutableStateFlow<String?>(null)

    val estado: StateFlow<ConfiguracionUiState> =
        combine(repositorio.observarResumenCola(), observarConexion(contexto.applicationContext), aviso) { cola, enLinea, texto ->
            ConfiguracionUiState(
                elementosPendientes = cola.pendientes,
                fotosEvidencia = cola.fotosEvidencia,
                rondasCompletas = cola.rondasCompletas,
                conflictosPorResolver = 0, // todavía no hay nada que pueda chocar: la subida es la actividad 20
                hayConexion = enLinea,
                errorSincronizacion = texto
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConfiguracionUiState())

    /** La subida al servidor es la actividad 20 (Anahi). Mientras, el botón dice la verdad y nada se pierde. */
    fun sincronizarAhora() {
        val pendientes = estado.value.elementosPendientes
        aviso.value = if (pendientes == 0) {
            "No hay nada pendiente de subir"
        } else {
            "$pendientes pendientes guardados en el teléfono. La subida al servidor llega con la actividad 20"
        }
    }
}

/** true mientras haya una red con internet verificado. Se actualiza solo al entrar o salir del modo avión. */
private fun observarConexion(contexto: Context): Flow<Boolean> = callbackFlow {
    val cm = contexto.getSystemService(ConnectivityManager::class.java)

    fun hayRed(): Boolean {
        val capacidades = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    val aviso = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            trySend(hayRed())
        }

        override fun onCapabilitiesChanged(network: Network, capacidades: NetworkCapabilities) {
            trySend(hayRed())
        }

        override fun onLost(network: Network) {
            trySend(false)
        }
    }
    trySend(hayRed())
    cm.registerDefaultNetworkCallback(aviso)
    awaitClose { cm.unregisterNetworkCallback(aviso) }
}.distinctUntilChanged()
