package com.equipo1.controlasistencia.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo1.controlasistencia.repository.AsistenciaRepository
import com.equipo1.controlasistencia.util.generarQrBitmap
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TomarAsistenciaScreen(
    grupoId: String,
    nombreGrupo: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val asistenciaRepository = remember { AsistenciaRepository() }
    val appleGrayBackground = Color(0xFFF2F2F7)

    var tokenDinamico by remember { mutableStateOf(UUID.randomUUID().toString().substring(0, 6)) }
    val fechaHoy = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    fun actualizarTokenEnNube(nuevoToken: String) {
        asistenciaRepository.actualizarTokenAsistencia(grupoId, nuevoToken, fechaHoy) { success ->
            if (!success) {
                Toast.makeText(context, "Error de sincronización", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) { actualizarTokenEnNube(tokenDinamico) }

    val contenidoQr = "asistencia|$grupoId|$fechaHoy|$tokenDinamico"
    val qrBitmap = remember(tokenDinamico) { generarQrBitmap(contenidoQr, size = 1024) }

    Scaffold(
        containerColor = appleGrayBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(nombreGrupo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("CÓDIGO DE ASISTENCIA", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(8.dp).clip(CircleShape).background(Color.White)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = appleGrayBackground)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val nuevoToken = UUID.randomUUID().toString().substring(0, 6)
                    tokenDinamico = nuevoToken
                    actualizarTokenEnNube(nuevoToken)
                },
                containerColor = Color.Black,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                text = { Text("Renovar QR", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Tarjeta del QR Estilo Apple
            Surface(
                modifier = Modifier
                    .size(340.dp)
                    .padding(8.dp),
                shape = RoundedCornerShape(40.dp), // Esquinas extra redondeadas
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier
                                .size(280.dp)
                                .padding(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Información del Token
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.wrapContentWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOKEN:",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = tokenDinamico,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF007AFF) // Azul Apple
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Pide a tus alumnos escanear el código\npara registrar su entrada hoy.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.DarkGray,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Footer informativo
            Text(
                text = "FECHA DE SESIÓN: $fechaHoy",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray,
                letterSpacing = 1.sp
            )
        }
    }
}