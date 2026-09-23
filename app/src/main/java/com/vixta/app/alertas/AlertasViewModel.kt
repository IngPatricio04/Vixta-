package com.vixta.app.alertas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vixta.app.datos.local.AlertaEntity
import com.vixta.app.datos.local.RepositorioRondas
import com.vixta.app.datos.remoto.AuthRepositorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlertasViewModel(
    private val repositorio: RepositorioRondas
) : ViewModel() {

    // Obtiene las alertas abiertas en tiempo real desde la base local
    val alertasPendientes: StateFlow<List<AlertaEntity>> = repositorio.observarAlertasAbiertas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    // Atiende una alerta a nombre del supervisor que tiene la sesión, con su constancia
    fun atenderAlerta(alerta: AlertaEntity, nota: String) {
        viewModelScope.launch {
            val sesion = AuthRepositorio.sesion.value ?: return@launch
            repositorio.atenderAlerta(alerta, nota, sesion)
                .onFailure { _mensaje.value = it.message }
        }
    }
}
