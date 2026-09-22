package com.vixta.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.vixta.app.ui.theme.VixtaTheme
import com.vixta.app.navegacion.VixtaNavHost
import androidx.lifecycle.lifecycleScope
import com.vixta.app.datos.local.DatabaseProvider
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Comprobación temporal de la cola local.
        lifecycleScope.launch {
            val base = DatabaseProvider.obtener(applicationContext)

            val rondas = base.rondaDao().observarPendientes().first()
            val inspecciones = base.inspeccionDao().observarPendientes().first()
            Log.i(
                "PruebaCola",
                "Pendientes: ${rondas.size} rondas y ${inspecciones.size} inspecciones"
            )

            inspecciones.forEach { inspeccion ->
                Log.i(
                    "PruebaCola",
                    "Inspección pendiente: ${inspeccion.id_local}"
                )
            }
        }
        enableEdgeToEdge()
        setContent {
            VixtaTheme {
                VixtaNavHost()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VixtaTheme {
        Greeting("Android")
    }
}
