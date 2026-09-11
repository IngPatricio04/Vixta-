
package com.vixta.app.datos.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RondaDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(ronda: RondaEntity)

    @Query("SELECT * FROM ronda WHERE id_local = :idLocal LIMIT 1")
    suspend fun buscarPorIdLocal(idLocal: String): RondaEntity?
}
