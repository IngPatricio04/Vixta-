package com.vixta.app.datos.sincronizacion

class SincronizadorPendientes(
    private val cola: ColaSincronizacionLocal,
    private val enviador: EnviadorPendientes
) {
    suspend fun sincronizar(): Boolean {
        val pendientes = cola.obtenerPendientes()

        for (ronda in pendientes.rondas) {
            enviador.enviarRonda(ronda)

            if (!cola.confirmarRondaEnviada(ronda)) {
                return false
            }
        }

        for (inspeccion in pendientes.inspecciones) {
            enviador.enviarInspeccion(inspeccion)

            if (!cola.confirmarInspeccionEnviada(inspeccion)) {
                return false
            }
        }

        return true
    }
}