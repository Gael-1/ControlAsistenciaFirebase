package com.equipo1.controlasistencia.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeProfesorScreen(
    nombreProfesor: String,
    onGrupoClick: (String) -> Unit
) {

    val grupoRepository = remember {
        com.equipo1.controlasistencia.repository.GrupoRepository()
    }

    var grupos by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var nuevoGrupo by remember { mutableStateOf("") }
    var mostrarDialogo by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        grupoRepository.obtenerGrupos {
            grupos = it
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Buenos días, $nombreProfesor",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { mostrarDialogo = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear Grupo")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Mis Grupos",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(grupos) { grupo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = { onGrupoClick(grupo.first) }
                ) {
                    Text(
                        text = grupo.second,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            confirmButton = {
                Button(
                    onClick = {
                        grupoRepository.crearGrupo(nuevoGrupo) { success, _ ->
                            if (success) {
                                grupoRepository.obtenerGrupos {
                                    grupos = it
                                }
                            }
                        }
                        mostrarDialogo = false
                        nuevoGrupo = ""
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Nuevo Grupo") },
            text = {
                OutlinedTextField(
                    value = nuevoGrupo,
                    onValueChange = { nuevoGrupo = it },
                    label = { Text("Nombre del grupo") }
                )
            }
        )
    }
}