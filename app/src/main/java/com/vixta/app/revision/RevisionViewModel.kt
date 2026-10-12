package com.vixta.app.revision

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RevisionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RevisionUiState())
    val uiState: StateFlow<RevisionUiState> = _uiState.asStateFlow()

    init {
        cargarChecklistInicial()
    }

    private fun cargarChecklistInicial() {
        val itemsPrueba = listOf(
            ItemChecklist(
                id = "1",
                titulo = "Temperatura de Cámara",
                descripcion = "Verificar que el indicador marque entre -18°C y -22°C."
            ),
            ItemChecklist(
                id = "2",
                titulo = "Sellado de Cortina / Puerta",
                descripcion = "Inspeccionar que no existan fugas de aire o empaques rotos."
            ),
            ItemChecklist(
                id = "3",
                titulo = "Estiba y Estado de Empaques",
                descripcion = "Verificar que las cajas no presenten aplastamiento o escarcha excesiva."
            )
        )

        _uiState.update { it.copy(items = itemsPrueba) }
        generarSelloAutomatico()
    }

    fun generarSelloAutomatico(lat: Double = 25.6580, lng: Double = -100.3662, precision: Float = 4.5f) {
        val formatoFecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val fechaActual = formatoFecha.format(Date())

        val nuevoSello = SelloEvidencia(
            fechaHora = fechaActual,
            latitud = lat,
            longitud = lng,
            precisionMetros = precision
        )

        _uiState.update { it.copy(selloActual = nuevoSello) }
    }

    fun responderPasoActual(esCorrecto: Boolean, observacion: String = "") {
        val estado = _uiState.value
        val itemsActualizados = estado.items.toMutableList()
        val index = estado.pasoActualIndex

        if (index in itemsActualizados.indices) {
            itemsActualizados[index] = itemsActualizados[index].copy(
                completado = true,
                respuestaOk = esCorrecto,
                observacion = observacion
            )

            val esUltimoPaso = index == itemsActualizados.lastIndex
            _uiState.update {
                it.copy(
                    items = itemsActualizados,
                    pasoActualIndex = if (esUltimoPaso) index else index + 1,
                    rondaFinalizada = esUltimoPaso
                )
            }
            generarSelloAutomatico()
        }
    }
}