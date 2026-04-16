package com.equipo1.controlasistencia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeEscolarScreen(
    nombreAdmin: String,
    onLogout: () -> Unit,
    onVerDetalleGrupo: (String, String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    var grupos by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }

    // Estados para el formulario de nueva materia
    var nombreMateria by remember { mutableStateOf("") }
    var numeroSocioProfesor by remember { mutableStateOf("") } // Cambio de UID a Número de Socio

    val appleGrayBackground = Color(0xFFF2F2F7)

    // Escuchar todos los grupos del sistema en tiempo real
    LaunchedEffect(Unit) {
        db.collection("grupos")
            .orderBy("nombre") // Para que siempre aparezcan ordenados alfabéticamente
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    grupos = snapshot.documents.map { it.id to (it.getString("nombre") ?: "Sin nombre") }
                }
            }
    }

    Scaffold(
        containerColor = appleGrayBackground,
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text("SISTEMA ESCOLAR", style = MaterialTheme.typography.labelMedium, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("Administración", fontWeight = FontWeight.ExtraBold)
                    }
                },
                actions = {
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.padding(8.dp).clip(CircleShape).background(Color.White)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Salir", tint = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = appleGrayBackground)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                containerColor = Color.Black,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Asignar Materia", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "MATERIAS REGISTRADAS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                    letterSpacing = 1.sp
                )
            }

            if (grupos.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                        color = Color.Transparent
                    ) {
                        Text(
                            "No hay materias activas.\nPresiona + para organizar el ciclo.",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = Color.LightGray,
                            lineHeight = 20.sp
                        )
                    }
                }
            } else {
                items(grupos) { grupo ->
                    Surface(
                        onClick = { onVerDetalleGrupo(grupo.first, grupo.second) },
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF2F2F7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.School, contentDescription = null, tint = Color.Black)
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(grupo.second, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), fontSize = 17.sp)
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFC7C7CC))
                        }
                    }
                }
            }
        }
    }

    // Diálogo estilo iOS para crear materia
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (nombreMateria.isNotBlank() && numeroSocioProfesor.isNotBlank()) {
                            val nuevoGrupo = hashMapOf(
                                "nombre" to nombreMateria,
                                "profesorSocio" to numeroSocioProfesor, // Usamos el número de socio
                                "codigoQr" to UUID.randomUUID().toString().take(6).uppercase()
                            )
                            db.collection("grupos").add(nuevoGrupo)
                            showDialog = false
                            nombreMateria = ""
                            numeroSocioProfesor = ""
                        }
                    }
                ) {
                    Text("Asignar", fontWeight = FontWeight.Bold, color = Color(0xFF007AFF))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar", color = Color.Red)
                }
            },
            title = { Text("Nueva Asignación", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Vincula una materia con un profesor mediante su número de socio.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(Modifier.height(20.dp))

                    OutlinedTextField(
                        value = nombreMateria,
                        onValueChange = { nombreMateria = it },
                        label = { Text("Nombre de la Materia") },
                        placeholder = { Text("Ej. Cálculo Integral") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = numeroSocioProfesor,
                        onValueChange = { numeroSocioProfesor = it },
                        label = { Text("Número de Socio") },
                        placeholder = { Text("Ej. 12345") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White
        )
    }
}