package com.vixta.app.alertas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vixta.app.datos.local.Alerta
import com.vixta.app.datos.local.EstadoAlerta
import com.vixta.app.datos.local.VixtaDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlertasViewModel(
    private val vixtaDao: VixtaDao
) : ViewModel() {

    // Obtiene las alertas pendientes en tiempo real desde Room
    val alertasPendientes: StateFlow<List<Alerta>> = vixtaDao.obtenerAlertasPendientes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Atiende una alerta guardando la constancia del operador en la base de datos
    fun atenderAlerta(alerta: Alerta, nota: String) {
        viewModelScope.launch {
            val alertaAtendida = alerta.copy(
                estado = EstadoAlerta.ATENDIDA,
                notaAtencion = nota
            )
            vixtaDao.atenderAlerta(alertaAtendida)
        }
    }
}