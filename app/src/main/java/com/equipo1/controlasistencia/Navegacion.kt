package com.equipo1.controlasistencia

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.google.accompanist.permissions.isGranted
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavegacion() {
    // 1. Definimos auth y nombre de forma global al inicio (Esto quita los rojos)
    val auth = remember { FirebaseAuth.getInstance() }
    var nombreUsuarioGlobal by rememberSaveable { mutableStateOf("") }

    var pantallaActual by rememberSaveable { mutableStateOf("login") }
    var grupoSeleccionadoId by rememberSaveable { mutableStateOf("") }
    var grupoSeleccionadoNombre by rememberSaveable { mutableStateOf("") }

    when (pantallaActual) {

        "login" -> LoginScreen(
            onLoginSuccess = { rol, nombre ->
                nombreUsuarioGlobal = nombre // Guardamos el nombre real
                pantallaActual = if (rol == "profesor") "home" else "alumno_home"
            },
            onNavigateToRegister = {
                pantallaActual = "register"
            }
        )

        "register" -> RegisterScreen(
            onRegisterSuccess = {
                pantallaActual = "login"
            }
        )

        "home" -> HomeProfesorScreen(
            nombreProfesor = nombreUsuarioGlobal,
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

        "lista_alumnos" -> {
            if (grupoSeleccionadoId.isBlank()) {
                pantallaActual = "home"
            } else {
                ListaAlumnosScreen(
                    grupoId = grupoSeleccionadoId,
                    nombreGrupo = grupoSeleccionadoNombre,
                    onTomarAsistencia = { pantallaActual = "tomar_asistencia" },
                    onVerReportes = { pantallaActual = "reportes" },
                    onBack = { pantallaActual = "home" }
                )
            }
        }

        "tomar_asistencia" -> {
            if (grupoSeleccionadoId.isBlank()) {
                pantallaActual = "home"
            } else {
                TomarAsistenciaScreen(
                    grupoId = grupoSeleccionadoId,
                    nombreGrupo = grupoSeleccionadoNombre,
                    onBack = { pantallaActual = "lista_alumnos" }
                )
            }
        }

        "reportes" -> {
            if (grupoSeleccionadoId.isBlank()) {
                pantallaActual = "home"
            } else {
                ReportesScreen(
                    nombreGrupo = grupoSeleccionadoNombre,
                    onBack = { pantallaActual = "lista_alumnos" },
                    onGuardarReporte = { pantallaActual = "lista_alumnos" }
                )
            }
        }

        "alumno_home" -> {
            // ✅ Eliminamos 'val nombreUsuario = ""' de aquí para usar la global
            AlumnoHomeScreen(
                nombreAlumno = nombreUsuarioGlobal,
                onEscanearClick = {
                    pantallaActual = "scanner_alumno"
                },
                onLogout = {
                    auth.signOut()
                    pantallaActual = "login"
                }
            )
        }

        "scanner_alumno" -> {
            CameraPermissionHandler(
                onPermissionGranted = {
                    ScannerScreen(
                        // ✅ Ahora 'auth' sí existe porque se definió al inicio
                        alumnoId = auth.currentUser?.uid ?: "",
                        onSuccess = { pantallaActual = "alumno_home" },
                        onBack = { pantallaActual = "alumno_home" }
                    )
                },
                onBack = { pantallaActual = "alumno_home" }
            )
        }
    }
}

@OptIn(com.google.accompanist.permissions.ExperimentalPermissionsApi::class)
@Composable
fun CameraPermissionHandler(
    onPermissionGranted: @Composable () -> Unit,
    onBack: () -> Unit
) {
    val cameraPermissionState = com.google.accompanist.permissions.rememberPermissionState(
        android.Manifest.permission.CAMERA
    )
    if (cameraPermissionState.status.isGranted) {
        onPermissionGranted()
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Se necesita la cámara para el QR")
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(8.dp))
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Dar permiso")
            }
            TextButton(onClick = onBack) { Text("Regresar") }
        }
    }
}