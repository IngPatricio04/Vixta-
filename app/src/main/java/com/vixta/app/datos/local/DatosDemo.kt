package com.vixta.app.datos.local

import android.content.Context

/**
 * Datos de demostración del avance del 50 %: los puntos fríos del catálogo.
 *
 * El catálogo nace en el servidor (contrato de DATOS.md): mientras la actividad 20 no lo baje de
 * Supabase, se siembra aquí con LOS MISMOS ids y códigos que docs/modelo-datos/datos_demo.sql, para
 * que al sincronizar coincidan. El código es lo que trae el QR de cada punto.
 */
object DatosDemo {

    val PUNTOS = listOf(
        PuntoFrioEntity(
            id = "0b82dcf0-9794-4450-9c80-c8b31dc10914",
            codigo = "VX-CAM-01",
            nombre = "Cámara 01 · Lácteos",
            tipo = "camara",
            ubicacion = "Nave A · pasillo 1"
        ),
        PuntoFrioEntity(
            id = "9a085fa1-56cf-4fe5-9cbb-38b10d768b7e",
            codigo = "VX-CAM-02",
            nombre = "Cámara 02 · Cárnicos",
            tipo = "camara",
            ubicacion = "Nave A · pasillo 3"
        ),
        PuntoFrioEntity(
            id = "1245d43b-5673-4461-a1fd-ceb5862ce8b5",
            codigo = "VX-TAR-01",
            nombre = "Tarima 01 · Congelados",
            tipo = "tarima",
            ubicacion = "Nave B · congelación"
        ),
        PuntoFrioEntity(
            id = "0092e6d9-87f3-499e-8be5-83e45fa0414c",
            codigo = "VX-AND-01",
            nombre = "Andén 1 · Recepción",
            tipo = "anden",
            ubicacion = "Andén norte"
        )
    )

    /** Idempotente: si los puntos ya están, no hace nada. */
    suspend fun sembrar(context: Context) {
        DatabaseProvider.obtener(context).puntoFrioDao().insertarSiNoExisten(PUNTOS)
    }
}
