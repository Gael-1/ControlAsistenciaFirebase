package com.equipo1.controlasistencia.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

private enum class TipoLogin(
    val titulo: String,
    val etiqueta: String,
    val ejemplo: String,
    val tipoAcceso: String
) {
    ALUMNO(
        titulo = "Alumno",
        etiqueta = "Matrícula",
        ejemplo = "Ej. 2022477",
        tipoAcceso = AuthRepository.TIPO_ALUMNO
    ),
    EMPLEADO(
        titulo = "Empleado",
        etiqueta = "Número de socio",
        ejemplo = "Ej. 150504",
        tipoAcceso = AuthRepository.TIPO_EMPLEADO
    )
}

@Composable
fun LoginScreen(
    onLoginSuccess: (String, String, String) -> Unit, // (rol, nombre, uid)
    onNavigateToForgotPassword: () -> Unit
) {
    val authRepository = remember { AuthRepository() }

    var identificadorInput by remember { mutableStateOf("") }
    var tipoLogin by remember { mutableStateOf(TipoLogin.ALUMNO) }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

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
            Text(
                text = "CONTROL",
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = 4.sp,
                color = Color.Gray
            )

            Text(
                text = "Asistencia",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "Tipo de acceso",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TipoLogin.values().forEach { tipo ->
                    val seleccionado = tipoLogin == tipo
                    val botonModifier = Modifier
                        .weight(1f)
                        .height(48.dp)

                    if (seleccionado) {
                        Button(
                            onClick = {
                                tipoLogin = tipo
                                identificadorInput = ""
                                errorMsg = ""
                            },
                            modifier = botonModifier,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                        ) {
                            Text(tipo.titulo, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                tipoLogin = tipo
                                identificadorInput = ""
                                errorMsg = ""
                            },
                            modifier = botonModifier,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = tipo.titulo,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = identificadorInput,
                onValueChange = {
                    identificadorInput = it
                    errorMsg = ""
                },
                label = { Text(tipoLogin.etiqueta) },
                placeholder = { Text(tipoLogin.ejemplo) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                isError = errorMsg.isNotEmpty()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMsg = ""
                },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) {
                        Icons.Filled.Visibility
                    } else {
                        Icons.Filled.VisibilityOff
                    }

                    val description = if (passwordVisible) {
                        "Ocultar contraseña"
                    } else {
                        "Mostrar contraseña"
                    }

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = image,
                            contentDescription = description,
                            tint = Color.Gray
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                isError = errorMsg.isNotEmpty()
            )

            if (errorMsg.isNotEmpty()) {
                Text(
                    text = errorMsg,
                    color = Color(0xFFFF3B30),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (identificadorInput.isNotBlank() && password.isNotBlank()) {
                        cargando = true
                        errorMsg = ""

                        authRepository.login(
                            identificador = identificadorInput.trim(),
                            password = password,
                            tipoAcceso = tipoLogin.tipoAcceso
                        ) { success, mensaje, rol, nombre, uid ->
                            cargando = false

                            if (success) {
                                onLoginSuccess(
                                    rol ?: "alumno",
                                    nombre ?: "Usuario",
                                    uid ?: ""
                                )
                            } else {
                                errorMsg = mensaje ?: "Credenciales incorrectas"
                            }
                        }
                    } else {
                        errorMsg = "Por favor completa todos los campos"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                enabled = !cargando
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Entrar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = onNavigateToForgotPassword) {
                Text(
                    "¿Olvidaste tu contraseña?",
                    color = Color(0xFF007AFF),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}