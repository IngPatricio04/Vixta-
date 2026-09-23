package com.vixta.app

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vixta.app.datos.local.*
import com.vixta.app.datos.sincronizacion.ColaSincronizacionLocal
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ColaSincronizacionLocalTest {

    @Test
    fun confirmaSoloLaVersionEnviada() = runBlocking {
        val context =
            InstrumentationRegistry.getInstrumentation().targetContext

        val base = Room.inMemoryDatabaseBuilder(
            context,
            VixtaDatabase::class.java
        ).build()

        try {
            base.puntoFrioDao().insertar(
                PuntoFrioEntity(
                    id = "punto-prueba",
                    codigo = "PRUEBA-MEMORIA",
                    nombre = "Punto simulado",
                    tipo = "camara",
                    ubicacion = null
                )
            )

            base.usuarioDao().insertar(
                UsuarioEntity(
                    id = "usuario-prueba",
                    nombre = "Usuario simulado",
                    correo = "prueba@example.invalid",
                    rol = "operador",
                    creado_en = "2026-09-22T00:00:00Z"
                )
            )

            val enviada = RondaEntity(
                id = "ronda-prueba",
                id_local = "ronda-local-prueba",
                punto_id = "punto-prueba",
                usuario_id = "usuario-prueba",
                iniciada_en = "2026-09-22T00:00:00Z"
            )

            // Representa un cambio local ocurrido durante el envío.
            val actual = enviada.copy(estado = "completa")
            base.rondaDao().insertar(actual)

            val cola = ColaSincronizacionLocal(base)

            // La confirmación de la versión anterior debe rechazarse.
            assertFalse(cola.confirmarRondaEnviada(enviada))
            assertEquals(
                actual,
                base.rondaDao().buscarPorIdLocal(actual.id_local)
            )
            assertEquals(1, cola.obtenerPendientes().rondas.size)

            // Simulamos confirmar la versión que sí coincide.
            assertTrue(cola.confirmarRondaEnviada(actual))
            assertEquals(
                actual.copy(sincronizada = true),
                base.rondaDao().buscarPorIdLocal(actual.id_local)
            )
            assertTrue(cola.obtenerPendientes().rondas.isEmpty())

            // Una segunda confirmación no debe volver a modificarla.
            assertFalse(cola.confirmarRondaEnviada(actual))
            val inspeccionEnviada = InspeccionEntity(
                id = "inspeccion-prueba",
                id_local = "inspeccion-local-prueba",
                ronda_id = actual.id,
                lote_id = null,
                foto_url = null,
                clase = "integro",
                confianza = null,
                origen = "manual",
                capturada_en = "2026-09-22T00:00:00Z",
                lat = null,
                lon = null,
                precision_gps = null
            )

// Representa un cambio local ocurrido durante el envío.
            val inspeccionActual = inspeccionEnviada.copy(
                clase = "empaque_danado"
            )
            base.inspeccionDao().insertar(inspeccionActual)

// La confirmación de la versión anterior debe rechazarse.
            assertFalse(
                cola.confirmarInspeccionEnviada(inspeccionEnviada)
            )
            assertEquals(
                inspeccionActual,
                base.inspeccionDao()
                    .buscarPorIdLocal(inspeccionActual.id_local)
            )
            assertEquals(1, cola.obtenerPendientes().inspecciones.size)

// La confirmación de la versión actual sí debe aceptarse.
            assertTrue(
                cola.confirmarInspeccionEnviada(inspeccionActual)
            )
            assertEquals(
                inspeccionActual.copy(sincronizada = true),
                base.inspeccionDao()
                    .buscarPorIdLocal(inspeccionActual.id_local)
            )
            assertTrue(cola.obtenerPendientes().inspecciones.isEmpty())

// Repetir la confirmación no debe volver a modificarla.
            assertFalse(
                cola.confirmarInspeccionEnviada(inspeccionActual)
            )
        } finally {
            base.close()
        }
    }
}