package com.equipo1.controlasistencia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo1.controlasistencia.repository.AuthRepository

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit
) {
    val authRepository = remember { AuthRepository() }

    var nombre by remember { mutableStateOf("") }
    var userInput by remember { mutableStateOf("") } // Puede ser correo o número de socio
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) } // <--- Nuevo estado para el ojito
    var rol by remember { mutableStateOf("alumno") }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    val appleGrayBackground = Color(0xFFF2F2F7)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = appleGrayBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Cabecera minimalista
            Text(
                text = "REGISTRO",
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = 4.sp,
                color = Color.Gray
            )
            Text(
                text = "Crear Cuenta",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Campo Nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre Completo") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo Dinámico: Correo o Número de Socio
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                label = { Text(if (rol == "profesor") "Número de Socio" else "Correo Electrónico") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (rol == "profesor") KeyboardType.Number else KeyboardType.Email
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo Contraseña con Ojito
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                // Lógica de visibilidad
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description, tint = Color.Gray)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Selector de Rol Estilo Apple
            Text(
                "Selecciona tu cargo",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("alumno", "profesor").forEach { opcion ->
                    val esSeleccionado = rol == opcion
                    Button(
                        onClick = { rol = opcion },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (esSeleccionado) Color.Black else Color.White,
                            contentColor = if (esSeleccionado) Color.White else Color.Black
                        ),
                        elevation = ButtonDefaults.buttonElevation(if (esSeleccionado) 4.dp else 0.dp)
                    ) {
                        Text(opcion.replaceFirstChar { it.uppercase() })
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de Registro
            Button(
                onClick = {
                    if (nombre.isNotBlank() && userInput.isNotBlank() && password.isNotBlank()) {
                        cargando = true

                        val correoFinal = if (rol == "profesor" && !userInput.contains("@")) {
                            "$userInput@control.com"
                        } else {
                            userInput
                        }

                        authRepository.registrar(nombre, correoFinal, password, rol) { success, error ->
                            cargando = false
                            if (success) {
                                mensaje = "¡Cuenta creada con éxito!"
                                onRegisterSuccess()
                            } else {
                                mensaje = error ?: "Error al registrar"
                            }
                        }
                    } else {
                        mensaje = "Completa todos los campos"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                enabled = !cargando
            ) {
                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Registrarse", fontWeight = FontWeight.Bold)
                }
            }

            if (mensaje.isNotEmpty()) {
                Text(
                    text = mensaje,
                    color = if (mensaje.contains("éxito")) Color(0xFF4CAF50) else Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}