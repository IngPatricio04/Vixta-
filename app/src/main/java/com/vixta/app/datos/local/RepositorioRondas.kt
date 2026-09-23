package com.vixta.app.datos.local

import android.content.Context
import androidx.room.withTransaction
import com.vixta.app.datos.remoto.SesionUsuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

/** El semáforo del tablero (pantalla 2). */
enum class EstadoSemaforo {
    AL_DIA,
    PENDIENTE,
    CON_ALERTA
}

/** Un punto frío como lo pinta el tablero: el dato de la base más su semáforo, calculado. */
data class PuntoTablero(
    val id: String,
    val codigo: String,
    val nombre: String,
    val ubicacion: String,
    val estado: EstadoSemaforo
)

/** Lo que devuelve escanear un QR válido: la ronda que se abrió y en qué punto. */
data class RondaIniciada(val rondaId: String, val puntoNombre: String)

/**
 * Lo que las pantallas de la ronda leen y escriben: tablero, escaneo, checklist y alertas.
 *
 * - Una sola base, la de Pablo (vixta.db). Las pantallas no tocan los DAO directo: pasan por aquí
 *   (contrato de DATOS.md, decisión 4).
 * - Offline-first: todo se escribe en Room y queda con sincronizada = false para la cola.
 * - Lo que nace en el teléfono lleva el mismo UUID en id y en id_local (decisión 2).
 * - Las fechas van en ISO-8601 UTC (decisión 3).
 */
class RepositorioRondas(context: Context) {

    private val base = DatabaseProvider.obtener(context)

    /**
     * Pantalla 2. El semáforo sale de los datos, no se guarda:
     * con alerta = tiene una alerta abierta · al día = tiene una ronda completa hoy · pendiente = lo demás.
     */
    fun observarTablero(): Flow<List<PuntoTablero>> =
        combine(
            base.puntoFrioDao().observarActivos(),
            base.rondaDao().observarDesde(inicioDeHoy()),
            base.alertaDao().observarPuntosConAlerta()
        ) { puntos, rondasDeHoy, puntosConAlerta ->
            val alDia = rondasDeHoy.filter { it.estado == "completa" }.map { it.punto_id }.toSet()
            val conAlerta = puntosConAlerta.toSet()
            puntos.map { p ->
                PuntoTablero(
                    id = p.id,
                    codigo = p.codigo,
                    nombre = p.nombre,
                    ubicacion = p.ubicacion.orEmpty(),
                    estado = when (p.id) {
                        in conAlerta -> EstadoSemaforo.CON_ALERTA
                        in alDia -> EstadoSemaforo.AL_DIA
                        else -> EstadoSemaforo.PENDIENTE
                    }
                )
            }
        }

    /**
     * Pantalla 3. El QR trae punto_frio.codigo. Si el punto existe, abre una ronda a nombre de quien
     * tiene la sesión, con su checklist. Todo en una transacción: o queda completo, o no queda nada.
     */
    suspend fun iniciarRonda(codigoQr: String, sesion: SesionUsuario): Result<RondaIniciada> {
        val codigo = codigoQr.trim()
        val punto = base.puntoFrioDao().buscarPorCodigo(codigo)
            ?: return Result.failure(IllegalArgumentException("El código «$codigo» no corresponde a ningún punto frío"))
        val ahora = ahora()
        val rondaId = UUID.randomUUID().toString()
        base.withTransaction {
            // La ronda apunta a usuario: quien tiene la sesión tiene que existir también en la base local
            if (base.usuarioDao().buscarPorId(sesion.usuarioId) == null) {
                base.usuarioDao().insertar(
                    UsuarioEntity(
                        id = sesion.usuarioId,
                        nombre = sesion.nombre,
                        correo = sesion.correo,
                        rol = sesion.rol,
                        activo = true,
                        creado_en = ahora
                    )
                )
            }
            base.rondaDao().insertar(
                RondaEntity(
                    id = rondaId,
                    id_local = rondaId,
                    punto_id = punto.id,
                    usuario_id = sesion.usuarioId,
                    iniciada_en = ahora,
                    estado = "en_curso",
                    sincronizada = false
                )
            )
            base.pasoRondaDao().insertarTodos(
                PASOS.mapIndexed { i, descripcion ->
                    PasoRondaEntity(
                        id = UUID.randomUUID().toString(),
                        ronda_id = rondaId,
                        orden = i + 1,
                        descripcion = descripcion
                    )
                }
            )
        }
        return Result.success(RondaIniciada(rondaId, punto.nombre))
    }

    /** Pantalla 5: los pasos de la ronda, en tiempo real. */
    fun observarPasos(rondaId: String): Flow<List<PasoRondaEntity>> =
        base.pasoRondaDao().observarDeRonda(rondaId)

    /** La hora de cada paso la pone el teléfono, nunca quien usa la app. */
    suspend fun marcarPaso(paso: PasoRondaEntity, completado: Boolean) {
        base.pasoRondaDao().marcar(paso.id, completado, if (completado) ahora() else null)
    }

    /** Cierra la ronda: completa si se marcaron todos sus pasos, incompleta si no. */
    suspend fun cerrarRonda(rondaId: String) {
        val pasos = base.pasoRondaDao().obtenerDeRonda(rondaId)
        val estado = if (pasos.isNotEmpty() && pasos.all { it.completado }) "completa" else "incompleta"
        base.rondaDao().cerrar(rondaId, estado, ahora())
    }

    /** Pantalla 6: las alertas abiertas. */
    fun observarAlertasAbiertas(): Flow<List<AlertaEntity>> = base.alertaDao().observarAbiertas()

    /** Sólo el supervisor atiende, y queda a su nombre: lo mismo que exige la política RLS alerta_atender. */
    suspend fun atenderAlerta(alerta: AlertaEntity, nota: String, sesion: SesionUsuario): Result<Unit> {
        if (!sesion.esSupervisor) {
            return Result.failure(IllegalStateException("Sólo un supervisor puede atender alertas"))
        }
        base.alertaDao().atender(alerta.id, sesion.usuarioId, ahora(), nota.trim())
        return Result.success(Unit)
    }

    companion object {
        /**
         * El checklist por omisión. En el esquema los pasos son datos, no código: en la actividad 17
         * vendrán del servidor y cambiar la ronda no requerirá recompilar.
         */
        val PASOS = listOf(
            "Verificar el sello hermético de la puerta",
            "Revisar la lectura del termómetro",
            "Inspeccionar los empaques visibles",
            "Confirmar que las etiquetas de lote se leen"
        )

        private fun ahora(): String = Instant.now().toString()

        private fun inicioDeHoy(): String =
            LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toString()
    }
}
