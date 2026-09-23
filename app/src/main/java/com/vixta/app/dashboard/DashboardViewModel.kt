package com.vixta.app.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vixta.app.datos.local.PuntoTablero
import com.vixta.app.datos.local.RepositorioRondas
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    repositorio: RepositorioRondas
) : ViewModel() {

    // Expone los puntos fríos con su semáforo, desde la base local, en tiempo real
    val puntosFrios: StateFlow<List<PuntoTablero>> = repositorio.observarTablero()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
