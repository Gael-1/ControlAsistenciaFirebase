package com.equipo1.controlasistencia.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.equipo1.controlasistencia.repository.AuthRepository

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit
) {

    val authRepository = AuthRepository()

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("alumno") }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Registro",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Selecciona Rol")

        Row {
            RadioButton(
                selected = rol == "alumno",
                onClick = { rol = "alumno" }
            )
            Text("Alumno")

            Spacer(modifier = Modifier.width(16.dp))

            RadioButton(
                selected = rol == "profesor",
                onClick = { rol = "profesor" }
            )
            Text("Profesor")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                cargando = true
                authRepository.registrar(
                    nombre,
                    correo,
                    password,
                    rol
                ) { success, error ->
                    cargando = false
                    if (success) {
                        mensaje = "Registro exitoso"
                        onRegisterSuccess()
                    } else {
                        mensaje = error ?: "Error desconocido, intentalo de nuevo mas tarde"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrarse")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (cargando) {
            CircularProgressIndicator()
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(mensaje)
    }
}