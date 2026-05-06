package com.equipo1.controlasistencia.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class GrupoRepository {
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "GrupoRepo"

    // Obtener grupos de un profesor (conversión a número)
    fun obtenerGruposPorProfesor(matriculaProfesor: String, callback: (List<Pair<String, String>>) -> Unit) {
        val matriculaNum = matriculaProfesor.toLongOrNull()
        if (matriculaNum == null) {
            Log.e(TAG, "Matrícula inválida: $matriculaProfesor")
            callback(emptyList())
            return
        }
        Log.d(TAG, "Buscando grupos para profesor con matrícula numérica: $matriculaNum")
        db.collection("grupos")
            .whereEqualTo("profesorSocio", matriculaNum)
            .get()
            .addOnSuccessListener { snapshot ->
                val grupos = snapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val nombre = doc.getString("nombre") ?: ""
                    Log.d(TAG, "Grupo encontrado: $id - $nombre")
                    id to nombre
                }
                callback(grupos)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al obtener grupos: ${e.message}")
                callback(emptyList())
            }
    }

    // Escucha en tiempo real para profesores (opcional, si prefieres actualización automática)
    fun escucharGrupos(profesorMatricula: String, callback: (List<Pair<String, String>>) -> Unit) {
        val matriculaNum = profesorMatricula.toLongOrNull()
        if (matriculaNum == null) {
            callback(emptyList())
            return
        }
        db.collection("grupos")
            .whereEqualTo("profesorSocio", matriculaNum)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error en listener: ${error.message}")
                    callback(emptyList())
                    return@addSnapshotListener
                }
                val grupos = snapshot?.documents?.mapNotNull { doc ->
                    doc.id to (doc.getString("nombre") ?: "")
                } ?: emptyList()
                callback(grupos)
            }
    }
}