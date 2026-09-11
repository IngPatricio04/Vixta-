package com.vixta.app.datos.local

import java.time.Instant
import java.util.UUID

suspend fun probarInspeccionLocal(base: VixtaDatabase): String {
    val punto = checkNotNull(
        base.puntoFrioDao().buscarPorCodigo("PRUEBA-CAMARA-01")
    ) {
        "Primero debe existir el punto de prueba"
    }

    val ahora = Instant.now().toString()
    val usuarioId = UUID.randomUUID().toString()

    base.usuarioDao().insertar(
        UsuarioEntity(
            id = usuarioId,
            nombre = "Operador de prueba",
            correo = "$usuarioId@example.invalid",
            rol = "operador",
            activo = true,
            creado_en = ahora
        )
    )

    val rondaId = UUID.randomUUID().toString()

    base.rondaDao().insertar(
        RondaEntity(
            id = rondaId,
            id_local = UUID.randomUUID().toString(),
            punto_id = punto.id,
            usuario_id = usuarioId,
            iniciada_en = ahora
        )
    )

    val inspeccion = InspeccionEntity(
        id = UUID.randomUUID().toString(),
        id_local = UUID.randomUUID().toString(),
        ronda_id = rondaId,
        lote_id = null,
        foto_url = null,
        clase = "integro",
        confianza = null,
        origen = "manual",
        capturada_en = ahora,
        lat = null,
        lon = null,
        precision_gps = null,
        sincronizada = false
    )

    base.inspeccionDao().insertar(inspeccion)

    val recuperada = base.inspeccionDao()
        .buscarPorIdLocal(inspeccion.id_local)

    check(recuperada == inspeccion) {
        "La inspección recuperada no coincide con la guardada"
    }

    return inspeccion.id_local
}
