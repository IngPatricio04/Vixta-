package com.vixta.app.alertas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vixta.app.datos.local.Alerta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertasScreen(
    viewModel: AlertasViewModel,
    onVolver: () -> Unit
) {
    val alertas by viewModel.alertasPendientes.collectAsState()
    var alertaAAtender by remember { mutableStateOf<Alerta?>(null) }
    var notaTexto by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alertas Pendientes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (alertas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay alertas pendientes por atender.",
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
                items(alertas) { alerta ->
                    AlertaItemCard(
                        alerta = alerta,
                        onAtenderClick = { alertaAAtender = alerta }
                    )
                }
            }
        }

        // Diálogo modal para ingresar la constancia de atención escrita
        alertaAAtender?.let { alerta ->
            AlertDialog(
                onDismissRequest = { alertaAAtender = null },
                title = { Text("Atender Alerta") },
                text = {
                    Column {
                        Text("Ingresa la constancia de atención para: ${alerta.titulo}")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = notaTexto,
                            onValueChange = { notaTexto = it },
                            label = { Text("Nota de atención") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (notaTexto.isNotBlank()) {
                                viewModel.atenderAlerta(alerta, notaTexto)
                                notaTexto = ""
                                alertaAAtender = null
                            }
                        }
                    ) {
                        Text("Guardar Constancia")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { alertaAAtender = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun AlertaItemCard(
    alerta: Alerta,
    onAtenderClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = alerta.titulo,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC62828)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = alerta.descripcion,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onAtenderClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Atender Evento")
            }
        }
    }
}