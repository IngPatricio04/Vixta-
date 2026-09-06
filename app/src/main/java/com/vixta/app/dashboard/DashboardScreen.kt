package com.vixta.app.dashboard
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun DashboardScreen(
    onIrAEscanear: () -> Unit,
    onIrAAlertas: () -> Unit,
    onIrAHistorial: () -> Unit,
    onIrAConfiguracion: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Dashboard")
        Button(onClick = onIrAEscanear) { Text("Escanear") }
        Button(onClick = onIrAAlertas) { Text("Alertas") }
        Button(onClick = onIrAHistorial) { Text("Historial") }
        Button(onClick = onIrAConfiguracion) { Text("Configuración") }
    }
}