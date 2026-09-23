package com.vixta.app.datos.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Lo que hay que atender, con constancia de quién lo hizo: la tabla alerta del esquema.
 * Nace de una inspección (la evidencia que la originó). Sólo un supervisor la atiende.
 */
@Entity(
    tableName = "alerta",
    foreignKeys = [
        ForeignKey(
            entity = InspeccionEntity::class,
            parentColumns = ["id"],
            childColumns = ["inspeccion_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["inspeccion_id"]),
        Index(value = ["estado"])
    ]
)
data class AlertaEntity(
    @PrimaryKey
    val id: String,
    val inspeccion_id: String,
    val tipo: String,
    val severidad: String,
    val abierta_en: String,
    val atendida_por: String? = null,
    val atendida_en: String? = null,
    val nota_atencion: String? = null,
    val estado: String = "abierta"
) {
    init {
        require(severidad == "baja" || severidad == "media" || severidad == "alta") {
            "La severidad debe ser baja, media o alta"
        }
        require(estado == "abierta" || estado == "atendida" || estado == "cerrada") {
            "El estado debe ser abierta, atendida o cerrada"
        }
    }
}
