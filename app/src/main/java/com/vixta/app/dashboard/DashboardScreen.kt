package com.vixta.app.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vixta.app.datos.local.EstadoSemaforo
import com.vixta.app.datos.local.PuntoFrio

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onIrAEscanear: () -> Unit,
    onIrAAlertas: () -> Unit
) {
    // Escucha la base de datos Room en tiempo real mediante StateFlow
    val puntosFrios by viewModel.puntosFrios.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tablero de Puntos Fríos", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = onIrAAlertas) {
                        Text("Alertas")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onIrAEscanear,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("Escanear QR")
            }
        }
    ) { paddingValues ->
        if (puntosFrios.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay puntos fríos registrados",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(puntosFrios) { punto ->
                    PuntoFrioItem(punto = punto)
                }
            }
        }
    }
}

@Composable
fun PuntoFrioItem(punto: PuntoFrio) {
    val colorSemaforo = when (punto.estado) {
        EstadoSemaforo.AL_DIA -> Color(0xFF2E7D32) // Verde
        EstadoSemaforo.PENDIENTE -> Color(0xFFF57C00) // Naranja / Amarillo
        EstadoSemaforo.CON_ALERTA -> Color(0xFFC62828) // Rojo
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = punto.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ubicación: ${punto.ubicacion}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Indicador visual del Semáforo
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(color = colorSemaforo, shape = CircleShape)
            )
        }
    }
}