package com.equipo1.controlasistencia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
    var fechasSesion by remember { mutableStateOf<List<String>>(emptyList()) }
    var fechaSeleccionada by remember { mutableStateOf("") }
    var presentes by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var ausentes by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var cargandoFechas by remember { mutableStateOf(true) }
    var cargandoListas by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(grupoId) {
        repo.obtenerFechasSesion(grupoId) { fechas ->
            fechasSesion = fechas.sortedDescending()
            cargandoFechas = false
        }
    }

    Scaffold(
        containerColor = Color(0xFFF2F2F7),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(nombreGrupo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("REPORTE DE CLASE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    // Para evitar usar background, usamos Surface
                    Surface(
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        color = Color.White,
                        onClick = { onBack() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.Black)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF2F2F7))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Asistencia por fecha",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Selecciona una fecha para ver quiénes asistieron o faltaron",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (cargandoFechas) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else if (fechasSesion.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White
                ) {
                    Text(
                        "No hay sesiones registradas todavía.\nEl profesor debe generar el código QR para empezar.",
                        modifier = Modifier.padding(24.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = Color.Gray
                    )
                }
            } else {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = fechaSeleccionada.ifEmpty { "Seleccionar fecha" },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        fechasSesion.forEach { fecha ->
                            DropdownMenuItem(
                                text = { Text(fecha) },
                                onClick = {
                                    fechaSeleccionada = fecha
                                    expanded = false
                                    cargandoListas = true
                                    repo.obtenerAlumnosPresentesEnFecha(grupoId, fecha) { listaPresentes ->
                                        presentes = listaPresentes
                                        repo.obtenerAlumnosAusentesEnFecha(grupoId, fecha) { listaAusentes ->
                                            ausentes = listaAusentes
                                            cargandoListas = false
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (fechaSeleccionada.isNotEmpty()) {
                    if (cargandoListas) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        TabRow(selectedTabIndex = selectedTab) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { Text("Asistencias (${presentes.size})") }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = { Text("Faltas (${ausentes.size})") }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        when (selectedTab) {
                            0 -> {
                                if (presentes.isEmpty()) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFF34C759).copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            "✅ No hubo asistencias registradas en esta fecha.",
                                            modifier = Modifier.padding(16.dp),
                                            color = Color(0xFF34C759)
                                        )
                                    }
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(presentes) { (matricula, nombre) ->
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = Color.White,
                                                shadowElevation = 1.dp,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(16.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.Person, contentDescription = "Alumno", tint = Color(0xFF34C759))
                                                    Spacer(Modifier.width(12.dp))
                                                    Text("$nombre ($matricula)", fontWeight = FontWeight.Medium)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            1 -> {
                                if (ausentes.isEmpty()) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFF34C759).copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            "✅ Todos los alumnos asistieron.",
                                            modifier = Modifier.padding(16.dp),
                                            color = Color(0xFF34C759)
                                        )
                                    }
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(ausentes) { (matricula, nombre) ->
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = Color.White,
                                                shadowElevation = 1.dp,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(16.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.Person, contentDescription = "Alumno", tint = Color(0xFFFF3B30))
                                                    Spacer(Modifier.width(12.dp))
                                                    Text("$nombre ($matricula)", fontWeight = FontWeight.Medium)
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

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onGuardarReporte,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Exportar Reporte (próximamente)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}