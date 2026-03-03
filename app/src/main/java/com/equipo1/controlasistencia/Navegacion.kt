package com.equipo1.controlasistencia

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.equipo1.controlasistencia.screens.*

@Composable
fun AppNavegacion() {

    var pantallaActual by rememberSaveable { mutableStateOf("login") }

    var grupoSeleccionadoId by rememberSaveable { mutableStateOf("") }
    var grupoSeleccionadoNombre by rememberSaveable { mutableStateOf("") }

    var nombreProfesor by rememberSaveable { mutableStateOf("Profe Juan") }

    when (pantallaActual) {

        "login" -> LoginScreen(
            onLoginSuccess = { rol, nombre ->
                nombreProfesor = nombre
                pantallaActual =
                    if (rol == "profesor") "home" else "alumno_home"
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
            nombreProfesor = nombreProfesor,
            onGrupoClick = { grupoId, nombreGrupo ->

                // 🔥 Validación para evitar errores
                if (grupoId.isNotBlank()) {
                    grupoSeleccionadoId = grupoId
                    grupoSeleccionadoNombre = nombreGrupo
                    pantallaActual = "lista_alumnos"
                }
            }
        )

        "lista_alumnos" -> {

            // 🔥 Protección extra
            if (grupoSeleccionadoId.isBlank()) {
                pantallaActual = "home"
            } else {
                ListaAlumnosScreen(
                    grupoId = grupoSeleccionadoId,
                    nombreGrupo = grupoSeleccionadoNombre,
                    onTomarAsistencia = {
                        pantallaActual = "tomar_asistencia"
                    },
                    onVerReportes = {
                        pantallaActual = "reportes"
                    },
                    onBack = {
                        pantallaActual = "home"
                    }
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
                    onBack = {
                        pantallaActual = "lista_alumnos"
                    }
                )
            }
        }

        "reportes" -> {

            if (grupoSeleccionadoId.isBlank()) {
                pantallaActual = "home"
            } else {
                ReportesScreen(
                    nombreGrupo = grupoSeleccionadoNombre,
                    onBack = {
                        pantallaActual = "lista_alumnos"
                    },
                    onGuardarReporte = {
                        pantallaActual = "lista_alumnos"
                    }
                )
            }
        }
    }
}

@Composable
fun TomarAsistenciaScreen(grupoId: String, nombreGrupo: String, onBack: () -> Unit) {
    TODO("Not yet implemented")
}