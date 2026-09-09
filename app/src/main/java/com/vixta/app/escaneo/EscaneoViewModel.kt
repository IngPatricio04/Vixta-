package com.vixta.app.escaneo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vixta.app.datos.local.Ronda
import com.vixta.app.datos.local.VixtaDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface EscaneoState {
    object Idle : EscaneoState
    object Cargando : EscaneoState
    data class Exito(val rondaId: String) : EscaneoState
    data class Error(val mensaje: String) : EscaneoState
}

class EscaneoViewModel(
    private val vixtaDao: VixtaDao
) : ViewModel() {

    private val _estado = MutableStateFlow<EscaneoState>(EscaneoState.Idle)
    val estado: StateFlow<EscaneoState> = _estado.asStateFlow()

    fun procesarQrEscaneado(
        puntoFrioId: String,
        usuarioId: String,
        latitud: Double,
        longitud: Double,
        precisionGps: Float
    ) {
        viewModelScope.launch {
            _estado.value = EscaneoState.Cargando
            try {
                // Genera el ID único localmente
                val nuevaRondaId = UUID.randomUUID().toString()

                // Registra la ronda en la base de datos local (Room) con sellos de hardware
                val nuevaRonda = Ronda(
                    id = nuevaRondaId,
                    puntoFrioId = puntoFrioId,
                    usuarioId = usuarioId,
                    fechaHora = System.currentTimeMillis(), // Timestamp automático e inalterable
                    latitud = latitud,
                    longitud = longitud,
                    precisionGps = precisionGps,
                    completada = false
                )

                vixtaDao.crearRonda(nuevaRonda)
                _estado.value = EscaneoState.Exito(nuevaRondaId)
            } catch (e: Exception) {
                _estado.value = EscaneoState.Error("Error al iniciar ronda: ${e.message}")
            }
        }
    }
}