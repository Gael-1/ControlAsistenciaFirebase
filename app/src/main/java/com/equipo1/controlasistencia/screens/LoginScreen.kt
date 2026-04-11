package com.equipo1.controlasistencia.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo1.controlasistencia.repository.AuthRepository

@Composable
fun LoginScreen(
    onLoginSuccess: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val authRepository = remember { AuthRepository() }

    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "CONTROL DE ASISTENCIA",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (correo.isNotBlank() && password.isNotBlank()) {
                    cargando = true
                    errorMsg = ""
                    authRepository.login(correo, password) { success, mensaje, rol, nombre ->
                        cargando = false
                        if (success) {
                            onLoginSuccess(rol ?: "profesor", nombre ?: "Usuario")
                        } else {
                            errorMsg = mensaje ?: "Error al iniciar sesión"
                        }
                    }
                } else {
                    errorMsg = "Por favor llena todos los campos"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        ) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Iniciar Sesión")
            }
        }

        if (errorMsg.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = errorMsg, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("¿No tienes cuenta? Registrarse")
        }
    }
}