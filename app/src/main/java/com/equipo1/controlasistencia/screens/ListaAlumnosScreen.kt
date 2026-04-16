package com.equipo1.controlasistencia.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
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
import com.equipo1.controlasistencia.repository.AlumnoRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaAlumnosScreen(
    grupoId: String,
    nombreGrupo: String,
    esAdmin: Boolean,
    onTomarAsistencia: () -> Unit,
    onVerReportes: () -> Unit,
    onBack: () -> Unit
) {
    val alumnoRepository = remember { AlumnoRepository() }
    var alumnos by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var nombreAlumno by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }

    val appleGrayBackground = Color(0xFFF2F2F7)

    LaunchedEffect(grupoId) {
        cargando = true
        alumnoRepository.obtenerAlumnos(grupoId) { lista ->
            alumnos = lista
            cargando = false
        }
    }

    Scaffold(
        containerColor = appleGrayBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(nombreGrupo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Lista de Clase", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
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
                actions = {
                    IconButton(onClick = onVerReportes) {
                        Icon(Icons.Default.Assessment, contentDescription = "Reportes", tint = Color(0xFF007AFF))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = appleGrayBackground)
            )
        },
        floatingActionButton = {
            if (!esAdmin) {
                ExtendedFloatingActionButton(
                    onClick = onTomarAsistencia,
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                    text = { Text("Asistencia", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (esAdmin) {
                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF007AFF)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(2.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Inscribir Nuevo Alumno", fontWeight = FontWeight.SemiBold)
                }
            }

            if (cargando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), color = Color.Black)
            }

            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        "ESTUDIANTES (${alumnos.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                    )
                }

                items(alumnos) { alumno ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF2F2F7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = alumno.second,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false; errorMensaje = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (nombreAlumno.isNotBlank()) {
                            cargando = true
                            alumnoRepository.agregarAlumno(grupoId, nombreAlumno) { success, error ->
                                if (success) {
                                    alumnoRepository.obtenerAlumnos(grupoId) { alumnos = it }
                                    nombreAlumno = ""
                                    showDialog = false
                                } else {
                                    errorMensaje = error
                                }
                                cargando = false
                            }
                        }
                    }
                ) {
                    Text("Inscribir", fontWeight = FontWeight.Bold, color = Color(0xFF007AFF))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar", color = Color.Red) }
            },
            title = { Text("Nuevo Estudiante", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = nombreAlumno,
                    onValueChange = { nombreAlumno = it },
                    label = { Text("Nombre Completo") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMensaje != null
                )
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White
        )
    }
}