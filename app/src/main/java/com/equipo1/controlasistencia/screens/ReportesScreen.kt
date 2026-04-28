package com.equipo1.controlasistencia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo1.controlasistencia.repository.AsistenciaRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesScreen(
    grupoId: String,
    nombreGrupo: String,
    onBack: () -> Unit,
    onGuardarReporte: () -> Unit
) {
    val repo = remember { AsistenciaRepository() }
    var reportes by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(grupoId) {
        repo.obtenerReporteGrupo(grupoId) { lista ->
            reportes = lista
            cargando = false
        }
    }

    Scaffold(
        containerColor = Color(0xFFF2F2F7),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(nombreGrupo, fontWeight = FontWeight.Bold)
                        Text("ESTADÍSTICAS", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.clip(CircleShape).background(Color.White)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF2F2F7))
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(20.dp))
            Text("Rendimiento de Asistencia", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text("Resumen por alumno", color = Color.Gray)
            Spacer(Modifier.height(24.dp))

            if (cargando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            else LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(reportes) { (alumno, porcentaje) ->
                    Surface(shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF2F2F7)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(20.dp), tint = Color.Gray)
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(alumno, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            val colorStatus = when { porcentaje >= 90 -> Color(0xFF34C759); porcentaje >= 80 -> Color(0xFFFF9500); else -> Color(0xFFFF3B30) }
                            Surface(color = colorStatus.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
                                Text("$porcentaje%", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = colorStatus, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }

            Button(onClick = onGuardarReporte, modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp).height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Black), shape = RoundedCornerShape(18.dp)) {
                Icon(Icons.Default.Download, null)
                Spacer(Modifier.width(8.dp))
                Text("Exportar Reporte", fontWeight = FontWeight.Bold)
            }
        }
    }
}