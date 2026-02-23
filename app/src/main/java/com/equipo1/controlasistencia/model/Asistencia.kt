package com.equipo1.controlasistencia.model

data class Asistencia(
    val id: String = "",
    val alumnoId: String = "",
    val fecha: String = "",
    val estado: String = "" // Presente / Falta
)