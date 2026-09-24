package com.vixta.app.datos.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertaDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(alerta: AlertaEntity)

    @Query("SELECT * FROM alerta WHERE estado = 'abierta' ORDER BY abierta_en DESC")
    fun observarAbiertas(): Flow<List<AlertaEntity>>

    /** La atención queda a nombre de quien la hizo y a qué hora, como pide la restricción atencion_completa. */
    @Query(
        """
        UPDATE alerta
        SET estado = 'atendida', atendida_por = :usuarioId, atendida_en = :atendidaEn, nota_atencion = :nota
        WHERE id = :id AND estado = 'abierta'
        """
    )
    suspend fun atender(id: String, usuarioId: String, atendidaEn: String, nota: String)

    /** Los puntos fríos con al menos una alerta abierta: es el rojo del semáforo del tablero. */
    @Query(
        """
        SELECT DISTINCT r.punto_id
        FROM alerta a
        JOIN inspeccion i ON i.id = a.inspeccion_id
        JOIN ronda r ON r.id = i.ronda_id
        WHERE a.estado = 'abierta'
        """
    )
    fun observarPuntosConAlerta(): Flow<List<String>>
}
