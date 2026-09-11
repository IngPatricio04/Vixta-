package com.vixta.app.datos.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PuntoFrioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(punto: PuntoFrioEntity)

    @Query("SELECT * FROM punto_frio WHERE codigo = :codigo LIMIT 1")
    suspend fun buscarPorCodigo(codigo: String): PuntoFrioEntity?
}
