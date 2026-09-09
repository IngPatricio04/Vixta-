package com.vixta.app.inspeccion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vixta.app.datos.local.PuntoRonda
import com.vixta.app.datos.local.VixtaDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InspeccionViewModel(
    private val vixtaDao: VixtaDao
) : ViewModel() {

    private val _pasos = MutableStateFlow<List<PuntoRonda>>(emptyList())
    val pasos: StateFlow<List<PuntoRonda>> = _pasos.asStateFlow()

    // Carga los puntos de inspección asociados a la ronda actual
    fun cargarPasosRonda(rondaId: String) {
        viewModelScope.launch {
            vixtaDao.obtenerPasosRonda(rondaId).collect { lista ->
                _pasos.value = lista
            }
        }
    }

    // Actualiza el estado de una casilla y guarda el timestamp automático del hardware
    fun cambiarEstadoPaso(paso: PuntoRonda, verificado: Boolean) {
        viewModelScope.launch {
            val pasoActualizado = paso.copy(
                verificado = verificado,
                fechaHoraVerificacion = if (verificado) System.currentTimeMillis() else null
            )
            vixtaDao.actualizarPuntoRonda(pasoActualizado)
        }
    }
}