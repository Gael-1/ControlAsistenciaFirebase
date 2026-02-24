package com.equipo1.controlasistencia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.equipo1.controlasistencia.screens.*
import com.equipo1.controlasistencia.ui.theme.ControlAsistenciaTheme
import com.equipo1.controlasistencia.screens.ListaAlumnosScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ControlAsistenciaTheme {
                var pantallaActual by rememberSaveable { mutableStateOf("login") }
                var grupoSeleccionado by rememberSaveable { mutableStateOf("") }
                var nombreProfesor by rememberSaveable { mutableStateOf("Profe Juan") }

                when (pantallaActual) {
                    "login" -> LoginScreen(
                        onLoginSuccess = { rol, nombre ->
                            nombreProfesor = nombre
                            if (rol == "profesor") {
                                pantallaActual = "home"
                            } else {
                                pantallaActual = "alumno_home"
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

                    "home" -> HomeProfesorScreen(
                        nombreProfesor = nombreProfesor,
                        onGrupoClick = { grupo ->
                            grupoSeleccionado = grupo
                            pantallaActual = "lista_alumnos"
                        },
                        onCreateGrupo = {
                            //se ejecuta al crear grupo pero aun no se agrega la pantalla agregenla despuues
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
        }
    }
}