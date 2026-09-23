package com.vixta.app.datos.sincronizacion

import androidx.room.withTransaction
import com.vixta.app.datos.local.InspeccionEntity
import com.vixta.app.datos.local.RondaEntity
import com.vixta.app.datos.local.VixtaDatabase

data class PendientesSincronizacion(
    val rondas: List<RondaEntity>,
    val inspecciones: List<InspeccionEntity>
)

class ColaSincronizacionLocal(
    private val base: VixtaDatabase
) {
    suspend fun obtenerPendientes(): PendientesSincronizacion {
        return base.withTransaction {
            PendientesSincronizacion(
                rondas = base.rondaDao().obtenerPendientes(),
                inspecciones = base.inspeccionDao().obtenerPendientes()
            )
        }
    }
}