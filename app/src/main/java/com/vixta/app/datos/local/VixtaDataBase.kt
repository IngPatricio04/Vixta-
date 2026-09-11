package com.vixta.app.datos.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PuntoFrioEntity::class],
    version = 1,
    exportSchema = true
)
abstract class VixtaDatabase : RoomDatabase() {

    abstract fun puntoFrioDao(): PuntoFrioDao
}
