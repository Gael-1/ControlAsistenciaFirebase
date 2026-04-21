package com.equipo1.controlasistencia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo1.controlasistencia.repository.AuthRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit
) {
    val authRepository = remember { AuthRepository() }

    var matriculaInput by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var correoEnviado by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    var mensajeEsError by remember { mutableStateOf(false) }

    val appleGrayBackground = Color(0xFFF2F2F7)

    Scaffold(
        containerColor = appleGrayBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "RECUPERAR CONTRASEÑA",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = appleGrayBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 32.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.05f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(if (correoEnviado) "📧" else "🔐", fontSize = 48.sp)
                }
            }

            Text(
                text = if (correoEnviado) "¡Correo enviado!" else "¿Olvidaste tu contraseña?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (correoEnviado)
                    "Revisa tu bandeja de entrada y sigue las instrucciones para restablecer tu contraseña."
                else
                    "Ingresa tu matrícula y te enviaremos un enlace para restablecer tu contraseña.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (!correoEnviado) {
                // Campo de matrícula
                OutlinedTextField(
                    value = matriculaInput,
                    onValueChange = {
                        matriculaInput = it
                        mensaje = ""
                    },
                    label = { Text("Tu matrícula") },
                    placeholder = { Text("Ej. 2022477") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    isError = mensajeEsError
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botón enviar
                Button(
                    onClick = {
                        if (matriculaInput.isNotBlank()) {
                            cargando = true
                            mensaje = ""

                            authRepository.enviarCorreoRestablecimiento(matriculaInput) { success, msg ->
                                cargando = false
                                if (success) {
                                    correoEnviado = true
                                    mensaje = msg ?: "Correo enviado"
                                    mensajeEsError = false
                                } else {
                                    mensaje = msg ?: "Error al enviar correo"
                                    mensajeEsError = true
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    enabled = !cargando && matriculaInput.isNotBlank()
                ) {
                    if (cargando) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Enviar correo de recuperación", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Mensaje de estado
            if (mensaje.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (mensajeEsError)
                        Color(0xFFFF3B30).copy(alpha = 0.1f)
                    else
                        Color(0xFF34C759).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = mensaje,
                        modifier = Modifier.padding(16.dp),
                        color = if (mensajeEsError) Color(0xFFFF3B30) else Color(0xFF34C759),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón volver
            TextButton(onClick = onBack) {
                Text(
                    "← Volver al inicio de sesión",
                    color = Color(0xFF007AFF),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}