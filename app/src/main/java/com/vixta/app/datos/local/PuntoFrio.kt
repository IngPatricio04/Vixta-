package com.vixta.app.datos.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EstadoSemaforo {
    AL_DIA,
    PENDIENTE,
    CON_ALERTA
}

@Entity(tableName = "punto_frio")
data class PuntoFrio(
    @PrimaryKey
    val id: String,
    val nombre: String,
    val ubicacion: String,
    val estado: EstadoSemaforo,
    val ultimaInspeccion: Long? = null
)