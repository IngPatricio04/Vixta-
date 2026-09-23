package com.vixta.app.datos.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var instancia: VixtaDatabase? = null

    fun obtener(context: Context): VixtaDatabase {
        return instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(
                context.applicationContext,
                VixtaDatabase::class.java,
                "vixta.db"
            ).build().also {
                instancia = it
            }
        }
    }
}