package com.equipo1.controlasistencia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaAlumnosScreen(
    grupoId: String,
    nombreGrupo: String,
    esAdmin: Boolean, // false para profesor (solo lectura)
    onBack: () -> Unit
) {
    val alumnoRepo = remember { AlumnoRepository() }
    var alumnos by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

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
                    IconButton(onClick = onBack, modifier = Modifier.clip(CircleShape).background(Color.White)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF2F2F7))
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (cargando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { Text("ESTUDIANTES (${alumnos.size})", style = MaterialTheme.typography.labelSmall, color = Color.Gray) }
                items(alumnos) { (matricula, nombre) ->
                    Surface(shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF2F2F7)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, null, tint = Color.DarkGray, modifier = Modifier.size(20.dp))
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