package com.vixta.app.escaneo

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

private const val TAG = "EscaneoScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscaneoScreen(
    viewModel: EscaneoViewModel,
    onCodigoEscaneado: (rondaId: String) -> Unit,
    onVolverAlTablero: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val estado by viewModel.estado.collectAsState()

    // Estado local para verificar si el usuario aceptó el permiso
    var tienePermisoCamara by remember { mutableStateOf(false) }

    // Launcher oficial de Jetpack Compose para solicitar permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        tienePermisoCamara = isGranted
    }

    // Al abrir la pantalla, se solicita el permiso de la cámara
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Reacciona al cambio de estado en el ViewModel cuando se completa el registro
    LaunchedEffect(estado) {
        val actual = estado
        if (actual is EscaneoState.Exito) {
            onCodigoEscaneado(actual.rondaId)
        }
    }

    // Respaldo: escribir el código si el QR está dañado o la cámara no lo lee
    var codigoManual by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Escaneo de Punto Frío",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (tienePermisoCamara) {
                Text(
                    text = "Apunta la cámara al código QR",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Visor de Cámara Real con CameraX y lector de QR ML Kit
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                // Configuración de ML Kit para escanear QR en tiempo real
                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()

                                val scanner = BarcodeScanning.getClient()

                                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                                    val mediaImage = imageProxy.image
                                    if (mediaImage != null) {
                                        val image = InputImage.fromMediaImage(
                                            mediaImage,
                                            imageProxy.imageInfo.rotationDegrees
                                        )
                                        scanner.process(image)
                                            .addOnSuccessListener { barcodes ->
                                                for (barcode in barcodes) {
                                                    val valorQr = barcode.rawValue
                                                    if (!valorQr.isNullOrEmpty()) {
                                                        // El QR trae punto_frio.codigo. El ViewModel ignora las
                                                        // lecturas repetidas y usa el usuario de la sesión.
                                                        // El GPS se sella en la inspección (actividad 17), no aquí.
                                                        viewModel.procesarQrEscaneado(valorQr)
                                                    }
                                                }
                                            }
                                            .addOnCompleteListener {
                                                imageProxy.close()
                                            }
                                    } else {
                                        imageProxy.close()
                                    }
                                }

                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageAnalysis
                                    )
                                } catch (exc: Exception) {
                                    Log.e(TAG, "Error al vincular cámara", exc)
                                }
                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                // Vista si el usuario aún no otorga el permiso
                Text(
                    text = "Se requiere permiso de la cámara para escanear los códigos QR.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Conceder Permiso")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (estado) {
                is EscaneoState.Cargando -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Guardando registro...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                is EscaneoState.Error -> {
                    Text(
                        text = (estado as EscaneoState.Error).mensaje,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Respaldo manual: el mismo código que trae el QR, escrito a mano
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = codigoManual,
                    onValueChange = { codigoManual = it },
                    label = { Text("¿No lo lee? Escribe el código") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.procesarQrEscaneado(codigoManual) },
                    enabled = codigoManual.isNotBlank() && estado !is EscaneoState.Cargando,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Abrir")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onVolverAlTablero,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al Tablero")
            }
        }
    }
}