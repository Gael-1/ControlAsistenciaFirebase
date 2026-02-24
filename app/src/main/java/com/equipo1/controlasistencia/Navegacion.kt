package com.equipo1.controlasistencia

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.equipo1.controlasistencia.screens.*

@Composable
fun AppNavegacion() {
    var pantallaActual by rememberSaveable { mutableStateOf("login") }
    var grupoSeleccionado by rememberSaveable { mutableStateOf("") }
    var nombreProfesor by rememberSaveable { mutableStateOf("Profe Juan") }

    when (pantallaActual) {
        "login" -> LoginScreen(
            onLoginSuccess = { rol, nombre ->
                nombreProfesor = nombre
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
            nombreProfesor = nombreProfesor,
            onGrupoClick = { grupo ->
                grupoSeleccionado = grupo
                pantallaActual = "lista_alumnos"
            },
            onCreateGrupo = {
                // aquí va la pantalla de crear grupo
            }
        )

        "lista_alumnos" -> ListaAlumnosScreen(
            nombreGrupo = grupoSeleccionado,
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

        "tomar_asistencia" -> TomarAsistenciaScreen(
            nombreGrupo = grupoSeleccionado,
            fecha = "22/02/2026",
            onGuardar = {
                pantallaActual = "lista_alumnos"
            },
            onBack = {
                pantallaActual = "lista_alumnos"
            }
        )

        "reportes" -> ReportesScreen(
            nombreGrupo = grupoSeleccionado,
            onBack = {
                pantallaActual = "lista_alumnos"
            },
            onGuardarReporte = {
                pantallaActual = "lista_alumnos"
            }
        )
    }
}