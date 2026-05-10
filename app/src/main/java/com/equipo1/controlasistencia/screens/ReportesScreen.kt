package com.equipo1.controlasistencia.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.equipo1.controlasistencia.repository.AsistenciaRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesScreen(
    grupoId: String,
    nombreGrupo: String,
    onBack: () -> Unit
) {
    val repo = remember { AsistenciaRepository() }
    val context = LocalContext.current
    var reportes by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(grupoId) {
        repo.obtenerReporteGrupo(grupoId) { lista ->
            reportes = lista
            cargando = false
        }
    }

    Scaffold(
        containerColor = Color(0xFFF2F2F7),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(nombreGrupo, fontWeight = FontWeight.Bold)
                        Text(
                            text = "ESTADÍSTICAS",
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF2F2F7)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            Text(
                text = "Rendimiento de Asistencia",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Resumen por alumno",
                color = Color.Gray
            )

            Spacer(Modifier.height(24.dp))

            if (cargando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reportes) { (alumno, porcentaje) ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            shadowElevation = 2.dp,
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
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Alumno",
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.Gray
                                    )
                                }

                                Spacer(Modifier.width(16.dp))

                                Text(
                                    text = alumno,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )

                                val colorStatus = when {
                                    porcentaje >= 90 -> Color(0xFF34C759)
                                    porcentaje >= 80 -> Color(0xFFFF9500)
                                    else -> Color(0xFFFF3B30)
                                }

                                Surface(
                                    color = colorStatus.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "$porcentaje%",
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 4.dp
                                        ),
                                        color = colorStatus,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { exportarReporteCsv(context, nombreGrupo, reportes) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(18.dp),
                enabled = !cargando && reportes.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Exportar"
                )
                Spacer(Modifier.width(8.dp))
                Text("Exportar Reporte", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun exportarReporteCsv(
    context: Context,
    nombreGrupo: String,
    reportes: List<Pair<String, Int>>
) {
    if (reportes.isEmpty()) {
        Toast.makeText(context, "No hay datos para exportar", Toast.LENGTH_SHORT).show()
        return
    }

    runCatching {
        val archivo = crearArchivoReporteCsv(context, nombreGrupo, reportes)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Reporte de asistencia - $nombreGrupo")
            putExtra(Intent.EXTRA_TEXT, "Reporte de asistencia del grupo $nombreGrupo")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Exportar reporte"))
    }.onFailure { error ->
        Toast.makeText(
            context,
            "No se pudo exportar: ${error.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}

private fun crearArchivoReporteCsv(
    context: Context,
    nombreGrupo: String,
    reportes: List<Pair<String, Int>>
): File {
    val carpetaReportes = File(context.cacheDir, "reportes").apply { mkdirs() }
    val fechaArchivo = SimpleDateFormat(
        "yyyyMMdd_HHmmss",
        Locale.getDefault()
    ).format(Date())

    val nombreSeguro = nombreGrupo.replace(Regex("[^A-Za-z0-9_-]"), "_")
    val archivo = File(carpetaReportes, "reporte_${nombreSeguro}_$fechaArchivo.csv")

    val contenido = buildString {
        appendLine("Grupo,Alumno,Porcentaje de asistencia")
        reportes.forEach { (alumno, porcentaje) ->
            appendLine("${nombreGrupo.aCsv()},${alumno.aCsv()},$porcentaje%")
        }
    }

    archivo.writeText(contenido, Charsets.UTF_8)
    return archivo
}

private fun String.aCsv(): String {
    val valorEscapado = replace("\"", "\"\"")
    return "\"$valorEscapado\""
}