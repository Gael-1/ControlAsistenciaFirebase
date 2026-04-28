package com.equipo1.controlasistencia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.equipo1.controlasistencia.repository.AlumnoRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionAlumnosGrupoScreen(
    grupoId: String,
    nombreGrupo: String,
    onBack: () -> Unit
) {
    val alumnoRepo = remember { AlumnoRepository() }
    val db = FirebaseFirestore.getInstance()
    var alumnos by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var matriculaInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var listenerReg by remember { mutableStateOf<ListenerRegistration?>(null) }

    // Usar escucha en tiempo real para actualizar automáticamente
    LaunchedEffect(grupoId) {
        listenerReg = alumnoRepo.escucharAlumnos(grupoId) { lista ->
            alumnos = lista
            cargando = false
        }
    }
    DisposableEffect(grupoId) {
        onDispose { listenerReg?.remove() }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Gestionar $nombreGrupo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.clip(CircleShape).background(Color.White)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF2F2F7))
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = Color.Black) {
                Icon(Icons.Default.Add, contentDescription = "Agregar", tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (cargando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("ALUMNOS INSCRITOS (${alumnos.size})", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                items(alumnos) { (matricula, nombre) ->
                    Surface(shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF2F2F7)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.DarkGray)
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(nombre, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.weight(1f))
                            Text(matricula, color = Color.Gray, fontSize = MaterialTheme.typography.bodySmall.fontSize)
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false; errorMsg = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (matriculaInput.isNotBlank()) {
                            val matriculaNum = matriculaInput.toLongOrNull()
                            if (matriculaNum == null) {
                                errorMsg = "Matrícula inválida"
                                return@TextButton
                            }
                            db.collection("usuarios")
                                .whereEqualTo("matricula", matriculaNum)
                                .get()
                                .addOnSuccessListener { snapshot ->
                                    if (snapshot.isEmpty) {
                                        errorMsg = "Matrícula no registrada en el sistema"
                                        return@addOnSuccessListener
                                    }
                                    val nombre = snapshot.documents[0].getString("nombre") ?: "Alumno"
                                    alumnoRepo.agregarAlumnoAGrupo(grupoId, matriculaInput, nombre) { success, error ->
                                        if (success) {
                                            // La lista se actualizará sola gracias al listener
                                            showDialog = false
                                            matriculaInput = ""
                                            errorMsg = null
                                        } else {
                                            errorMsg = error
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    errorMsg = "Error de conexión: ${e.message}"
                                }
                        } else {
                            errorMsg = "Ingresa la matrícula"
                        }
                    }
                ) {
                    Text("Inscribir", color = Color(0xFF007AFF))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar", color = Color.Red)
                }
            },
            title = { Text("Inscribir alumno") },
            text = {
                OutlinedTextField(
                    value = matriculaInput,
                    onValueChange = { matriculaInput = it; errorMsg = null },
                    label = { Text("Matrícula del alumno") },
                    isError = errorMsg != null,
                    supportingText = { errorMsg?.let { Text(it, color = Color.Red) } },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}