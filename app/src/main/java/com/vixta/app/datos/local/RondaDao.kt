
package com.vixta.app.datos.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RondaDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(ronda: RondaEntity)

    @Query("SELECT * FROM ronda WHERE id_local = :idLocal LIMIT 1")
    suspend fun buscarPorIdLocal(idLocal: String): RondaEntity?
    @Query("""
    SELECT * FROM ronda
    WHERE sincronizada = 0
    ORDER BY iniciada_en ASC, id_local ASC
""")
    suspend fun obtenerPendientes(): List<RondaEntity>
    @Query("""
    SELECT * FROM ronda
    WHERE sincronizada = 0
    ORDER BY iniciada_en ASC, id_local ASC
""")
    fun observarPendientes(): Flow<List<RondaEntity>>

    // Integración 22-sep: el semáforo del tablero y el cierre de la ronda
    @Query("SELECT * FROM ronda WHERE iniciada_en >= :desde")
    fun observarDesde(desde: String): Flow<List<RondaEntity>>

    /** Todo cambio local vuelve a marcar la ronda como pendiente de subir. */
    @Query("UPDATE ronda SET estado = :estado, cerrada_en = :cerradaEn, sincronizada = 0 WHERE id = :id")
    suspend fun cerrar(id: String, estado: String, cerradaEn: String)
}
