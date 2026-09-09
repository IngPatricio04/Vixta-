package com.vixta.app.datos.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        PuntoFrio::class,
        Ronda::class,
        Alerta::class,
        PuntoRonda::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vixtaDao(): VixtaDao
}