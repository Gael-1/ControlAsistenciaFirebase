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
    var tokenDinamico by remember { mutableStateOf(UUID.randomUUID().toString().substring(0, 6)) }
    val fechaHoy = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    fun actualizarToken(nuevoToken: String) {
        asistenciaRepository.actualizarTokenAsistencia(grupoId, nuevoToken, fechaHoy) { success ->
            if (!success) Toast.makeText(context, "Error al guardar QR", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) { actualizarToken(tokenDinamico) }

    val contenidoQr = "asistencia|$grupoId|$fechaHoy|$tokenDinamico"
    val qrBitmap = remember(tokenDinamico) { generarQrBitmap(contenidoQr, size = 1024) }

    Scaffold(
        containerColor = Color(0xFFF2F2F7),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(nombreGrupo, fontWeight = FontWeight.Bold)
                        Text("CÓDIGO DE ASISTENCIA", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.clip(CircleShape).background(Color.White)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF2F2F7))
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val nuevoToken = UUID.randomUUID().toString().substring(0, 6)
                    tokenDinamico = nuevoToken
                    actualizarToken(nuevoToken)
                },
                containerColor = Color.Black,
                shape = RoundedCornerShape(20.dp),
                icon = { Icon(Icons.Default.Refresh, null) },
                text = { Text("Renovar QR", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Surface(modifier = Modifier.size(340.dp), shape = RoundedCornerShape(40.dp), color = Color.White, shadowElevation = 4.dp) {
                Box(contentAlignment = Alignment.Center) {
                    qrBitmap?.let { Image(bitmap = it.asImageBitmap(), contentDescription = "QR", modifier = Modifier.size(280.dp)) }
                }
            }
            Spacer(Modifier.height(40.dp))
            Surface(color = Color.White, shape = RoundedCornerShape(16.dp)) {
                Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("TOKEN:", color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Text(tokenDinamico, fontWeight = FontWeight.ExtraBold, color = Color(0xFF007AFF))
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Pide a tus alumnos escanear el código", textAlign = TextAlign.Center, color = Color.DarkGray)
            Spacer(Modifier.height(32.dp))
            Text("FECHA: $fechaHoy", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
        }
    }
}