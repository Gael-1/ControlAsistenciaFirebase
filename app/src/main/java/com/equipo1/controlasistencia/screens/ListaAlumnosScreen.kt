package com.equipo1.controlasistencia.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment // Icono más apropiado para reportes
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
    var cargando by remember { mutableStateOf(false) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }

    // Cargar alumnos al iniciar
    LaunchedEffect(grupoId) {
        cargando = true
        alumnoRepository.obtenerAlumnos(grupoId) { lista ->
            alumnos = lista
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(nombreGrupo) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = onVerReportes) {
                        Icon(Icons.Default.Assessment, contentDescription = "Ver Reportes")
                    }
                }
            )
        },
        floatingActionButton = {
            // Cambiamos a un FAB extendido para que sea más claro
            ExtendedFloatingActionButton(
                onClick = onTomarAsistencia,
                icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                text = { Text("Asistencia") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(text = "Lista de Alumnos", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Inscribir Nuevo Alumno")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (cargando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (alumnos.isEmpty() && !cargando) {
                Text(
                    text = "No hay alumnos registrados en este grupo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(alumnos) { alumno ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = alumno.second,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }

    // Dialog para crear alumno con detección de errores
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                errorMensaje = null
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nombreAlumno.isNotBlank()) {
                            cargando = true
                            alumnoRepository.agregarAlumno(grupoId, nombreAlumno) { success, error ->
                                if (success) {
                                    // Recargar lista tras guardar
                                    alumnoRepository.obtenerAlumnos(grupoId) { alumnos = it }
                                    nombreAlumno = ""
                                    showDialog = false
                                    errorMensaje = null
                                } else {
                                    errorMensaje = error ?: "Error al conectar con Firebase"
                                    Log.e("FirebaseError", "Error: $error")
                                }
                                cargando = false
                            }
                        }
                    },
                    enabled = !cargando
                ) {
                    Text(if (cargando) "Guardando..." else "Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            },
            title = { Text("Registrar Alumno") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nombreAlumno,
                        onValueChange = { nombreAlumno = it },
                        label = { Text("Nombre Completo") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorMensaje != null
                    )
                    if (errorMensaje != null) {
                        Text(
                            text = errorMensaje!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        )
    }
}