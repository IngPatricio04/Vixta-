package com.vixta.app.navegacion
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(onLoginSuccess = { navController.navigate("dashboard") })
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
            ConfiguracionScreen(onVolver = { navController.popBackStack() })
        }
    }
}