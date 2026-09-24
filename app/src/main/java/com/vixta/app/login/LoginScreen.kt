package com.vixta.app.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vixta.app.R
import com.vixta.app.datos.local.PreferenciasUsuario
import com.vixta.app.datos.remoto.AuthRepositorio
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image

@Composable
fun LoginScreen(onLoginSuccess: (rol: String) -> Unit) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    val ultimoUsuario by PreferenciasUsuario.obtenerUltimoUsuario(context)
        .collectAsState(initial = "")

    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var mostrarContrasena by remember { mutableStateOf(false) }

    var errorUsuario by remember { mutableStateOf<String?>(null) }
    var errorContrasena by remember { mutableStateOf<String?>(null) }
    var errorGeneral by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    // Precarga el último usuario guardado, una sola vez
    LaunchedEffect(ultimoUsuario) {
        if (usuario.isBlank() && ultimoUsuario.isNotBlank()) {
            usuario = ultimoUsuario
        }
    }

    fun intentarLogin() {
        val usuarioLimpio = usuario.trim()
        val contrasenaLimpia = contrasena.trim()

        errorUsuario = if (usuarioLimpio.isBlank()) "Escribe tu correo" else null
        errorContrasena = if (contrasenaLimpia.isBlank()) "Escribe tu contraseña" else null
        errorGeneral = null

        if (errorUsuario != null || errorContrasena != null) return

        keyboardController?.hide()
        cargando = true
        scope.launch {
            // Actividad 12: Supabase Auth valida la cuenta y el rol sale de la tabla usuario.
            // La contraseña va tal cual se escribió: un espacio puede ser parte de ella.
            val resultado = AuthRepositorio.iniciarSesion(context, usuarioLimpio, contrasena)
            cargando = false
            resultado
                .onSuccess { sesion ->
                    PreferenciasUsuario.guardarUltimoUsuario(context, sesion.correo)
                    onLoginSuccess(sesion.rol)
                }
                .onFailure { errorGeneral = it.message }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0A1230), Color(0xFF1657C9)))
            )
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_vixta),
            contentDescription = "Logo de Vixta",
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(22.dp))
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text("Vixta", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text(
            "Inspección de cadena de frío",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Tarjeta que agrupa el formulario, para separarlo visualmente del fondo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .padding(18.dp)
        ) {
            OutlinedTextField(
                value = usuario,
                onValueChange = {
                    usuario = it
                    errorUsuario = null
                    errorGeneral = null
                },
                label = { Text("Correo") },
                singleLine = true,
                isError = errorUsuario != null,
                enabled = !cargando,
                supportingText = {
                    errorUsuario?.let { Text(it, color = Color(0xFFFFB4AB)) }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    focusedLabelColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedBorderColor = Color(0xFF1FD1BE),
                    cursorColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = contrasena,
                onValueChange = {
                    contrasena = it
                    errorContrasena = null
                    errorGeneral = null
                },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (mostrarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                isError = errorContrasena != null,
                enabled = !cargando,
                supportingText = {
                    errorContrasena?.let { Text(it, color = Color(0xFFFFB4AB)) }
                },
                trailingIcon = {
                    Text(
                        text = if (mostrarContrasena) "Ocultar" else "Mostrar",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clickable { mostrarContrasena = !mostrarContrasena }
                            .padding(horizontal = 8.dp)
                            .semantics {
                                contentDescription = if (mostrarContrasena)
                                    "Ocultar contraseña" else "Mostrar contraseña"
                            }
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { intentarLogin() }),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    focusedLabelColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedBorderColor = Color(0xFF1FD1BE),
                    cursorColor = Color.White
                )
            )

            // Actividad 12: ya no se escoge el rol aquí. Lo asigna el supervisor en la base
            // (usuario.rol) y la app lo lee al entrar; escogerlo en pantalla dejaba a
            // cualquiera entrar como supervisor.
            Text(
                "Tu rol lo asigna el supervisor",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )

            if (errorGeneral != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(errorGeneral!!, color = Color(0xFFFFB4AB), fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { intentarLogin() },
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14B8A6))
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Iniciar sesión", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            "● Sesión disponible sin conexión",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}