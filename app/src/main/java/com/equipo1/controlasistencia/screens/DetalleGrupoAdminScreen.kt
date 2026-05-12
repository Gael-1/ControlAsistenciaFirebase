package com.equipo1.controlasistencia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.equipo1.controlasistencia.repository.AlumnoRepository
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleGrupoAdminScreen(
    grupoId: String,
    nombreGrupo: String,
    onBack: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val alumnoRepo = remember { AlumnoRepository() }
    var alumnos by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var matriculaInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var agregando by remember { mutableStateOf(false) }

    // Cargar alumnos
    LaunchedEffect(grupoId) {
        alumnoRepo.obtenerAlumnos(grupoId) { lista ->
            alumnos = lista
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(nombreGrupo, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF2F2F7))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Alumno", tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (cargando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "ALUMNOS INSCRITOS (${alumnos.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    items(alumnos) { (matricula, nombre) ->
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
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF2F2F7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, null, tint = Color.DarkGray)
                                }
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(nombre, fontWeight = FontWeight.Medium)
                                    Text("Matrícula: $matricula", fontSize = MaterialTheme.typography.bodySmall.fontSize, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo para agregar alumno
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                errorMsg = null
                matriculaInput = ""
            },
            title = { Text("Inscribir alumno") },
            text = {
                Column {
                    Text(
                        "Ingresa la matrícula del alumno para inscribirlo en esta materia.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = matriculaInput,
                        onValueChange = {
                            matriculaInput = it
                            errorMsg = null
                        },
                        label = { Text("Matrícula del alumno") },
                        placeholder = { Text("Ej. 2022477") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = errorMsg != null,
                        supportingText = { errorMsg?.let { Text(it, color = Color.Red) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (matriculaInput.isBlank()) {
                            errorMsg = "Ingresa una matrícula"
                            return@Button
                        }
                        val matriculaNum = matriculaInput.toLongOrNull()
                        if (matriculaNum == null) {
                            errorMsg = "Matrícula inválida"
                            return@Button
                        }
                        agregando = true
                        db.collection("usuarios")
                            .whereEqualTo("matricula", matriculaNum)
                            .whereEqualTo("rol", "alumno")
                            .get()
                            .addOnSuccessListener { snapshot ->
                                if (snapshot.isEmpty) {
                                    errorMsg = "Matrícula no registrada como alumno"
                                    agregando = false
                                    return@addOnSuccessListener
                                }
                                val nombreAlumno = snapshot.documents[0].getString("nombre") ?: "Alumno"
                                alumnoRepo.agregarAlumnoAGrupo(grupoId, matriculaInput, nombreAlumno) { success, msg ->
                                    agregando = false
                                    if (success) {
                                        showDialog = false
                                        matriculaInput = ""
                                        errorMsg = null
                                        // Refrescar lista
                                        alumnoRepo.obtenerAlumnos(grupoId) { nuevaLista ->
                                            alumnos = nuevaLista
                                        }
                                    } else {
                                        errorMsg = msg ?: "Error al inscribir"
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                agregando = false
                                errorMsg = "Error de conexión: ${e.message}"
                            }
                    },
                    enabled = !agregando,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                ) {
                    if (agregando) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text("Inscribir")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false; errorMsg = null }) {
                    Text("Cancelar", color = Color.Red)
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}