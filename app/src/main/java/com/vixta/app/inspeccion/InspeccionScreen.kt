package com.vixta.app.inspeccion

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun InspeccionScreen(onSiguiente: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Inspección con IA")
        Button(onClick = onSiguiente) { Text("Continuar") }
    }
}