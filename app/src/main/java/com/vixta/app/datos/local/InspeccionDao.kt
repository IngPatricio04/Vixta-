package com.vixta.app.datos.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InspeccionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(inspeccion: InspeccionEntity)

    @Query("SELECT * FROM inspeccion WHERE id_local = :idLocal LIMIT 1")
    suspend fun buscarPorIdLocal(idLocal: String): InspeccionEntity?
    @Query("""
    SELECT * FROM inspeccion
    WHERE sincronizada = 0
    ORDER BY capturada_en ASC, id_local ASC
""")
    suspend fun obtenerPendientes(): List<InspeccionEntity>
    @Query("""
    SELECT * FROM inspeccion
    WHERE sincronizada = 0
    ORDER BY capturada_en ASC, id_local ASC
""")
    fun observarPendientes(): Flow<List<InspeccionEntity>>
}
