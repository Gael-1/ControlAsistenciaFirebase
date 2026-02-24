package com.equipo1.controlasistencia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.equipo1.controlasistencia.ui.theme.ControlAsistenciaTheme
import com.equipo1.controlasistencia.AppNavegacion

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ControlAsistenciaTheme {
                AppNavegacion()
            }
        }
    }
}