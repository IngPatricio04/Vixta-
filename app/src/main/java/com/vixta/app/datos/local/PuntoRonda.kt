package com.vixta.app.datos.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "punto_ronda")
data class PuntoRonda(
    @PrimaryKey
    val id: String, // UUID generado en el teléfono
    val rondaId: String, // A qué ronda pertenece este paso
    val descripcionPaso: String, // Ej: "Verificar sello de empaque"
    val verificado: Boolean = false, // Marca si ya se revisó
    val fechaHoraVerificacion: Long? = null // Timestamp automático al marcarlo
)