package com.vixta.app.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vixta.app.datos.local.PuntoFrio
import com.vixta.app.datos.local.VixtaDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    private val vixtaDao: VixtaDao
) : ViewModel() {

    // Expone la lista de puntos fríos desde Room a la pantalla en tiempo real
    val puntosFrios: StateFlow<List<PuntoFrio>> = vixtaDao.obtenerPuntosFrios()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}