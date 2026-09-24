package com.vixta.app.datos.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuario",
    indices = [Index(value = ["correo"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey
    val id: String,
    val nombre: String,
    val correo: String,
    val rol: String,
    val activo: Boolean = true,
    val creado_en: String
) {
    init {
        require(rol == "operador" || rol == "supervisor") {
            "El rol debe ser operador o supervisor"
        }
    }
}