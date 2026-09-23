package com.vixta.app.datos.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PuntoFrioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(punto: PuntoFrioEntity)

    @Query("SELECT * FROM punto_frio WHERE codigo = :codigo LIMIT 1")
    suspend fun buscarPorCodigo(codigo: String): PuntoFrioEntity?

    // Integración 22-sep: el tablero (pantalla 2) y la siembra del catálogo de demostración
    @Query("SELECT * FROM punto_frio WHERE activo = 1 ORDER BY nombre")
    fun observarActivos(): Flow<List<PuntoFrioEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarSiNoExisten(puntos: List<PuntoFrioEntity>)
}
