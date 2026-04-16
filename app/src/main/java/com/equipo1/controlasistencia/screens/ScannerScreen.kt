package com.equipo1.controlasistencia.screens

import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.equipo1.controlasistencia.repository.AsistenciaRepository
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun ScannerScreen(
    alumnoId: String,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val asistenciaRepository = remember { AsistenciaRepository() }
    var escaneadoCompletado by remember { mutableStateOf(false) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // 1. Vista de la Cámara
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
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
                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        val rawValue = barcode.rawValue ?: ""
                                        if (rawValue.startsWith("asistencia")) {
                                            escaneadoCompletado = true
                                            validarAsistencia(rawValue, alumnoId, asistenciaRepository) { success ->
                                                if (success) {
                                                    onSuccess()
                                                } else {
                                                    escaneadoCompletado = false
                                                    Toast.makeText(context, "QR inválido", Toast.LENGTH_SHORT).show()
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

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
                    } catch (e: Exception) { Log.e("Scanner", "Error cam", e) }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Capa de diseño (El hueco del QR) corregido para evitar errores de tipo
        Canvas(modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = 0.99f) // Necesario para que el BlendMode.Clear funcione
        ) {
            val scanBoxSize = 280.dp.toPx()
            val offset = Offset((size.width - scanBoxSize) / 2, (size.height - scanBoxSize) / 2.5f)

            // Dibujar fondo oscuro
            drawRect(Color.Black.copy(alpha = 0.7f))

            // Dibujar el hueco redondeado (Usando la función de Compose pura)
            drawRoundRect(
                color = Color.Transparent,
                topLeft = offset,
                size = Size(scanBoxSize, scanBoxSize),
                cornerRadius = CornerRadius(30.dp.toPx()),
                blendMode = BlendMode.Clear
            )
        }

        // 3. UI y Controles
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Escanear QR", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                }
            }

            Surface(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text = "Apunta al código del profesor",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 14.sp
                )
            }
        }

        // Marco estético
        Box(
            modifier = Modifier
                .size(282.dp)
                .align(Alignment.Center)
                .offset(y = (-58).dp)
                .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(30.dp))
        )
    }
}

// Función validada para el nuevo repositorio
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
    val token = partes[3]

    repository.registrarAsistenciaAlumno(grupoId, fecha, alumnoId, token) { success, _ ->
        onFinished(success)
    }
}