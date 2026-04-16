package com.equipo1.controlasistencia.model

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val rol: String = "" // Valores posibles: "escolar", "profesor", "alumno"
) {
    // Esto te ayudará a no cometer errores de dedo al comparar roles en el código
    companion object {
        const val ROL_ESCOLAR = "escolar"
        const val ROL_PROFESOR = "profesor"
        const val ROL_ALUMNO = "alumno"
    }
}