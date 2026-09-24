package com.vixta.app.datos.sincronizacion

import com.vixta.app.datos.local.InspeccionEntity
import com.vixta.app.datos.local.RondaEntity

interface EnviadorPendientes {

    /**
     * Termina normalmente solo si el servidor confirmó
     * la recepción de esta versión de la ronda.
     *
     * Si falla o la recepción es incierta, debe lanzar
     * una excepción y conservarse el pendiente local.
     */
    suspend fun enviarRonda(ronda: RondaEntity)

    /**
     * Termina normalmente solo si el servidor confirmó
     * la recepción de esta versión de la inspección.
     *
     * La ronda relacionada debe existir en el servidor.
     * Si falla o la recepción es incierta, debe lanzar
     * una excepción.
     */
    suspend fun enviarInspeccion(inspeccion: InspeccionEntity)
}