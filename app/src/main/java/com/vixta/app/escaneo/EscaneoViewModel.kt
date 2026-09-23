package com.vixta.app.escaneo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vixta.app.datos.local.RepositorioRondas
import com.vixta.app.datos.remoto.AuthRepositorio
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

sealed interface EscaneoState {
    object Idle : EscaneoState
    object Cargando : EscaneoState
    data class Exito(val rondaId: String) : EscaneoState
    data class Error(val mensaje: String) : EscaneoState
}

class EscaneoViewModel(
    private val repositorio: RepositorioRondas
) : ViewModel() {

    private val _estado = MutableStateFlow<EscaneoState>(EscaneoState.Idle)
    val estado: StateFlow<EscaneoState> = _estado.asStateFlow()

    // La cámara entrega unas 30 lecturas por segundo del mismo QR: sólo la primera abre ronda
    private val procesando = AtomicBoolean(false)

    fun procesarQrEscaneado(codigo: String) {
        if (!procesando.compareAndSet(false, true)) return
        viewModelScope.launch {
            _estado.value = EscaneoState.Cargando
            val sesion = AuthRepositorio.sesion.value
            if (sesion == null) {
                _estado.value = EscaneoState.Error("No hay sesión activa. Vuelve a iniciar sesión")
                return@launch
            }
            try {
                // La ronda queda a nombre de quien tiene la sesión, con fecha y hora del teléfono
                repositorio.iniciarRonda(codigo, sesion)
                    .onSuccess { _estado.value = EscaneoState.Exito(it.rondaId) }
                    .onFailure { _estado.value = EscaneoState.Error(it.message ?: "Código no válido") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.value = EscaneoState.Error("Error al iniciar ronda: ${e.message}")
            }
            if (_estado.value is EscaneoState.Error) {
                // Deja leer otro código, pero no el mismo 30 veces por segundo
                delay(2000)
                procesando.set(false)
            }
        }
    }
}
