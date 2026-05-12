package com.equipo1.controlasistencia.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.equipo1.controlasistencia.repository.AlumnoRepository
import com.equipo1.controlasistencia.repository.AsistenciaRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialAsistenciaScreen(
    matriculaAlumno: String,
    grupoId: String,
    nombreGrupo: String,
    onBack: () -> Unit
) {
    val alumnoRepo = remember { AlumnoRepository() }
    val asistenciaRepo = remember { AsistenciaRepository() }

    var historial by remember { mutableStateOf<List<Pair<String, Boolean>>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(grupoId, matriculaAlumno) {
        // Obtener todas las fechas únicas en las que se ha tomado asistencia en este grupo
        asistenciaRepo.obtenerFechasAsistenciaGrupo(grupoId) { fechas ->
            // Obtener las asistencias del alumno en este grupo
            alumnoRepo.obtenerAsistenciasAlumno(matriculaAlumno, grupoId) { asistencias ->
                // Mapa de fecha -> presente (true)
                val asistenciasMap = asistencias.associate {
                    (it["fecha"] as? String) to true
                }.filterKeys { it != null } as Map<String, Boolean>

                // Construir lista completa: cada fecha con su estado (false si no está en el mapa)
                val lista = fechas.map { fecha ->
                    fecha to (asistenciasMap[fecha] == true)
                }
                historial = lista
                cargando = false
            }
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
            if (cargando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else if (historial.isEmpty()) {
                Text("No hay registros de asistencia para esta materia.", modifier = Modifier.padding(32.dp), color = Color.Gray)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(historial) { (fecha, presente) ->
                        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    if (presente) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Presente", tint = Color(0xFF34C759))
                                    } else {
                                        Icon(Icons.Default.Cancel, contentDescription = "Falta", tint = Color(0xFFFF3B30))
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Text(fecha, fontWeight = FontWeight.Medium)
                                }
                                Text(
                                    text = if (presente) "Presente" else "Falta",
                                    color = if (presente) Color(0xFF34C759) else Color(0xFFFF3B30),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}