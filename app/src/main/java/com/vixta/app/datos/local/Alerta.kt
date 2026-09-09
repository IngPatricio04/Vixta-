package com.vixta.app.datos.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EstadoAlerta {
    PENDIENTE,
    ATENDIDA
}

@Entity(tableName = "alerta")
data class Alerta(
    @PrimaryKey
    val id: String, // UUID generado localmente
    val puntoFrioId: String, // En qué cámara ocurrió
    val titulo: String, // Ej: "Temperatura fuera de rango"
    val descripcion: String,
    val fechaHora: Long, // Sello de tiempo automático
    val estado: EstadoAlerta = EstadoAlerta.PENDIENTE,
    val notaAtencion: String? = null // Constancia de la solución
)