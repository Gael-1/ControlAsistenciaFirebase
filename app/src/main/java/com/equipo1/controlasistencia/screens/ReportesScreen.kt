package com.equipo1.controlasistencia.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesScreen(
    nombreGrupo: String,
    onBack: () -> Unit,
    onGuardarReporte: () -> Unit
) {
    // estos son de ejemplo los vamos a cambiar cuando tengamos la base de datos
    val reportes = remember {
        listOf(
            "Juan Pérez" to 90,
            "Ana López" to 85,
            "Luis Torres" to 100,
            "María Gómez" to 75,
            "Pedro Ruiz" to 95
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reportes - $nombreGrupo",
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
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
            Text(
                text = "Porcentaje de Asistencias",
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))


            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reportes) { (alumno, porcentaje) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = alumno,
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f)
                            )


                            Surface(      //esto modiifica el colo segun su asistencia
                                color = when {
                                    porcentaje >= 90 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    porcentaje >= 80 -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                    else -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                },
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    text = "$porcentaje%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        porcentaje >= 90 -> MaterialTheme.colorScheme.primary
                                        porcentaje >= 80 -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.error
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onGuardarReporte,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Guardar Reporte",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}