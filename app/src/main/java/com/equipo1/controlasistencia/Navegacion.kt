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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api

@Composable
fun AppNavegacion() {
    val auth = remember { FirebaseAuth.getInstance() }
    var nombreUsuarioGlobal by rememberSaveable { mutableStateOf("") }
    var rolUsuarioGlobal by rememberSaveable { mutableStateOf("") } // Guardamos el rol para lógica interna

    var pantallaActual by rememberSaveable { mutableStateOf("login") }
    var grupoSeleccionadoId by rememberSaveable { mutableStateOf("") }
    var grupoSeleccionadoNombre by rememberSaveable { mutableStateOf("") }

    when (pantallaActual) {

        "login" -> LoginScreen(
            onLoginSuccess = { rol, nombre ->
                nombreUsuarioGlobal = nombre
                rolUsuarioGlobal = rol
                // Redirección según rol
                pantallaActual = when (rol) {
                    "escolar" -> "home_escolar"
                    "profesor" -> "home"
                    else -> "alumno_home"
                }
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

        // --- ROL ESCOLAR (Administrador) ---
        "home_escolar" -> {
            HomeEscolarScreen(
                nombreAdmin = nombreUsuarioGlobal,
                onLogout = {
                    auth.signOut()
                    pantallaActual = "login"
                },
                onVerDetalleGrupo = { grupoId, nombreGrupo ->
                    grupoSeleccionadoId = grupoId
                    grupoSeleccionadoNombre = nombreGrupo
                    pantallaActual = "lista_alumnos" // Reutilizamos pantalla
                }
            )
        }

        // --- ROL PROFESOR ---
        "home" -> {
            val user = auth.currentUser
            if (user != null) {
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
                        auth.signOut()
                        pantallaActual = "login"
                    }
                )
            } else {
                pantallaActual = "login"
            }
        }

        "lista_alumnos" -> {
            if (grupoSeleccionadoId.isBlank()) {
                pantallaActual = if (rolUsuarioGlobal == "escolar") "home_escolar" else "home"
            } else {
                ListaAlumnosScreen(
                    grupoId = grupoSeleccionadoId,
                    nombreGrupo = grupoSeleccionadoNombre,
                    esAdmin = rolUsuarioGlobal == "escolar", // Para mostrar/ocultar botones
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
                    auth.signOut()
                    pantallaActual = "login"
                }
            )
        }

        "scanner_alumno" -> {
            CameraPermissionHandler(
                onPermissionGranted = {
                    ScannerScreen(
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