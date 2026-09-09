package com.vixta.app.navegacion

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vixta.app.alertas.AlertasScreen
import com.vixta.app.alertas.AlertasViewModel
import com.vixta.app.configuracion.ConfiguracionScreen
import com.vixta.app.dashboard.DashboardScreen
import com.vixta.app.dashboard.DashboardViewModel
import com.vixta.app.escaneo.EscaneoScreen
import com.vixta.app.escaneo.EscaneoViewModel
import com.vixta.app.historial.HistorialScreen
import com.vixta.app.inspeccion.InspeccionScreen
import com.vixta.app.inspeccion.InspeccionViewModel
import com.vixta.app.login.LoginScreen
import com.vixta.app.revision.RevisionScreen

@Composable
fun VixtaNavHost(
    navController: NavHostController,
    dashboardViewModel: DashboardViewModel,
    escaneoViewModel: EscaneoViewModel,
    inspeccionViewModel: InspeccionViewModel,
    alertasViewModel: AlertasViewModel
) {
    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(onLoginSuccess = { navController.navigate("dashboard") })
        }

        composable("dashboard") {
            DashboardScreen(
                viewModel = dashboardViewModel,
                onIrAEscanear = { navController.navigate("escaneo") },
                onIrAAlertas = { navController.navigate("alertas") }
            )
        }

        composable("escaneo") {
            EscaneoScreen(
                viewModel = escaneoViewModel,
                onCodigoEscaneado = { navController.navigate("inspeccion") },
                onVolverAlTablero = { navController.popBackStack() }
            )
        }

        composable("inspeccion") {
            InspeccionScreen(
                viewModel = inspeccionViewModel,
                onSiguiente = { navController.navigate("revision") }
            )
        }

        composable("revision") {
            RevisionScreen(onGuardar = { navController.navigate("dashboard") })
        }

        composable("alertas") {
            AlertasScreen(
                viewModel = alertasViewModel,
                onVolver = { navController.popBackStack() }
            )
        }

        composable("historial") {
            HistorialScreen(onVolver = { navController.popBackStack() })
        }

        composable("configuracion") {
            ConfiguracionScreen(onVolver = { navController.popBackStack() })
        }
    }
}