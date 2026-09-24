package com.vixta.app.configuracion
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vixta.app.datos.remoto.AuthRepositorio
import com.vixta.app.datos.remoto.SesionUsuario
import kotlinx.coroutines.launch

// Estos son los datos que la pantalla necesita para dibujarse.
// Quien conecte la data real solo tiene que crear un ConfiguracionUiState
// con los números de verdad y pasarlo aquí — no toca nada del diseño.
data class ConfiguracionUiState(
    val elementosPendientes: Int = 0,
    val fotosEvidencia: Int = 0,
    val rondasCompletas: Int = 0,
    val conflictosPorResolver: Int = 0,
    val hayConexion: Boolean = false,
    val sincronizando: Boolean = false,
    val errorSincronizacion: String? = null
)

@Composable
fun ConfiguracionScreen(
    estado: ConfiguracionUiState = ConfiguracionUiState(
        // valores de ejemplo, solo para que se vea completo mientras no hay datos reales
        elementosPendientes = 7,
        fotosEvidencia = 5,
        rondasCompletas = 2,
        conflictosPorResolver = 0,
        hayConexion = false
    ),
    onSincronizarAhora: () -> Unit = {},
    onVolver: () -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    // Actividad 12: quién tiene la sesión y con qué rol, tal como vino de la base
    val sesion by AuthRepositorio.sesion.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding() // sin esto el título queda debajo de la hora (la app dibuja de borde a borde)
            .padding(20.dp)
    ) {
        Text("Configuración", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        sesion?.let { actual ->
            TarjetaSesion(
                sesion = actual,
                onCerrarSesion = {
                    scope.launch {
                        AuthRepositorio.cerrarSesion(context)
                        onCerrarSesion()
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(listOf(Color(0xFF0E1A45), Color(0xFF1657C9)))
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Cola de sincronización", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(
                        "${estado.elementosPendientes} elementos pendientes de subir",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
                EtiquetaEstadoConexion(hayConexion = estado.hayConexion)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onSincronizarAhora,
                enabled = !estado.sincronizando,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f))
            ) {
                if (estado.sincronizando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    if (estado.sincronizando) "Sincronizando..." else "Sincronizar ahora",
                    color = Color.White
                )
            }

            if (estado.errorSincronizacion != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    estado.errorSincronizacion,
                    color = Color(0xFFFFB4AB),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        FilaResumen("Fotos de evidencia", estado.fotosEvidencia.toString())
        FilaResumen("Rondas completas", estado.rondasCompletas.toString())
        FilaResumen("Conflictos por resolver", estado.conflictosPorResolver.toString())

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "La app funciona completa sin conexión: todo se guarda primero en el teléfono. La subida automática al servidor llega con la actividad 20.",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
        }
    }
}

/** Nombre, correo y rol de quien tiene la sesión, con la salida. Colores del tema, no hex sueltos. */
@Composable
private fun TarjetaSesion(sesion: SesionUsuario, onCerrarSesion: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(sesion.nombre, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(sesion.correo, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (sesion.esSupervisor) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.primary
                    )
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    sesion.rolLegible,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (sesion.esSupervisor) MaterialTheme.colorScheme.onSecondary
                    else MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        OutlinedButton(onClick = onCerrarSesion) {
            Text("Cerrar sesión")
        }
    }
}

@Composable
private fun EtiquetaEstadoConexion(hayConexion: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (hayConexion) Color(0xFF22B07D).copy(alpha = 0.2f) else Color(0xFFF5A524).copy(alpha = 0.2f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            if (hayConexion) "Conectado" else "Sin señal",
            color = if (hayConexion) Color(0xFF22B07D) else Color(0xFFF5A524),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
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