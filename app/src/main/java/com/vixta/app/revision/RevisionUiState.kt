package com.vixta.app.revision

data class SelloEvidencia(
    val fechaHora: String,
    val latitud: Double,
    val longitud: Double,
    val precisionMetros: Float
)

data class ItemChecklist(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val completado: Boolean = false,
    val respuestaOk: Boolean = true,
    val observacion: String = ""
)

data class RevisionUiState(
    val codigoPuntoEscaneado: String = "CAMARA-01 (Congelados)",
    val items: List<ItemChecklist> = emptyList(),
    val pasoActualIndex: Int = 0,
    val selloActual: SelloEvidencia? = null,
    val rondaFinalizada: Boolean = false
)