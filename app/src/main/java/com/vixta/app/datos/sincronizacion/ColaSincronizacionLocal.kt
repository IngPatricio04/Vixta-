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

    suspend fun confirmarRondaEnviada(
        enviada: RondaEntity
    ): Boolean {
        return base.withTransaction {
            val actual = base.rondaDao()
                .buscarPorIdLocal(enviada.id_local)

            if (enviada.sincronizada || actual != enviada) {
                return@withTransaction false
            }

            base.rondaDao()
                .marcarSincronizada(enviada.id_local) == 1
        }
    }

    suspend fun confirmarInspeccionEnviada(
        enviada: InspeccionEntity
    ): Boolean {
        return base.withTransaction {
            val actual = base.inspeccionDao()
                .buscarPorIdLocal(enviada.id_local)

            if (enviada.sincronizada || actual != enviada) {
                return@withTransaction false
            }

            base.inspeccionDao()
                .marcarSincronizada(enviada.id_local) == 1
        }
    }
}