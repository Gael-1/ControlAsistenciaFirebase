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
    var uidUsuarioGlobal by rememberSaveable { mutableStateOf("") }

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
                    "escolar" -> "home_escolar"
                    "profesor" -> "home"
                    else -> "alumno_home"
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

        // --- ROL ESCOLAR (Administrador) ---
        "home_escolar" -> {
            HomeEscolarScreen(
                nombreAdmin = nombreUsuarioGlobal,
                onLogout = {
                    pantallaActual = "login"
                },
                onVerDetalleGrupo = { grupoId, nombreGrupo ->
                    grupoSeleccionadoId = grupoId
                    grupoSeleccionadoNombre = nombreGrupo
                    pantallaActual = "lista_alumnos"
                }
            )
        }

        // --- ROL PROFESOR ---
        "home" -> {
            HomeProfesorScreen(
                nombreProfesor = if (nombreUsuarioGlobal.isBlank()) "Profesor" else nombreUsuarioGlobal,
                onGrupoClick = { grupoId, nombreGrupo ->
                    if (grupoId.isNotBlank()) {
                        grupoSeleccionadoId = grupoId
                        grupoSeleccionadoNombre = nombreGrupo
                        pantallaActual = "lista_alumnos"
                    }
                },
                onBack = {
                    pantallaActual = "login"
                }
            )
        }

        "lista_alumnos" -> {
            if (grupoSeleccionadoId.isBlank()) {
                pantallaActual = if (rolUsuarioGlobal == "escolar") "home_escolar" else "home"
            } else {
                ListaAlumnosScreen(
                    grupoId = grupoSeleccionadoId,
                    nombreGrupo = grupoSeleccionadoNombre,
                    esAdmin = rolUsuarioGlobal == "escolar",
                    onTomarAsistencia = { pantallaActual = "tomar_asistencia" },
                    onVerReportes = { pantallaActual = "reportes" },
                    onBack = {
                        pantallaActual = if (rolUsuarioGlobal == "escolar") "home_escolar" else "home"
                    }
                )
            }
        }

        "tomar_asistencia" -> {
            TomarAsistenciaScreen(
                grupoId = grupoSeleccionadoId,
                nombreGrupo = grupoSeleccionadoNombre,
                onBack = { pantallaActual = "lista_alumnos" }
            )
        }

        "reportes" -> {
            ReportesScreen(
                nombreGrupo = grupoSeleccionadoNombre,
                onBack = { pantallaActual = "lista_alumnos" },
                onGuardarReporte = { pantallaActual = "lista_alumnos" }
            )
        }

        // --- ROL ALUMNO ---
        "alumno_home" -> {
            AlumnoHomeScreen(
                nombreAlumno = nombreUsuarioGlobal,
                onEscanearClick = { pantallaActual = "scanner_alumno" },
                onLogout = {
                    pantallaActual = "login"
                }
            )
        }

        "scanner_alumno" -> {
            CameraPermissionHandler(
                onPermissionGranted = {
                    ScannerScreen(
                        alumnoId = uidUsuarioGlobal,
                        onSuccess = { pantallaActual = "alumno_home" },
                        onBack = { pantallaActual = "alumno_home" }
                    )
                },
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