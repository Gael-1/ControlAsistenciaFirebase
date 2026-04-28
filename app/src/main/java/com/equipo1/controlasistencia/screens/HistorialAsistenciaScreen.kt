package com.equipo1.controlasistencia.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.equipo1.controlasistencia.repository.AlumnoRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialAsistenciaScreen(
    matriculaAlumno: String,
    grupoId: String,
    nombreGrupo: String,
    onBack: () -> Unit
) {
    val repo = remember { AlumnoRepository() }
    var asistencias by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(grupoId) {
        repo.obtenerAsistenciasAlumno(matriculaAlumno, grupoId) { lista ->
            asistencias = lista
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Historial: $nombreGrupo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF2F2F7))
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (cargando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            else if (asistencias.isEmpty()) {
                Text("No hay registros de asistencia para esta materia.", modifier = Modifier.padding(32.dp), color = Color.Gray)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(asistencias) { asistencia ->
                        val fecha = asistencia["fecha"] as? String ?: "sin fecha"
                        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Presente", tint = Color(0xFF34C759))
                                Spacer(Modifier.width(16.dp))
                                Text(fecha, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.weight(1f))
                                Text("Presente", color = Color(0xFF34C759))
                            }
                        }
                    }
                }
            }
        }
    }
}