package com.equipo1.controlasistencia.screens

import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.equipo1.controlasistencia.repository.AsistenciaRepository
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(
    alumnoId: String,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val asistenciaRepository = remember { AsistenciaRepository() }

    // Control de estado para no escanear mil veces el mismo código
    var escaneadoCompletado by remember { mutableStateOf(false) }

    // Executor para el análisis de imagen (fuera del hilo principal)
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    // 🔥 IMPORTANTE: Limpiar la cámara al salir de la pantalla
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    val scanner = BarcodeScanning.getClient()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null && !escaneadoCompletado) {
                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )

                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        val rawValue = barcode.rawValue ?: ""
                                        if (rawValue.startsWith("asistencia")) {
                                            // Detenemos escaneos futuros
                                            escaneadoCompletado = true

                                            validarAsistencia(rawValue, alumnoId, asistenciaRepository) { success ->
                                                if (success) {
                                                    Toast.makeText(context, "¡Asistencia registrada!", Toast.LENGTH_LONG).show()
                                                    onSuccess()
                                                } else {
                                                    escaneadoCompletado = false
                                                    Toast.makeText(context, "Error: QR inválido", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                }
                                .addOnCompleteListener { imageProxy.close() }
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
                    } catch (e: Exception) {
                        Log.e("ScannerScreen", "Fallo al iniciar cámara", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // UI de apoyo sobre la cámara
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 48.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Apunta al código QR del profesor",
                color = androidx.compose.ui.graphics.Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Cancelar")
            }
        }
    }
}

fun validarAsistencia(
    datosQr: String,
    alumnoId: String,
    repository: AsistenciaRepository,
    onFinished: (Boolean) -> Unit
) {
    val partes = datosQr.split("|")
    if (partes.size < 4) {
        onFinished(false)
        return
    }

    val grupoId = partes[1]
    val fecha = partes[2]
    val tokenDelQr = partes[3]

    // Aquí registramos la asistencia.
    // Nota: El repositorio que creamos antes ya maneja el guardado en Firestore.
    repository.registrarAsistenciaAlumno(grupoId, fecha, alumnoId) { success, _ ->
        onFinished(success)
    }
}