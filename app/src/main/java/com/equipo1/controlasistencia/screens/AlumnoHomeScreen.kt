package com.equipo1.controlasistencia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo1.controlasistencia.repository.AlumnoRepository
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumnoHomeScreen(
    matriculaAlumno: String,
    nombreAlumno: String,
    onEscanearClick: (String, String) -> Unit,
    onHistorialClick: (String, String) -> Unit,
    onLogout: () -> Unit
) {
    val alumnoRepo = remember { AlumnoRepository() }
    var grupos by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    val appleGrayBackground = Color(0xFFF2F2F7)

    // Estado de asistencia del día para cada grupo
    var attendanceStatus by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

    // Cargar grupos
    LaunchedEffect(Unit) {
        alumnoRepo.obtenerGruposDelAlumno(matriculaAlumno) { lista ->
            grupos = lista
            cargando = false
        }
    }

    // Escuchar en tiempo real la asistencia de hoy para cada grupo
    DisposableEffect(grupos, matriculaAlumno) {
        val listeners = mutableListOf<ListenerRegistration>()
        grupos.forEach { (grupoId, _) ->
            val listener = alumnoRepo.escucharAsistenciaHoy(matriculaAlumno, grupoId) { presente ->
                attendanceStatus = attendanceStatus.toMutableMap().apply {
                    put(grupoId, presente)
                }
            }
            listeners.add(listener)
        }
        onDispose {
            listeners.forEach { it.remove() }
        }
    }

    Scaffold(
        containerColor = appleGrayBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MI ASISTENCIA", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp) },
                actions = {
                    IconButton(onClick = onLogout, modifier = Modifier.padding(8.dp).clip(CircleShape).background(Color.White)) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Cerrar Sesión", tint = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = appleGrayBackground)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(20.dp))
                Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Person, contentDescription = "Avatar", modifier = Modifier.size(50.dp), tint = Color(0xFF007AFF))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("¡Hola, $nombreAlumno!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tus materias inscritas", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(24.dp))
            if (cargando) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.Black) }
            } else if (grupos.isEmpty()) {
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), color = Color.White, shadowElevation = 2.dp) {
                    Text("No estás inscrito en ninguna materia.\nContacta al administrador.", modifier = Modifier.padding(24.dp), textAlign = TextAlign.Center, color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                    items(grupos) { (grupoId, nombreGrupo) ->
                        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = Color.White, shadowElevation = 2.dp) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(nombreGrupo, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    val presente = attendanceStatus[grupoId] ?: false
                                    if (presente) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = "Asistencia tomada", tint = Color(0xFF34C759), modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Tomada", color = Color(0xFF34C759), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        }
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Cancel, contentDescription = "Pendiente", tint = Color(0xFFFF3B30), modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Pendiente", color = Color(0xFFFF3B30), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(onClick = { onEscanearClick(grupoId, nombreGrupo) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Black), shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Filled.QrCodeScanner, contentDescription = "Escanear QR", modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Escanear QR")
                                    }
                                    Button(onClick = { onHistorialClick(grupoId, nombreGrupo) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)), shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Filled.History, contentDescription = "Historial", modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Historial")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}