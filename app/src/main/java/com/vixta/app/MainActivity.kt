package com.vixta.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.vixta.app.alertas.AlertasViewModel
import com.vixta.app.dashboard.DashboardViewModel
import com.vixta.app.datos.local.Alerta
import com.vixta.app.datos.local.AppDatabase
import com.vixta.app.datos.local.EstadoAlerta
import com.vixta.app.datos.local.EstadoSemaforo
import com.vixta.app.datos.local.PuntoFrio
import com.vixta.app.datos.local.PuntoRonda
import com.vixta.app.escaneo.EscaneoViewModel
import com.vixta.app.inspeccion.InspeccionViewModel
import com.vixta.app.navegacion.VixtaNavHost
import com.vixta.app.ui.theme.VixtaTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VixtaTheme {
                val navController = rememberNavController()

                // 1. Inicialización de Room Database
                val db = remember {
                    Room.databaseBuilder(
                        applicationContext,
                        AppDatabase::class.java,
                        "vixta_db"
                    ).fallbackToDestructiveMigration().build()
                }

                val vixtaDao = db.vixtaDao()

                // 2. Seeding / Precarga automática de datos de prueba
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        // Creación de Puntos Fríos con parámetros posicionales
                        val punto1 = PuntoFrio(
                            id = "PF-01",
                            nombre = "Cámara Principal de Lácteos",
                            ubicacion = "Zona A - Nivel 1",
                            estado = EstadoSemaforo.AL_DIA
                        )
                        val punto2 = PuntoFrio(
                            id = "PF-02",
                            nombre = "Cámara Conservación Carnes",
                            ubicacion = "Zona B - Nivel 2",
                            estado = EstadoSemaforo.CON_ALERTA
                        )
                        vixtaDao.insertarPuntoFrio(punto1)
                        vixtaDao.insertarPuntoFrio(punto2)

                        // Precargar Alerta de prueba
                        val alertaPrueba = Alerta(
                            id = "ALT-101",
                            puntoFrioId = "PF-02",
                            titulo = "Desviación de Temperatura",
                            descripcion = "Temperatura detectada a +8°C (Límite: +4°C)",
                            fechaHora = System.currentTimeMillis(),
                            estado = EstadoAlerta.PENDIENTE
                        )
                        vixtaDao.atenderAlerta(alertaPrueba)

                        // Precargar pasos de inspección de demostración
                        val paso1 = PuntoRonda(
                            id = "PASO-101",
                            rondaId = "RONDA-DEMO",
                            descripcionPaso = "Verificar sello hermético de la puerta principal",
                            verificado = false
                        )
                        val paso2 = PuntoRonda(
                            id = "PASO-102",
                            rondaId = "RONDA-DEMO",
                            descripcionPaso = "Inspeccionar pantalla del termómetro digital",
                            verificado = false
                        )
                        vixtaDao.actualizarPuntoRonda(paso1)
                        vixtaDao.actualizarPuntoRonda(paso2)
                    }
                }

                // 3. Instanciar ViewModels
                val dashboardViewModel = remember { DashboardViewModel(vixtaDao) }
                val escaneoViewModel = remember { EscaneoViewModel(vixtaDao) }
                val inspeccionViewModel = remember { InspeccionViewModel(vixtaDao) }
                val alertasViewModel = remember { AlertasViewModel(vixtaDao) }

                // 4. Cargar la navegación principal
                VixtaNavHost(
                    navController = navController,
                    dashboardViewModel = dashboardViewModel,
                    escaneoViewModel = escaneoViewModel,
                    inspeccionViewModel = inspeccionViewModel,
                    alertasViewModel = alertasViewModel
                )
            }
        }
    }
}