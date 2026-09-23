package com.vixta.app.datos.sincronizacion

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vixta.app.datos.local.DatabaseProvider

class PruebaColaWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.i(
            "PruebaWorker",
            "Tarea: $id | Intento de prueba: ${runAttemptCount + 1}"
        )

        if (runAttemptCount == 0) {
            Log.i(
                "PruebaWorker",
                "Fallo simulado: se solicita un reintento"
            )
            return Result.retry()
        }
        val base = DatabaseProvider.obtener(applicationContext)

        val rondas = base.rondaDao().obtenerPendientes()
        val inspecciones = base.inspeccionDao().obtenerPendientes()

        Log.i(
            "PruebaWorker",
            "Lectura en segundo plano: ${rondas.size} rondas y " +
                    "${inspecciones.size} inspecciones pendientes"
        )

        return Result.success()
    }
}