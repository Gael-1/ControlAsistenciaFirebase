package com.equipo1.controlasistencia.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FieldPath

class AlumnoRepository {
    private val db = FirebaseFirestore.getInstance()

    // Obtener alumnos de un grupo (matrícula como string para ID, pero guardamos número en campo opcional)
    fun obtenerAlumnos(grupoId: String, callback: (List<Pair<String, String>>) -> Unit) {
        db.collection("grupos").document(grupoId).collection("alumnos")
            .get()
            .addOnSuccessListener { snapshot ->
                val lista = snapshot.documents.mapNotNull { doc ->
                    doc.id to (doc.getString("nombre") ?: "")
                }
                callback(lista)
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    // Escuchar alumnos en tiempo real (para actualización automática)
    fun escucharAlumnos(grupoId: String, callback: (List<Pair<String, String>>) -> Unit): ListenerRegistration {
        return db.collection("grupos").document(grupoId).collection("alumnos")
            .addSnapshotListener { snapshot, _ ->
                val lista = snapshot?.documents?.mapNotNull { doc ->
                    doc.id to (doc.getString("nombre") ?: "")
                } ?: emptyList()
                callback(lista)
            }
    }

    // Agregar alumno a un grupo (con matrícula como número)
    fun agregarAlumnoAGrupo(grupoId: String, matricula: String, nombre: String, callback: (Boolean, String?) -> Unit) {
        val matriculaNum = matricula.toLongOrNull()
        if (matriculaNum == null) {
            callback(false, "Matrícula inválida")
            return
        }
        val grupoRef = db.collection("grupos").document(grupoId)
        val alumnoEnGrupoRef = grupoRef.collection("alumnos").document(matricula) // ID como string (consistente)
        val studentGroupRef = db.collection("student_groups").document()

        grupoRef.get().addOnSuccessListener { grupoDoc ->
            val nombreGrupo = grupoDoc.getString("nombre") ?: ""
            val batch = db.batch()
            batch.set(alumnoEnGrupoRef, mapOf("nombre" to nombre, "matricula" to matriculaNum))
            batch.set(studentGroupRef, mapOf(
                "matricula" to matriculaNum,
                "grupoId" to grupoId,
                "nombreGrupo" to nombreGrupo,
                "nombreAlumno" to nombre
            ))
            batch.commit()
                .addOnSuccessListener { callback(true, null) }
                .addOnFailureListener { e -> callback(false, e.message) }
        }.addOnFailureListener { e -> callback(false, e.message) }
    }

    // Obtener grupos de un alumno (con matrícula como número)
    fun obtenerGruposDelAlumno(matricula: String, callback: (List<Pair<String, String>>) -> Unit) {
        val matriculaNum = matricula.toLongOrNull()
        if (matriculaNum == null) {
            callback(emptyList())
            return
        }
        db.collection("student_groups")
            .whereEqualTo("matricula", matriculaNum)
            .get()
            .addOnSuccessListener { snapshot ->
                val grupoIds = snapshot.documents.mapNotNull { it.getString("grupoId") }
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
                    .addOnFailureListener { callback(emptyList()) }
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    // Obtener historial de asistencias de un alumno (opcional filtrar por grupo)
    fun obtenerAsistenciasAlumno(matricula: String, grupoId: String? = null, callback: (List<Map<String, Any>>) -> Unit) {
        val matriculaNum = matricula.toLongOrNull()
        if (matriculaNum == null) {
            callback(emptyList())
            return
        }
        var query = db.collection("asistencias").whereEqualTo("matriculaAlumno", matriculaNum)
        if (grupoId != null) {
            query = query.whereEqualTo("grupoId", grupoId)
        }
        query.orderBy("fecha", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val lista = snapshot.documents.mapNotNull { it.data }
                callback(lista)
            }
            .addOnFailureListener { callback(emptyList()) }
    }
}