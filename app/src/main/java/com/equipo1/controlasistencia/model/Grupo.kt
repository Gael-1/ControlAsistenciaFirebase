package com.equipo1.controlasistencia.model

data class Grupo(
    val id: String = "",          // ID del documento en Firestore
    val nombre: String = "",      // Nombre de la materia (ej. Matemáticas)
    val profesorId: String = "",  // UID del profesor asignado
    val nombreProfesor: String = "", // Nombre del profesor para mostrar en la lista
    val codigoQr: String = ""     // El código/token estático generado por Escolar
)