package com.equipo1.controlasistencia.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.equipo1.controlasistencia.repository.AlumnoRepository
import com.equipo1.controlasistencia.repository.AsistenciaRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaAsistenciaDiaProfesorScreen(
    grupoId: String,
    nombreGrupo: String,
    fechaSesion: String? = "",
    token: String? = "",
    onBack: () -> Unit
) {

    val context = LocalContext.current

    val alumnoRepo = remember { AlumnoRepository() }
    val asistenciaRepo = remember { AsistenciaRepository() }

    // =========================
    // ESTADOS
    // =========================

    var fechasDisponibles by remember {
        mutableStateOf<List<String>>(emptyList())
    }

    var fechaSeleccionada by remember {
        mutableStateOf(fechaSesion ?: "")
    }

    var tokenSeleccionado by remember {
        mutableStateOf(token ?: "")
    }

    var cargandoFechas by remember {
        mutableStateOf(true)
    }

    var alumnos by remember {
        mutableStateOf<List<Pair<String, String>>>(emptyList())
    }

    var asistenciasRegistradas by remember {
        mutableStateOf<Set<String>>(emptySet())
    }

    var cargandoLista by remember {
        mutableStateOf(true)
    }

    var mostrarDialogoFechas by remember {
        mutableStateOf(false)
    }

    // =========================
    // CARGAR FECHAS
    // =========================

    LaunchedEffect(grupoId) {

        cargandoFechas = true

        asistenciaRepo.obtenerFechasAsistenciaGrupo(grupoId) { fechas ->

            fechasDisponibles = fechas

            // Si nunca se generó QR
            if (fechas.isEmpty()) {

                fechaSeleccionada = ""
                tokenSeleccionado = ""

                alumnos = emptyList()
                asistenciasRegistradas = emptySet()

                cargandoLista = false

            } else {

                // Usar la fecha más reciente
                fechaSeleccionada = fechas.first()
            }

            cargandoFechas = false
        }
    }

    // =========================
    // MENSAJE SI NO HAY SESIONES
    // =========================

    if (!cargandoFechas && fechasDisponibles.isEmpty()) {

        Scaffold(

            topBar = {

                CenterAlignedTopAppBar(

                    title = {
                        Text(
                            "Asistencia - $nombreGrupo",
                            fontWeight = FontWeight.Bold
                        )
                    },

                    navigationIcon = {

                        IconButton(
                            onClick = onBack
                        ) {

                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null
                            )
                        }
                    }
                )
            }

        ) { padding ->

            Box(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),

                contentAlignment = Alignment.Center

            ) {

                Column(

                    horizontalAlignment = Alignment.CenterHorizontally,

                    modifier = Modifier.padding(32.dp)

                ) {

                    Text(
                        "📱",
                        fontSize = MaterialTheme.typography.displayLarge.fontSize
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        "No hay sesiones de asistencia previas",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "El profesor debe generar un código QR desde la pantalla de tomar asistencia.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = onBack
                    ) {

                        Text("Volver")
                    }
                }
            }
        }

        return
    }

    // =========================
    // CARGAR DATOS
    // =========================

    LaunchedEffect(fechaSeleccionada) {

        if (fechaSeleccionada.isBlank()) {

            cargandoLista = false
            alumnos = emptyList()
            asistenciasRegistradas = emptySet()

            return@LaunchedEffect
        }

        cargandoLista = true

        asistenciaRepo.obtenerTokenPorFecha(
            grupoId,
            fechaSeleccionada
        ) { tokenEncontrado ->

            tokenSeleccionado = tokenEncontrado ?: ""

            alumnoRepo.obtenerAlumnos(grupoId) { listaAlumnos ->

                alumnos = listaAlumnos

                if (tokenSeleccionado.isNotBlank()) {

                    asistenciaRepo.obtenerAsistenciasPorSesion(
                        grupoId,
                        fechaSeleccionada,
                        tokenSeleccionado
                    ) { matriculas ->

                        asistenciasRegistradas = matriculas.toSet()

                        cargandoLista = false
                    }

                } else {

                    asistenciasRegistradas = emptySet()

                    cargandoLista = false
                }
            }
        }
    }

    // =========================
    // UI PRINCIPAL
    // =========================

    Scaffold(

        topBar = {

            CenterAlignedTopAppBar(

                title = {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            "Asistencia - $nombreGrupo",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            fechaSeleccionada,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {

                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            null,
                            tint = Color.Black
                        )
                    }
                },

                actions = {

                    if (fechasDisponibles.isNotEmpty()) {

                        IconButton(
                            onClick = {
                                mostrarDialogoFechas = true
                            }
                        ) {

                            Icon(
                                Icons.Default.CalendarToday,
                                null
                            )
                        }
                    }
                },

                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF2F2F7)
                )
            )
        },

        floatingActionButton = {

            if (alumnos.isNotEmpty()) {

                ExtendedFloatingActionButton(

                    onClick = {

                        exportarListaAsistencia(
                            context,
                            nombreGrupo,
                            fechaSeleccionada,
                            alumnos,
                            asistenciasRegistradas
                        )
                    },

                    containerColor = Color.Black,

                    shape = RoundedCornerShape(20.dp),

                    icon = {
                        Icon(Icons.Default.Download, null)
                    },

                    text = {
                        Text(
                            "Exportar CSV",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            if (cargandoLista) {

                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )

            } else {

                LazyColumn(

                    modifier = Modifier.fillMaxSize(),

                    contentPadding = PaddingValues(20.dp),

                    verticalArrangement = Arrangement.spacedBy(12.dp)

                ) {

                    item {

                        Text(
                            "Resumen: ${asistenciasRegistradas.size} de ${alumnos.size} presentes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(alumnos) { (matricula, nombre) ->

                        val presente =
                            asistenciasRegistradas.contains(matricula)

                        Surface(

                            shape = RoundedCornerShape(16.dp),

                            color = if (presente)
                                Color(0xFFE8F5E9)
                            else
                                Color.White,

                            shadowElevation = 1.dp,

                            modifier = Modifier.fillMaxWidth()

                        ) {

                            Row(

                                modifier = Modifier.padding(16.dp),

                                verticalAlignment = Alignment.CenterVertically

                            ) {

                                Box(

                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF2F2F7)),

                                    contentAlignment = Alignment.Center

                                ) {

                                    Icon(
                                        Icons.Default.Person,
                                        null,
                                        tint = Color.DarkGray
                                    )
                                }

                                Spacer(Modifier.width(16.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    Text(
                                        nombre,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Text(
                                        "Matrícula: $matricula",
                                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                        color = Color.Gray
                                    )
                                }

                                if (presente) {

                                    Icon(
                                        Icons.Default.CheckCircle,
                                        null,
                                        tint = Color(0xFF34C759)
                                    )

                                    Spacer(Modifier.width(4.dp))

                                    Text(
                                        "Presente",
                                        color = Color(0xFF34C759),
                                        fontWeight = FontWeight.Medium
                                    )

                                } else {

                                    Text(
                                        "Ausente",
                                        color = Color(0xFFFF3B30),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================
    // DIALOGO FECHAS
    // =========================

    if (mostrarDialogoFechas && fechasDisponibles.isNotEmpty()) {

        AlertDialog(

            onDismissRequest = {
                mostrarDialogoFechas = false
            },

            title = {
                Text("Seleccionar fecha")
            },

            text = {

                Column {

                    fechasDisponibles.forEach { fecha ->

                        TextButton(

                            onClick = {

                                fechaSeleccionada = fecha
                                mostrarDialogoFechas = false
                            },

                            modifier = Modifier.fillMaxWidth()

                        ) {

                            Text(
                                fecha,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }

                        HorizontalDivider()
                    }
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        mostrarDialogoFechas = false
                    }
                ) {

                    Text("Cancelar")
                }
            }
        )
    }
}

// =========================
// EXPORTAR CSV
// =========================

private fun exportarListaAsistencia(
    context: Context,
    nombreGrupo: String,
    fecha: String,
    alumnos: List<Pair<String, String>>,
    presentes: Set<String>
) {

    if (alumnos.isEmpty()) {

        Toast.makeText(
            context,
            "No hay alumnos para exportar",
            Toast.LENGTH_SHORT
        ).show()

        return
    }

    runCatching {

        val archivo = crearArchivoAsistenciaCsv(
            context,
            nombreGrupo,
            fecha,
            alumnos,
            presentes
        )

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )

        val intent = Intent(Intent.ACTION_SEND).apply {

            type = "text/csv"

            putExtra(Intent.EXTRA_STREAM, uri)

            putExtra(
                Intent.EXTRA_SUBJECT,
                "Lista de asistencia - $nombreGrupo - $fecha"
            )

            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(intent, "Exportar asistencia")
        )

    }.onFailure { error ->

        Toast.makeText(
            context,
            "Error al exportar: ${error.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}

private fun crearArchivoAsistenciaCsv(
    context: Context,
    nombreGrupo: String,
    fecha: String,
    alumnos: List<Pair<String, String>>,
    presentes: Set<String>
): File {

    val carpeta = File(
        context.cacheDir,
        "asistencias"
    ).apply {
        mkdirs()
    }

    val timestamp = SimpleDateFormat(
        "HHmmss",
        Locale.getDefault()
    ).format(Date())

    val nombreLimpio = nombreGrupo.replace(
        Regex("[^A-Za-z0-9_-]"),
        "_"
    )

    val archivo = File(
        carpeta,
        "asistencia_${nombreLimpio}_${fecha}_$timestamp.csv"
    )

    val contenido = buildString {

        appendLine(
            "Grupo,Materia,Fecha,Matrícula,Nombre,Asistencia"
        )

        alumnos.forEach { (matricula, nombre) ->

            val estado =
                if (presentes.contains(matricula))
                    "Presente"
                else
                    "Ausente"

            appendLine(
                "${nombreGrupo.aCsv()},${nombreGrupo.aCsv()},$fecha,$matricula,${nombre.aCsv()},$estado"
            )
        }
    }

    archivo.writeText(contenido, Charsets.UTF_8)

    return archivo
}

private fun String.aCsv(): String =
    "\"${replace("\"", "\"\"")}\""