package com.equipo1.controlasistencia.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GrupoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun crearGrupo(nombre: String, onResult: (Boolean, String?) -> Unit) {
        val profesorId = auth.currentUser?.uid ?: return

        val grupo = hashMapOf(
            "nombre" to nombre,
            "profesorId" to profesorId
        )

        db.collection("grupos")
            .add(grupo)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener {
                onResult(false, it.message)
            }
    }

    fun obtenerGrupos(onResult: (List<Pair<String, String>>) -> Unit) {
        val profesorId = auth.currentUser?.uid ?: return

        db.collection("grupos")
            .whereEqualTo("profesorId", profesorId)
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents.map {
                    Pair(it.id, it.getString("nombre") ?: "")
                }
                onResult(lista)
            }
    }
}