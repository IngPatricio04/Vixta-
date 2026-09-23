package com.vixta.app.navegacion
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vixta.app.datos.remoto.AuthRepositorio
import com.vixta.app.login.LoginScreen
import com.vixta.app.dashboard.DashboardScreen
import com.vixta.app.escaneo.EscaneoScreen
import com.vixta.app.inspeccion.InspeccionScreen
import com.vixta.app.revision.RevisionScreen
import com.vixta.app.alertas.AlertasScreen
import com.vixta.app.historial.HistorialScreen
import com.vixta.app.configuracion.ConfiguracionScreen

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

    NavHost(navController = navController, startDestination = destinoInicial) {

        composable("login") {
            LoginScreen(onLoginSuccess = {
                // Al entrar, el login sale de la pila: «atrás» no regresa a pedir la contraseña
                navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
            })
        }

        composable("dashboard") {
            DashboardScreen(
                onIrAEscanear = { navController.navigate("escaneo") },
                onIrAAlertas = { navController.navigate("alertas") },
                onIrAHistorial = { navController.navigate("historial") },
                onIrAConfiguracion = { navController.navigate("configuracion") }
            )
        }

        composable("escaneo") {
            EscaneoScreen(onCodigoEscaneado = { navController.navigate("inspeccion") })
        }

        composable("inspeccion") {
            InspeccionScreen(onSiguiente = { navController.navigate("revision") })
        }

        composable("revision") {
            RevisionScreen(onGuardar = { navController.navigate("dashboard") })
        }

        composable("alertas") {
            AlertasScreen(onVolver = { navController.popBackStack() })
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
