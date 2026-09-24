package com.vixta.app.inspeccion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vixta.app.datos.local.PasoRondaEntity
import com.vixta.app.datos.local.RepositorioRondas
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InspeccionViewModel(
    private val repositorio: RepositorioRondas
) : ViewModel() {

    private val _pasos = MutableStateFlow<List<PasoRondaEntity>>(emptyList())
    val pasos: StateFlow<List<PasoRondaEntity>> = _pasos.asStateFlow()

    private var observacion: Job? = null

    // Carga los pasos de la ronda que se acaba de abrir con el QR
    fun cargarPasosRonda(rondaId: String) {
        observacion?.cancel()
        observacion = viewModelScope.launch {
            repositorio.observarPasos(rondaId).collect { lista -> _pasos.value = lista }
        }
    }

    // Marca o desmarca un paso; la hora la pone el teléfono
    fun cambiarEstadoPaso(paso: PasoRondaEntity, verificado: Boolean) {
        viewModelScope.launch {
            repositorio.marcarPaso(paso, verificado)
        }
    }
}
