package com.vixta.app.datos.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "punto_frio",
    indices = [Index(value = ["codigo"], unique = true)]
)
data class PuntoFrioEntity(
    @PrimaryKey
    val id: String,
    val codigo: String,
    val nombre: String,
    val tipo: String,
    val ubicacion: String?,
    val activo: Boolean = true
)
