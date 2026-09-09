package com.vixta.app.datos.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ronda")
data class Ronda(
    @PrimaryKey
    val id: String, // UUID generado localmente
    val puntoFrioId: String, // Relación con el punto frío escaneado
    val usuarioId: String, // Quién realizó la ronda
    val fechaHora: Long, // Timestamp automático
    val latitud: Double, // Captura automática del GPS
    val longitud: Double, // Captura automática del GPS
    val precisionGps: Float, // Precisión en metros
    val completada: Boolean = false
)