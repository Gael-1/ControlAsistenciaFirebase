package com.equipo1.controlasistencia.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FieldPath
import java.text.SimpleDateFormat
import java.util.*

class AlumnoRepository {

    private val db = FirebaseFirestore.getInstance()

    fun obtenerAlumnos(
        grupoId: String,
        callback: (List<Pair<String, String>>) -> Unit
    ) {
        db.collection("grupos")
            .document(grupoId)
            .collection("alumnos")
            .get()
            .addOnSuccessListener { snapshot ->

                val lista = snapshot.documents.mapNotNull { doc ->
                    doc.id to (doc.getString("nombre") ?: "")
                }

                callback(lista)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    fun escucharAlumnos(
        grupoId: String,
        callback: (List<Pair<String, String>>) -> Unit
    ): ListenerRegistration {

        return db.collection("grupos")
            .document(grupoId)
            .collection("alumnos")
            .addSnapshotListener { snapshot, _ ->

                val lista = snapshot?.documents?.mapNotNull { doc ->
                    doc.id to (doc.getString("nombre") ?: "")
                } ?: emptyList()

                callback(lista)
            }
    }

    fun agregarAlumnoAGrupo(
        grupoId: String,
        matricula: String,
        nombre: String,
        callback: (Boolean, String?) -> Unit
    ) {

        val matriculaNum = matricula.toLongOrNull()

        if (matriculaNum == null) {
            callback(false, "Matrícula inválida")
            return
        }

        val grupoRef = db.collection("grupos").document(grupoId)
        val alumnoEnGrupoRef = grupoRef.collection("alumnos").document(matricula)
        val studentGroupRef = db.collection("student_groups").document()

        grupoRef.get()
            .addOnSuccessListener { grupoDoc ->

                val nombreGrupo = grupoDoc.getString("nombre") ?: ""

                val batch = db.batch()

                batch.set(
                    alumnoEnGrupoRef,
                    mapOf(
                        "nombre" to nombre,
                        "matricula" to matriculaNum
                    )
                )

                batch.set(
                    studentGroupRef,
                    mapOf(
                        "matricula" to matriculaNum,
                        "grupoId" to grupoId,
                        "nombreGrupo" to nombreGrupo,
                        "nombreAlumno" to nombre
                    )
                )

                batch.commit()
                    .addOnSuccessListener {
                        callback(true, null)
                    }
                    .addOnFailureListener { e ->
                        callback(false, e.message)
                    }
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }

    fun obtenerGruposDelAlumno(
        matricula: String,
        callback: (List<Pair<String, String>>) -> Unit
    ) {

        val matriculaNum = matricula.toLongOrNull()

        if (matriculaNum == null) {
            callback(emptyList())
            return
        }

        db.collection("student_groups")
            .whereEqualTo("matricula", matriculaNum)
            .get()
            .addOnSuccessListener { snapshot ->

                val grupoIds = snapshot.documents.mapNotNull {
                    it.getString("grupoId")
                }

                if (grupoIds.isEmpty()) {
                    callback(emptyList())
                    return@addOnSuccessListener
                }

                db.collection("grupos")
                    .whereIn(FieldPath.documentId(), grupoIds)
                    .get()
                    .addOnSuccessListener { gruposSnap ->

                        val lista = gruposSnap.documents.mapNotNull { doc ->
                            doc.id to (doc.getString("nombre") ?: "")
                        }

                        callback(lista)
                    }
                    .addOnFailureListener {
                        callback(emptyList())
                    }
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    // =========================
    // OBTENER ASISTENCIAS
    // =========================

    fun obtenerAsistenciasAlumno(
        matricula: String,
        grupoId: String? = null,
        callback: (List<Map<String, Any>>) -> Unit
    ) {

        var query = db.collection("asistencias")
            .whereEqualTo("matriculaAlumno", matricula)

        if (grupoId != null) {
            query = query.whereEqualTo("grupoId", grupoId)
        }

        // IMPORTANTE:
        // Se quitó orderBy("fecha")
        // porque estaba causando error por índice en Firestore

        query.get()
            .addOnSuccessListener { snapshot ->

                val lista = snapshot.documents.mapNotNull { doc ->

                    Log.d("ASISTENCIA_DOC", doc.data.toString())

                    doc.data
                }

                Log.d("ASISTENCIAS", lista.toString())

                callback(lista)
            }
            .addOnFailureListener { e ->

                Log.e("FIREBASE_ERROR", "Error obteniendo asistencias", e)

                callback(emptyList())
            }
    }

    // =========================
    // ESCUCHAR ASISTENCIA HOY
    // =========================

    fun escucharAsistenciaHoy(
        matriculaAlumno: String,
        grupoId: String,
        onStatusChanged: (Boolean) -> Unit
    ): ListenerRegistration {

        val fechaHoy = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).format(Date())

        val query = db.collection("asistencias")
            .whereEqualTo("matriculaAlumno", matriculaAlumno)
            .whereEqualTo("grupoId", grupoId)
            .whereEqualTo("fecha", fechaHoy)

        return query.addSnapshotListener { snapshot, _ ->

            val presente = snapshot != null && !snapshot.isEmpty

            onStatusChanged(presente)
        }
    }
}