package com.equipo1.controlasistencia.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

    // 1. Estado para el Token dinámico
    var tokenDinamico by remember { mutableStateOf(UUID.randomUUID().toString().substring(0, 6)) }

    // 2. Obtener la fecha actual
    val fechaHoy = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // 3. Función para sincronizar el token con Firebase
    fun actualizarTokenEnNube(nuevoToken: String) {
        asistenciaRepository.actualizarTokenAsistencia(grupoId, nuevoToken, fechaHoy) { success ->
            if (!success) {
                Toast.makeText(context, "Error al sincronizar con la nube", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 4. Registrar el primer token al entrar a la pantalla
    LaunchedEffect(Unit) {
        actualizarTokenEnNube(tokenDinamico)
    }

    // 5. Contenido que leerá el alumno
    val contenidoQr = "asistencia|$grupoId|$fechaHoy|$tokenDinamico"

    // 6. Generar el Bitmap del QR
    val qrBitmap = remember(tokenDinamico) {
        generarQrBitmap(contenidoQr, size = 1024)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(nombreGrupo, style = MaterialTheme.typography.titleMedium)
                        Text("QR de Asistencia", style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val nuevoToken = UUID.randomUUID().toString().substring(0, 6)
                    tokenDinamico = nuevoToken
                    actualizarTokenEnNube(nuevoToken) // Sincroniza el nuevo QR
                },
                icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                text = { Text("Nuevo QR") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.size(320.dp)
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Los alumnos pueden escanear este QR para registrar su asistencia",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Token Activo: $tokenDinamico",
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Fecha: $fechaHoy",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Al presionar 'Nuevo QR' el código anterior dejará de funcionar inmediatamente.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}