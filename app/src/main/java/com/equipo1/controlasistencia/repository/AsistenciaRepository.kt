package com.equipo1.controlasistencia.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class AsistenciaRepository {

    private val db = FirebaseFirestore.getInstance()

    fun actualizarTokenAsistencia(
        grupoId: String,
        token: String,
        fecha: String,
        onResult: (Boolean) -> Unit
    ) {
        val datos = hashMapOf(
            "tokenActivo" to token,
            "fecha" to fecha,
            "ultimaActualizacion" to System.currentTimeMillis()
        )

        db.collection("grupos").document(grupoId)
            .collection("asistencias").document(fecha)
            .set(datos, SetOptions.merge())
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    fun registrarAsistenciaAlumno(
        grupoId: String,
        fecha: String,
        alumnoId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val actualizacion = hashMapOf<String, Any>(
            "registros.$alumnoId" to true
        )

        db.collection("grupos").document(grupoId)
            .collection("asistencias").document(fecha)
            .update(actualizacion)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener {
                val nuevoRegistro = hashMapOf(
                    "registros" to hashMapOf(alumnoId to true)
                )
                db.collection("grupos").document(grupoId)
                    .collection("asistencias").document(fecha)
                    .set(nuevoRegistro, SetOptions.merge())
                    .addOnSuccessListener { onResult(true, null) }
                    .addOnFailureListener { onResult(false, it.message) }
            }
    }
}