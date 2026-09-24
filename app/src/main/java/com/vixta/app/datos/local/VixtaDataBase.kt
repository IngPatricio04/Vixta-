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
        InspeccionEntity::class,
        // Versión 3 (integración 22-sep): paso_ronda y alerta, como pide el contrato de DATOS.md
        PasoRondaEntity::class,
        AlertaEntity::class
    ],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3)
    ]
)
abstract class VixtaDatabase : RoomDatabase() {

    abstract fun puntoFrioDao(): PuntoFrioDao

    abstract fun usuarioDao(): UsuarioDao

    abstract fun rondaDao(): RondaDao

    abstract fun inspeccionDao(): InspeccionDao

    abstract fun pasoRondaDao(): PasoRondaDao

    abstract fun alertaDao(): AlertaDao
}
