package com.equipo1.controlasistencia

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.equipo1.controlasistencia.screens.*
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppNavegacion() {
    var nombreUsuarioGlobal by rememberSaveable { mutableStateOf("") }
    var rolUsuarioGlobal by rememberSaveable { mutableStateOf("") }
    var uidUsuarioGlobal by rememberSaveable { mutableStateOf("") }  // matrícula

    var pantallaActual by rememberSaveable { mutableStateOf("login") }
    var grupoSeleccionadoId by rememberSaveable { mutableStateOf("") }
    var grupoSeleccionadoNombre by rememberSaveable { mutableStateOf("") }

    when (pantallaActual) {

        "login" -> LoginScreen(
            onLoginSuccess = { rol, nombre, uid ->
                nombreUsuarioGlobal = nombre
                rolUsuarioGlobal = rol
                uidUsuarioGlobal = uid
                pantallaActual = when (rol) {
                    "admin" -> "home_escolar"
                    "profesor" -> "home_profesor"
                    "alumno" -> "alumno_home"
                    else -> "login"
                }
            },
            onNavigateToForgotPassword = {
                pantallaActual = "forgot_password"
            }
        )

        "forgot_password" -> {
            ForgotPasswordScreen(
                onBack = { pantallaActual = "login" }
            )
        }

        // ========== ADMINISTRADOR (ESCOLAR) ==========
        "home_escolar" -> {
            HomeEscolarScreen(
                nombreAdmin = nombreUsuarioGlobal,
                onLogout = { pantallaActual = "login" },
                onVerDetalleGrupo = { grupoId, nombreGrupo ->
                    grupoSeleccionadoId = grupoId
                    grupoSeleccionadoNombre = nombreGrupo
                    pantallaActual = "gestion_alumnos"   // Admin puede asignar alumnos
                }
            )
        }

        "gestion_alumnos" -> {
            if (grupoSeleccionadoId.isBlank()) {
                pantallaActual = "home_escolar"
            } else {
                GestionAlumnosGrupoScreen(
                    grupoId = grupoSeleccionadoId,
                    nombreGrupo = grupoSeleccionadoNombre,
                    onBack = { pantallaActual = "home_escolar" }
                )
            }
        }

        // ========== PROFESOR ==========
        "home_profesor" -> {
            HomeProfesorScreen(
                profesorMatricula = uidUsuarioGlobal,
                nombreProfesor = nombreUsuarioGlobal,
                onGrupoClick = { grupoId, nombreGrupo ->
                    grupoSeleccionadoId = grupoId
                    grupoSeleccionadoNombre = nombreGrupo
                    pantallaActual = "profesor_detalle"
                },
                onBack = { pantallaActual = "login" }
            )
        }

        "profesor_detalle" -> {
            if (grupoSeleccionadoId.isBlank()) {
                pantallaActual = "home_profesor"
            } else {
                DetalleGrupoProfesorScreen(
                    nombreGrupo = grupoSeleccionadoNombre,
                    onVerAlumnos = { pantallaActual = "lista_alumnos_profesor" },
                    onTomarAsistencia = { pantallaActual = "tomar_asistencia" },
                    onBack = { pantallaActual = "home_profesor" }
                )
            }
        }

        "lista_alumnos_profesor" -> {
            ListaAlumnosScreen(
                grupoId = grupoSeleccionadoId,
                nombreGrupo = grupoSeleccionadoNombre,
                esAdmin = false,   // profesor solo ve la lista
                onBack = { pantallaActual = "profesor_detalle" }
            )
        }

        "tomar_asistencia" -> {
            TomarAsistenciaScreen(
                grupoId = grupoSeleccionadoId,
                nombreGrupo = grupoSeleccionadoNombre,
                onBack = { pantallaActual = "profesor_detalle" }
            )
        }

        "reportes" -> {
            ReportesScreen(
                grupoId = grupoSeleccionadoId,
                nombreGrupo = grupoSeleccionadoNombre,
                onBack = { pantallaActual = "lista_alumnos_profesor" },
                onGuardarReporte = { /* exportar */ }
            )
        }

        // ========== ALUMNO ==========
        "alumno_home" -> {
            AlumnoHomeScreen(
                matriculaAlumno = uidUsuarioGlobal,
                nombreAlumno = nombreUsuarioGlobal,
                onEscanearClick = { grupoId, nombreGrupo ->
                    grupoSeleccionadoId = grupoId
                    grupoSeleccionadoNombre = nombreGrupo
                    pantallaActual = "scanner_alumno"
                },
                onHistorialClick = { grupoId, nombreGrupo ->
                    grupoSeleccionadoId = grupoId
                    grupoSeleccionadoNombre = nombreGrupo
                    pantallaActual = "historial_asistencia"
                },
                onLogout = { pantallaActual = "login" }
            )
        }

        "scanner_alumno" -> {
            CameraPermissionHandler(
                onPermissionGranted = {
                    ScannerScreen(
                        matriculaAlumno = uidUsuarioGlobal,
                        grupoIdEsperado = grupoSeleccionadoId,
                        onSuccess = { pantallaActual = "alumno_home" },
                        onBack = { pantallaActual = "alumno_home" }
                    )
                },
                onBack = { pantallaActual = "alumno_home" }
            )
        }

        "historial_asistencia" -> {
            HistorialAsistenciaScreen(
                matriculaAlumno = uidUsuarioGlobal,
                grupoId = grupoSeleccionadoId,
                nombreGrupo = grupoSeleccionadoNombre,
                onBack = { pantallaActual = "alumno_home" }
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermissionHandler(
    onPermissionGranted: @Composable () -> Unit,
    onBack: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )
    if (cameraPermissionState.status.isGranted) {
        onPermissionGranted()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Se necesita la cámara para el QR")
            Spacer(modifier = Modifier.padding(8.dp))
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Dar permiso")
            }
            TextButton(onClick = onBack) { Text("Regresar") }
        }
    }
}