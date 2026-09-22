
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
}
