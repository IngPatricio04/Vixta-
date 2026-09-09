package com.vixta.app.datos.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VixtaDao {

    // --- PUNTOS FRÍOS (PANTALLA 2: TABLERO) ---
    @Query("SELECT * FROM punto_frio")
    fun obtenerPuntosFrios(): Flow<List<PuntoFrio>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPuntoFrio(punto: PuntoFrio)

    // --- RONDAS Y CHECKLIST (PANTALLA 3 Y 5) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun crearRonda(ronda: Ronda)

    @Query("SELECT * FROM punto_ronda WHERE rondaId = :rondaId")
    fun obtenerPasosRonda(rondaId: String): Flow<List<PuntoRonda>>

    @Update
    suspend fun actualizarPuntoRonda(puntoRonda: PuntoRonda)

    // --- ALERTAS (PANTALLA 6) ---
    @Query("SELECT * FROM alerta WHERE estado = 'PENDIENTE'")
    fun obtenerAlertasPendientes(): Flow<List<Alerta>>

    @Update
    suspend fun atenderAlerta(alerta: Alerta)
}