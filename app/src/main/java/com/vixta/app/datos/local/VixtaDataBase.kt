package com.vixta.app.datos.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        PuntoFrioEntity::class,
        UsuarioEntity::class,
        RondaEntity::class,
        LoteEntity::class,
        InspeccionEntity::class
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class VixtaDatabase : RoomDatabase() {

    abstract fun puntoFrioDao(): PuntoFrioDao

    abstract fun usuarioDao(): UsuarioDao

    abstract fun rondaDao(): RondaDao

    abstract fun inspeccionDao(): InspeccionDao
}
