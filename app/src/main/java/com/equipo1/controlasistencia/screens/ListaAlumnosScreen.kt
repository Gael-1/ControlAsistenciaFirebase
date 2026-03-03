package com.equipo1.controlasistencia.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.equipo1.controlasistencia.repository.AlumnoRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaAlumnosScreen(
    grupoId: String,
    nombreGrupo: String,
    onTomarAsistencia: () -> Unit,
    onVerReportes: () -> Unit,
    onBack: () -> Unit
) {

    val alumnoRepository = remember { AlumnoRepository() }

    var alumnos by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var nombreAlumno by remember { mutableStateOf("") }

    // Cargar alumnos cuando cambia el grupo
    LaunchedEffect(grupoId) {
        alumnoRepository.obtenerAlumnos(grupoId) {
            alumnos = it
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(nombreGrupo) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onVerReportes) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Reportes"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onTomarAsistencia) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Tomar Asistencia"
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            Text(
                text = "Alumnos",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar Alumno")
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(alumnos) { alumno ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "• ${alumno.second}",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }

    // 🔥 Dialog para crear alumno
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (nombreAlumno.isNotBlank()) {
                            alumnoRepository.agregarAlumno(
                                grupoId,
                                nombreAlumno
                            ) { success, _ ->
                                if (success) {
                                    alumnoRepository.obtenerAlumnos(grupoId) {
                                        alumnos = it
                                    }
                                    nombreAlumno = ""
                                    showDialog = false
                                }
                            }
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancelar")
                }
            },
            title = { Text("Nuevo Alumno") },
            text = {
                OutlinedTextField(
                    value = nombreAlumno,
                    onValueChange = { nombreAlumno = it },
                    label = { Text("Nombre del alumno") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }
}