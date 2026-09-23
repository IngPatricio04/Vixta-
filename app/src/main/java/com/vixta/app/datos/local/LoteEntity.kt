package com.vixta.app.datos.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lote",
    indices = [Index(value = ["codigo"], unique = true)]
)
data class LoteEntity(
    @PrimaryKey
    val id: String,
    val codigo: String,
    val descripcion: String?
)
