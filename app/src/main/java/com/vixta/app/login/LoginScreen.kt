package com.vixta.app.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(onLoginSuccess: (rol: String) -> Unit) {
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf("Operador") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A1230), Color(0xFF1657C9))
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Vixta",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Inspección de cadena de frío",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                focusedLabelColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                focusedLabelColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Ingresar como", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Operador", "Supervisor").forEach { rol ->
                val seleccionado = rol == rolSeleccionado
                Button(
                    onClick = { rolSeleccionado = rol },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (seleccionado) Color.White else Color.Transparent,
                        contentColor = if (seleccionado) Color(0xFF0E1A45) else Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(rol, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { onLoginSuccess(rolSeleccionado) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14B8A6))
        ) {
            Text("Iniciar sesión", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "● Sesión disponible sin conexión",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp
        )
    }
}