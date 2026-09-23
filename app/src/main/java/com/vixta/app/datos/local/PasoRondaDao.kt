package com.vixta.app.datos.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PasoRondaDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarTodos(pasos: List<PasoRondaEntity>)

    @Query("SELECT * FROM paso_ronda WHERE ronda_id = :rondaId ORDER BY orden")
    fun observarDeRonda(rondaId: String): Flow<List<PasoRondaEntity>>

    @Query("SELECT * FROM paso_ronda WHERE ronda_id = :rondaId ORDER BY orden")
    suspend fun obtenerDeRonda(rondaId: String): List<PasoRondaEntity>

    @Query("UPDATE paso_ronda SET completado = :completado, completado_en = :completadoEn WHERE id = :id")
    suspend fun marcar(id: String, completado: Boolean, completadoEn: String?)
}
