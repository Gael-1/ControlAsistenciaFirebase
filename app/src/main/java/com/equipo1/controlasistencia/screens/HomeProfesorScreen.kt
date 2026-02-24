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
    onGrupoClick: (String) -> Unit,
    onCreateGrupo: () -> Unit
) {
    // datoos de prueba se van a cambiar cuando ya tengamos base de datos, recuerden modificarlo
    val grupos = remember {
        mutableStateListOf(
            "Matemáticas 3A",
            "Física 2B",
            "Programación 1C"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "buenos dias, $nombreProfesor",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCreateGrupo,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Crear"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Crear Grupo")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Mis Grupos",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(grupos) { grupo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = { onGrupoClick(grupo) }
                ) {
                    Text(
                        text = "• $grupo",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}