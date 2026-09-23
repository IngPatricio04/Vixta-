package com.vixta.app.datos.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ronda",
    foreignKeys = [
        ForeignKey(
            entity = PuntoFrioEntity::class,
            parentColumns = ["id"],
            childColumns = ["punto_id"]
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuario_id"]
        )
    ],
    indices = [
        Index(value = ["id_local"], unique = true),
        Index(value = ["punto_id", "iniciada_en"]),
        Index(value = ["usuario_id"])
    ]
)
data class RondaEntity(
    @PrimaryKey
    val id: String,
    val id_local: String,
    val punto_id: String,
    val usuario_id: String,
    val iniciada_en: String,
    val cerrada_en: String? = null,
    val estado: String = "en_curso",
    val sincronizada: Boolean = false
) {
    init {
        require(
            estado == "en_curso" ||
                    estado == "completa" ||
                    estado == "incompleta"
        ) {
            "El estado debe ser en_curso, completa o incompleta"
        }
    }
}
