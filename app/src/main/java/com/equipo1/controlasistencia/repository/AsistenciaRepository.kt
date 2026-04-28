package com.equipo1.controlasistencia.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class AsistenciaRepository {
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "AsistenciaRepo"

    // Registrar asistencia del alumno al escanear QR
    fun registrarAsistenciaAlumno(
        grupoId: String,
        fecha: String,
        matriculaAlumno: String,
        token: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val matriculaNum = matriculaAlumno.toLongOrNull()
        if (matriculaNum == null) {
            callback(false, "Matrícula inválida")
            return
        }

        db.collection("asistencia_tokens")
            .whereEqualTo("grupoId", grupoId)
            .whereEqualTo("fecha", fecha)
            .whereEqualTo("token", token)
            .get()
            .addOnSuccessListener { tokenSnap ->
                if (tokenSnap.isEmpty) {
                    callback(false, "Token inválido o expirado")
                    return@addOnSuccessListener
                }
                db.collection("asistencias")
                    .whereEqualTo("grupoId", grupoId)
                    .whereEqualTo("fecha", fecha)
                    .whereEqualTo("matriculaAlumno", matriculaNum)
                    .get()
                    .addOnSuccessListener { existing ->
                        if (existing.isEmpty) {
                            val asistencia = hashMapOf(
                                "grupoId" to grupoId,
                                "fecha" to fecha,
                                "matriculaAlumno" to matriculaNum,
                                "timestamp" to Date(),
                                "token" to token,
                                "estado" to "presente"
                            )
                            db.collection("asistencias").add(asistencia)
                                .addOnSuccessListener { callback(true, null) }
                                .addOnFailureListener { e -> callback(false, e.message) }
                        } else {
                            callback(false, "Ya registraste asistencia hoy")
                        }
                    }
                    .addOnFailureListener { e -> callback(false, e.message) }
            }
            .addOnFailureListener { e -> callback(false, e.message) }
    }

    // Actualizar token dinámico para un grupo en una fecha (profesor genera QR)
    fun actualizarTokenAsistencia(grupoId: String, nuevoToken: String, fecha: String, callback: (Boolean) -> Unit) {
        val tokenDoc = hashMapOf(
            "grupoId" to grupoId,
            "fecha" to fecha,
            "token" to nuevoToken,
            "updatedAt" to Date()
        )
        db.collection("asistencia_tokens")
            .whereEqualTo("grupoId", grupoId)
            .whereEqualTo("fecha", fecha)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                snapshot.documents.forEach { doc -> batch.delete(doc.reference) }
                batch.set(db.collection("asistencia_tokens").document(), tokenDoc)
                batch.commit()
                    .addOnSuccessListener { callback(true) }
                    .addOnFailureListener { callback(false) }
            }
            .addOnFailureListener { callback(false) }
    }

    // Obtiene todas las fechas en que el profesor generó QR (sesiones de clase)
    fun obtenerFechasSesion(grupoId: String, callback: (List<String>) -> Unit) {
        db.collection("asistencia_tokens")
            .whereEqualTo("grupoId", grupoId)
            .get()
            .addOnSuccessListener { snapshot ->
                val fechas = snapshot.documents.mapNotNull { it.getString("fecha") }.distinct()
                callback(fechas)
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    // Obtiene lista de alumnos ausentes en una fecha específica
    fun obtenerAlumnosAusentesEnFecha(grupoId: String, fecha: String, callback: (List<Pair<String, String>>) -> Unit) {
        db.collection("grupos").document(grupoId).collection("alumnos")
            .get()
            .addOnSuccessListener { alumnosSnap ->
                val todosAlumnos = alumnosSnap.documents.mapNotNull { doc ->
                    doc.id to (doc.getString("nombre") ?: "")
                }
                if (todosAlumnos.isEmpty()) {
                    callback(emptyList())
                    return@addOnSuccessListener
                }
                db.collection("asistencias")
                    .whereEqualTo("grupoId", grupoId)
                    .whereEqualTo("fecha", fecha)
                    .get()
                    .addOnSuccessListener { asistenciasSnap ->
                        val matriculasPresentes = asistenciasSnap.documents.mapNotNull { doc ->
                            doc.getLong("matriculaAlumno")?.toString()
                        }
                        val ausentes = todosAlumnos.filterNot { (matricula, _) ->
                            matriculasPresentes.contains(matricula)
                        }
                        callback(ausentes)
                    }
                    .addOnFailureListener { callback(emptyList()) }
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    // NUEVO: Obtiene lista de alumnos presentes en una fecha específica
    fun obtenerAlumnosPresentesEnFecha(grupoId: String, fecha: String, callback: (List<Pair<String, String>>) -> Unit) {
        db.collection("asistencias")
            .whereEqualTo("grupoId", grupoId)
            .whereEqualTo("fecha", fecha)
            .get()
            .addOnSuccessListener { asistenciasSnap ->
                val matriculasPresentes = asistenciasSnap.documents.mapNotNull { doc ->
                    doc.getLong("matriculaAlumno")?.toString()
                }
                if (matriculasPresentes.isEmpty()) {
                    callback(emptyList())
                    return@addOnSuccessListener
                }
                // Obtener nombres de esos alumnos desde la subcolección alumnos
                val groupRef = db.collection("grupos").document(grupoId)
                val alumnosList = mutableListOf<Pair<String, String>>()
                var remaining = matriculasPresentes.size
                if (remaining == 0) {
                    callback(emptyList())
                    return@addOnSuccessListener
                }
                matriculasPresentes.forEach { matricula ->
                    groupRef.collection("alumnos").document(matricula).get()
                        .addOnSuccessListener { doc ->
                            val nombre = doc.getString("nombre") ?: ""
                            alumnosList.add(matricula to nombre)
                            remaining--
                            if (remaining == 0) callback(alumnosList)
                        }
                        .addOnFailureListener {
                            remaining--
                            if (remaining == 0) callback(alumnosList)
                        }
                }
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    // Método auxiliar para reporte de porcentajes (ya existente)
    fun obtenerReporteGrupo(grupoId: String, callback: (List<Pair<String, Int>>) -> Unit) {
        db.collection("grupos").document(grupoId).collection("alumnos")
            .get()
            .addOnSuccessListener { alumnosSnap ->
                val alumnos = alumnosSnap.documents.mapNotNull { doc ->
                    doc.id to (doc.getString("nombre") ?: "")
                }
                if (alumnos.isEmpty()) {
                    callback(emptyList())
                    return@addOnSuccessListener
                }
                db.collection("asistencias")
                    .whereEqualTo("grupoId", grupoId)
                    .get()
                    .addOnSuccessListener { asistenciasSnap ->
                        val fechasTotales = asistenciasSnap.documents.mapNotNull { it.getString("fecha") }.distinct().size
                        if (fechasTotales == 0) {
                            callback(alumnos.map { it.first to 0 })
                            return@addOnSuccessListener
                        }
                        val asistenciasPorAlumno = asistenciasSnap.documents.groupBy { it.getLong("matriculaAlumno")?.toString() ?: "" }
                        val resultado = alumnos.map { (matricula, nombre) ->
                            val asistencias = asistenciasPorAlumno[matricula]?.size ?: 0
                            val porcentaje = (asistencias * 100) / fechasTotales
                            nombre to porcentaje
                        }
                        callback(resultado)
                    }
            }
    }
}