package com.equipo1.controlasistencia.model

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val rol: String = "" // "profesor" o "alumno"
)