package com.equipo1.controlasistencia.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo1.controlasistencia.repository.GrupoRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeProfesorScreen(
    profesorMatricula: String,   // matrícula del profesor que inició sesión
    nombreProfesor: String,
    onGrupoClick: (String, String) -> Unit,
    onBack: () -> Unit
) {
    val grupoRepo = remember { GrupoRepository() }
    var grupos by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    val appleGrayBackground = Color(0xFFF2F2F7)

    LaunchedEffect(profesorMatricula) {
        Log.d("HomeProfesor", "Cargando grupos para profesor matrícula: $profesorMatricula")
        grupoRepo.obtenerGruposPorProfesor(profesorMatricula) { lista ->
            Log.d("HomeProfesor", "Grupos recibidos: ${lista.size}")
            grupos = lista
            cargando = false
        }
    }

    Scaffold(
        containerColor = appleGrayBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("PANEL DOCENTE", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.clip(CircleShape).background(Color.White)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = appleGrayBackground)
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Surface(modifier = Modifier.fillMaxWidth(), color = Color.Black, shape = RoundedCornerShape(28.dp)) {
                    Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Hola,", color = Color.White.copy(alpha = 0.7f))
                            Text(nombreProfesor, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            item { Text("Tus Clases", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp)) }
            if (cargando) {
                item { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.Black) } }
            } else if (grupos.isEmpty()) {
                item { Text("No tienes materias asignadas.", modifier = Modifier.fillMaxWidth().padding(32.dp), color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
            } else {
                items(grupos) { (id, nombre) ->
                    Surface(onClick = { onGrupoClick(id, nombre) }, shape = RoundedCornerShape(24.dp), color = Color.White, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFE9F0FF)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Book, contentDescription = null, tint = Color(0xFF007AFF), modifier = Modifier.size(24.dp))
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(nombre, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Text("Administrar clase", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFC7C7CC))
                        }
                    }
                }
            }
        }
    }
}