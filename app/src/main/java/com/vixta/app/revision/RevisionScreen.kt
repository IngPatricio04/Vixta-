package com.vixta.app.revision

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun RevisionScreen(onGuardar: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().systemBarsPadding().padding(24.dp)) {
        Text("Checklist de ronda")
        Button(onClick = onGuardar) { Text("Guardar y volver al tablero") }
    }
}