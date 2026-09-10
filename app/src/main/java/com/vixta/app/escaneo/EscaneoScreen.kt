package com.vixta.app.escaneo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscaneoScreen(
    viewModel: EscaneoViewModel,
    usuarioId: String = "USR-001",
    onCodigoEscaneado: () -> Unit,
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Escaneo de Punto Frío",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Presiona el botón para simular la lectura de QR de una cámara fría y registrar la evidencia de hardware (GPS y Timestamp).",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            when (estado) {
                is EscaneoState.Cargando -> {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Generando registro local...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is EscaneoState.Error -> {
                    Text(
                        text = (estado as EscaneoState.Error).mensaje,
                        color = MaterialTheme.colorScheme.error,
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simular escaneo")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onVolverAlTablero,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al Tablero")
            }
        }
    }
}