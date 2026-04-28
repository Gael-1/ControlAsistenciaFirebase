package com.equipo1.controlasistencia.screens

import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import java.util.concurrent.Executors

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun ScannerScreen(
    matriculaAlumno: String,
    grupoIdEsperado: String? = null, // opcional, para validar
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val asistenciaRepository = remember { AsistenciaRepository() }
    var escaneadoCompletado by remember { mutableStateOf(false) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
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
                                        if (rawValue.startsWith("asistencia|")) {
                                            escaneadoCompletado = true
                                            val partes = rawValue.split("|")
                                            if (partes.size >= 4) {
                                                val grupoIdQr = partes[1]
                                                val fecha = partes[2]
                                                val token = partes[3]

                                                if (grupoIdEsperado != null && grupoIdQr != grupoIdEsperado) {
                                                    Toast.makeText(context, "QR no corresponde a tu materia", Toast.LENGTH_SHORT).show()
                                                    escaneadoCompletado = false
                                                    imageProxy.close()
                                                    return@addOnSuccessListener
                                                }

                                                asistenciaRepository.registrarAsistenciaAlumno(
                                                    grupoIdQr, fecha, matriculaAlumno, token
                                                ) { success, msg ->
                                                    if (success) {
                                                        Toast.makeText(context, "Asistencia registrada", Toast.LENGTH_SHORT).show()
                                                        onSuccess()
                                                    } else {
                                                        Toast.makeText(context, msg ?: "Error", Toast.LENGTH_SHORT).show()
                                                        escaneadoCompletado = false
                                                    }
                                                }
                                            } else {
                                                Toast.makeText(context, "QR inválido", Toast.LENGTH_SHORT).show()
                                                escaneadoCompletado = false
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

        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.99f)) {
            val scanBoxSize = 280.dp.toPx()
            val offset = Offset((size.width - scanBoxSize) / 2, (size.height - scanBoxSize) / 2.5f)
            drawRect(Color.Black.copy(alpha = 0.7f))
            drawRoundRect(
                color = Color.Transparent,
                topLeft = offset,
                size = Size(scanBoxSize, scanBoxSize),
                cornerRadius = CornerRadius(30.dp.toPx()),
                blendMode = BlendMode.Clear
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Escanear QR", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onBack, modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                }
            }
            Surface(color = Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(16.dp)) {
                Text("Apunta al código del profesor", color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
        }

        Box(modifier = Modifier.size(282.dp).align(Alignment.Center).offset(y = (-58).dp).border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(30.dp)))
    }
}