package com.equipo1.controlasistencia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.equipo1.controlasistencia.screens.LoginScreen
import com.equipo1.controlasistencia.screens.RegisterScreen
import com.equipo1.controlasistencia.ui.theme.ControlAsistenciaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ControlAsistenciaTheme {

                var pantallaActual by remember { mutableStateOf("login") }

                when (pantallaActual) {

                    "login" -> LoginScreen(
                        onNavigateToRegister = {
                            pantallaActual = "register"
                        }
                    )

                    "register" -> RegisterScreen()
                }
            }
        }
    }
}