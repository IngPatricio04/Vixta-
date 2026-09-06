package com.vixta.app.escaneo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun EscaneoScreen(onCodigoEscaneado: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Escaneo QR")
        Button(onClick = onCodigoEscaneado) { Text("Simular escaneo") }
    }
}