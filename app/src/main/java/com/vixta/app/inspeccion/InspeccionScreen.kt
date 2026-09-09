package com.vixta.app.inspeccion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vixta.app.datos.local.PuntoRonda

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspeccionScreen(
    viewModel: InspeccionViewModel,
    rondaId: String = "RONDA-DEMO",
    onSiguiente: () -> Unit
) {
    // Carga los puntos de la ronda actual al abrir la pantalla
    LaunchedEffect(rondaId) {
        viewModel.cargarPasosRonda(rondaId)
    }

    val pasos by viewModel.pasos.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de Inspección", fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onSiguiente,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Continuar")
                }
            }
        }
    ) { paddingValues ->
        if (pasos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay puntos de verificación asignados.",
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pasos) { paso ->
                    PasoItemRow(
                        paso = paso,
                        onCheckChanged = { checked ->
                            viewModel.cambiarEstadoPaso(paso, checked)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PasoItemRow(
    paso: PuntoRonda,
    onCheckChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (paso.verificado) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = paso.descripcionPaso,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Checkbox(
                checked = paso.verificado,
                onCheckedChange = onCheckChanged
            )
        }
    }
}