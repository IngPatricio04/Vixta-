package com.vixta.app.navegacion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vixta.app.alertas.AlertasScreen
import com.vixta.app.alertas.AlertasViewModel
import com.vixta.app.configuracion.ConfiguracionScreen
import com.vixta.app.dashboard.DashboardScreen
import com.vixta.app.dashboard.DashboardViewModel
import com.vixta.app.datos.local.RepositorioRondas
import com.vixta.app.datos.remoto.AuthRepositorio
import com.vixta.app.escaneo.EscaneoScreen
import com.vixta.app.escaneo.EscaneoViewModel
import com.vixta.app.historial.HistorialScreen
import com.vixta.app.inspeccion.InspeccionScreen
import com.vixta.app.inspeccion.InspeccionViewModel
import com.vixta.app.login.LoginScreen
import com.vixta.app.revision.RevisionScreen
import kotlinx.coroutines.launch

@Composable
fun VixtaNavHost() {
    // Actividad 12: si ya hay una sesión guardada, la app abre directo en el tablero,
    // aunque no haya señal. Mientras se lee el teléfono (un instante) no se dibuja nada.
    val context = LocalContext.current
    var destinoInicial by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        destinoInicial = if (AuthRepositorio.cargarSesionGuardada(context) != null) "dashboard" else "login"
    }
    destinoInicial?.let { NavegacionVixta(it) }
}

@Composable
private fun NavegacionVixta(destinoInicial: String) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Una sola base para todas las pantallas: la de Pablo (vixta.db), a través del repositorio
    val repositorio = remember { RepositorioRondas(context.applicationContext) }

    NavHost(navController = navController, startDestination = destinoInicial) {

        composable("login") {
            LoginScreen(onLoginSuccess = {
                // Al entrar, el login sale de la pila: «atrás» no regresa a pedir la contraseña
                navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
            })
        }

        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel { DashboardViewModel(repositorio) },
                onIrAEscanear = { navController.navigate("escaneo") },
                onIrAAlertas = { navController.navigate("alertas") },
                onIrAConfiguracion = { navController.navigate("configuracion") }
            )
        }

        composable("escaneo") {
            EscaneoScreen(
                viewModel = viewModel { EscaneoViewModel(repositorio) },
                onCodigoEscaneado = { rondaId ->
                    // El escáner sale de la pila: «atrás» desde el checklist no abre otra ronda
                    navController.navigate("inspeccion/$rondaId") { popUpTo("escaneo") { inclusive = true } }
                },
                onVolverAlTablero = { navController.popBackStack() }
            )
        }

        // Checklist de la ronda que abrió el QR (pantalla de Cristian)
        composable("inspeccion/{rondaId}") { entrada ->
            val rondaId = entrada.arguments?.getString("rondaId").orEmpty()
            InspeccionScreen(
                viewModel = viewModel { InspeccionViewModel(repositorio) },
                rondaId = rondaId,
                onSiguiente = { navController.navigate("revision/$rondaId") }
            )
        }

        composable("revision/{rondaId}") { entrada ->
            val rondaId = entrada.arguments?.getString("rondaId").orEmpty()
            RevisionScreen(onGuardar = {
                scope.launch {
                    // Se cierra la ronda: completa si se marcaron todos los pasos. El tablero se actualiza solo
                    repositorio.cerrarRonda(rondaId)
                    navController.popBackStack("dashboard", inclusive = false)
                }
            })
        }

        composable("alertas") {
            AlertasScreen(
                viewModel = viewModel { AlertasViewModel(repositorio) },
                onVolver = { navController.popBackStack() }
            )
        }

        composable("historial") {
            HistorialScreen(onVolver = { navController.popBackStack() })
        }

        composable("configuracion") {
            ConfiguracionScreen(
                onVolver = { navController.popBackStack() },
                onCerrarSesion = {
                    // Se limpia toda la pila: después de salir, nadie regresa con «atrás»
                    navController.navigate("login") { popUpTo(navController.graph.id) { inclusive = true } }
                }
            )
        }
    }
}
