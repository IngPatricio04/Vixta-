package com.vixta.app.datos.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inspeccion",
    foreignKeys = [
        ForeignKey(
            entity = RondaEntity::class,
            parentColumns = ["id"],
            childColumns = ["ronda_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["lote_id"]
        )
    ],
    indices = [
        Index(value = ["id_local"], unique = true),
        Index(value = ["ronda_id"]),
        Index(value = ["lote_id"]),
        Index(value = ["capturada_en"])
    ]
)
data class InspeccionEntity(
    @PrimaryKey
    val id: String,
    val id_local: String,
    val ronda_id: String,
    val lote_id: String?,
    val foto_url: String?,
    val clase: String,
    val confianza: Double?,
    val origen: String,
    val capturada_en: String,
    val lat: Double?,
    val lon: Double?,
    val precision_gps: Double?,
    val sincronizada: Boolean = false
) {
    init {
        require(
            clase in setOf(
                "integro",
                "empaque_danado",
                "contaminacion",
                "etiqueta_ilegible"
            )
        ) {
            "La clase de inspección no es válida"
        }

        require(origen == "modelo" || origen == "manual") {
            "El origen debe ser modelo o manual"
        }

        require(confianza == null || confianza in 0.0..1.0) {
            "La confianza debe estar entre 0 y 1"
        }

        require(origen != "modelo" || confianza != null) {
            "Una clasificación del modelo debe tener confianza"
        }
    }
}
