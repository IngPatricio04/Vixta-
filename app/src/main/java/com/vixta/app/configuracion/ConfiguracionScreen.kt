package com.vixta.app.configuracion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConfiguracionScreen(onVolver: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Configuración", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Tarjeta de cola de sincronización
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(listOf(Color(0xFF0E1A45), Color(0xFF1657C9)))
                )
                .padding(16.dp)
        ) {
            Text("Cola de sincronización", color = Color.White, fontWeight = FontWeight.Bold)
            Text(
                "7 elementos pendientes de subir",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { /* TODO: disparar sincronización manual */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f))
            ) {
                Text("Sincronizar ahora", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        FilaResumen("Fotos de evidencia", "5")
        FilaResumen("Rondas completas", "2")
        FilaResumen("Conflictos por resolver", "0")

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "La app funciona completa sin conexión; todo se guarda localmente y se sube solo al recuperar la red.",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
        }
    }
}

@Composable
private fun FilaResumen(titulo: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(titulo, fontSize = 14.sp)
        Text(valor, fontWeight = FontWeight.Bold)
    }
}