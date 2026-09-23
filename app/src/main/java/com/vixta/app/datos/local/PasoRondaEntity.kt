package com.vixta.app.datos.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Un paso del checklist de una ronda: la tabla paso_ronda del esquema.
 * Nace en el teléfono, así que su id es un UUID generado aquí (contrato de DATOS.md, decisión 2).
 */
@Entity(
    tableName = "paso_ronda",
    foreignKeys = [
        ForeignKey(
            entity = RondaEntity::class,
            parentColumns = ["id"],
            childColumns = ["ronda_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["ronda_id", "orden"], unique = true)
    ]
)
data class PasoRondaEntity(
    @PrimaryKey
    val id: String,
    val ronda_id: String,
    val orden: Int,
    val descripcion: String,
    val completado: Boolean = false,
    val completado_en: String? = null
)
