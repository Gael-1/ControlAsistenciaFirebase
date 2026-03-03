package com.equipo1.controlasistencia.repository

import com.google.firebase.firestore.FirebaseFirestore

class AlumnoRepository {

    private val db = FirebaseFirestore.getInstance()

    fun agregarAlumno(
        grupoId: String,
        nombre: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val alumno = hashMapOf(
            "nombre" to nombre
        )

        db.collection("grupos")
            .document(grupoId)
            .collection("alumnos")
            .add(alumno)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener {
                onResult(false, it.message)
            }
    }

    fun obtenerAlumnos(
        grupoId: String,
        onResult: (List<Pair<String, String>>) -> Unit
    ) {
        db.collection("grupos")
            .document(grupoId)
            .collection("alumnos")
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents.map {
                    Pair(it.id, it.getString("nombre") ?: "")
                }
                onResult(lista)
            }
    }
}