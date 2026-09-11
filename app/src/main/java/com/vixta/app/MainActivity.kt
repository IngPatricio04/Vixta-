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


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Apertura temporal de la base local para verificarla.
        lifecycleScope.launch {
            val dao = DatabaseProvider
                .obtener(applicationContext)
                .puntoFrioDao()

            dao.buscarPorCodigo("__prueba_apertura__")
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
