package com.equipo1.controlasistencia.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeProfesorScreen(
    nombreProfesor: String,
    onGrupoClick: (String, String) -> Unit,
    onBack: () -> Unit // ✅ Agregado para el botón de regreso
) {
    val grupoRepository = remember { com.equipo1.controlasistencia.repository.GrupoRepository() }

    var grupos by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var nuevoGrupo by remember { mutableStateOf("") }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(true) }
    var guardando by remember { mutableStateOf(false) } // Nuevo estado para no duplicar clics

    // Escuchar grupos en tiempo real
    LaunchedEffect(Unit) {
        grupoRepository.escucharGrupos { lista ->
            grupos = lista
            cargando = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asistencia") },
                navigationIcon = {
                    IconButton(onClick = onBack) { // ✅ Botón de regreso funcional
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(text = "Buenos días, $nombreProfesor", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { mostrarDialogo = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Crear Nuevo Grupo")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Mis Grupos", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            if (cargando) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (grupos.isEmpty()) {
                Text(
                    text = "Aún no tienes grupos. ¡Crea el primero!",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                // ✅ Agregué la lista para que los grupos se vean
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(grupos) { grupo ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onGrupoClick(grupo.first, grupo.second) },
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Text(
                                text = grupo.second,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo para Crear Grupo
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { if (!guardando) mostrarDialogo = false },
            confirmButton = {
                Button(
                    enabled = !guardando,
                    onClick = {
                        if (nuevoGrupo.isNotBlank()) {
                            guardando = true
                            grupoRepository.crearGrupo(nuevoGrupo) { success, _ ->
                                guardando = false
                                if (success) {
                                    nuevoGrupo = ""
                                    mostrarDialogo = false
                                }
                            }
                        }
                    }
                ) {
                    Text(if (guardando) "Guardando..." else "Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") }
            },
            title = { Text("Nuevo Grupo") },
            text = {
                OutlinedTextField(
                    value = nuevoGrupo,
                    onValueChange = { nuevoGrupo = it },
                    label = { Text("Nombre del grupo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }

    // Diálogo de Carga Inicial (Seguro de conexión)
    if (cargando) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = { cargando = false }) {
                    Text("Cancelar espera")
                }
            },
            title = { Text("Conectando con Firestore") },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(30.dp))
                    Spacer(modifier = Modifier.width(15.dp))
                    Text("Esto puede tardar según tu conexión...")
                }
            }
        )
    }
}