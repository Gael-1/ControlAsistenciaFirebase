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
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaAlumnosScreen(
    nombreGrupo: String,
    onTomarAsistencia: () -> Unit,
    onVerReportes: () -> Unit,
    onBack: () -> Unit
) {
    val alumnos = listOf(
        "Juan Pérez",
        "Ana López",
        "Luis Torres",
        "María Gómez",
        "Pedro Ruiz"
    )

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
                        Icon(Icons.Default.Star, contentDescription = "Reportes")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onTomarAsistencia) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Tomar Asistencia")
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

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(alumnos) { alumno ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "• $alumno",
                            modifier = Modifier.padding(16.dp),
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}