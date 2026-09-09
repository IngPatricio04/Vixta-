package com.vixta.app.escaneo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscaneoScreen(
    viewModel: EscaneoViewModel,
    usuarioId: String = "USR-001",
    onCodigoEscaneado: () -> Unit, // Mantenemos tu parámetro original de navegación
    onVolverAlTablero: () -> Unit = {}
) {
    val estado by viewModel.estado.collectAsState()

    // Reacciona al cambio de estado en el ViewModel cuando se completa el registro
    LaunchedEffect(estado) {
        if (estado is EscaneoState.Exito) {
            onCodigoEscaneado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escaneo de Punto Frío", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Simulador de Escaneo QR",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Presiona el botón para simular la lectura de QR de una cámara fría e registrar la evidencia de hardware (GPS y Timestamp).",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            when (estado) {
                is EscaneoState.Cargando -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Generando registro local...")
                }
                is EscaneoState.Error -> {
                    Text(
                        text = (estado as EscaneoState.Error).mensaje,
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                else -> {}
            }

            Button(
                onClick = {
                    // Guarda la ronda en Room con UUID, fecha y coordenadas GPS automáticas
                    viewModel.procesarQrEscaneado(
                        puntoFrioId = "PF-01",
                        usuarioId = usuarioId,
                        latitud = 25.6866,
                        longitud = -100.3161,
                        precisionGps = 4.5f
                    )
                },
                enabled = estado !is EscaneoState.Cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simular escaneo")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onVolverAlTablero,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al Tablero")
            }
        }
    }
}