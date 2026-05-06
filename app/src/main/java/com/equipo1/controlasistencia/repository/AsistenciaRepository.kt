package com.equipo1.controlasistencia.repository

import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class AsistenciaRepository {
    private val db = FirebaseFirestore.getInstance()

    // Registrar asistencia de un alumno al escanear QR
    fun registrarAsistenciaAlumno(
        grupoId: String,
        fecha: String,
        matriculaAlumno: String,
        token: String,
        callback: (Boolean, String?) -> Unit
    ) {
        // Primero verificar que el token sea válido para ese grupo y fecha
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

                // Verificar que el alumno no haya registrado ya asistencia hoy
                db.collection("asistencias")
                    .whereEqualTo("grupoId", grupoId)
                    .whereEqualTo("fecha", fecha)
                    .whereEqualTo("matriculaAlumno", matriculaAlumno)
                    .get()
                    .addOnSuccessListener { existing ->
                        if (existing.isEmpty) {
                            val asistencia = hashMapOf(
                                "grupoId" to grupoId,
                                "fecha" to fecha,
                                "matriculaAlumno" to matriculaAlumno,
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
            }
            .addOnFailureListener { e -> callback(false, e.message) }
    }

    // Actualizar token dinámico para un grupo en una fecha
    fun actualizarTokenAsistencia(grupoId: String, nuevoToken: String, fecha: String, callback: (Boolean) -> Unit) {
        val tokenDoc = db.collection("asistencia_tokens").document()
        val data = hashMapOf(
            "grupoId" to grupoId,
            "fecha" to fecha,
            "token" to nuevoToken,
            "updatedAt" to Date()
        )
        // Sobrescribir si ya existe (usamos set con merge o query + delete)
        db.collection("asistencia_tokens")
            .whereEqualTo("grupoId", grupoId)
            .whereEqualTo("fecha", fecha)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.set(tokenDoc, data)
                batch.commit()
                    .addOnSuccessListener { callback(true) }
                    .addOnFailureListener { callback(false) }
            }
    }

    // Obtener reporte de asistencia por grupo (porcentajes)
    fun obtenerReporteGrupo(grupoId: String, callback: (List<Pair<String, Int>>) -> Unit) {
        // Obtener todos los alumnos del grupo
        db.collection("grupos").document(grupoId)
            .collection("alumnos")
            .get()
            .addOnSuccessListener { alumnosSnap ->
                val alumnos = alumnosSnap.documents.mapNotNull { doc ->
                    doc.id to (doc.getString("nombre") ?: "")
                }
                if (alumnos.isEmpty()) {
                    callback(emptyList())
                    return@addOnSuccessListener
                }

                // Obtener todas las fechas distintas con asistencia
                db.collection("asistencias")
                    .whereEqualTo("grupoId", grupoId)
                    .get()
                    .addOnSuccessListener { asistenciasSnap ->
                        val fechasTotales = asistenciasSnap.documents.mapNotNull { it.getString("fecha") }.distinct().size
                        if (fechasTotales == 0) {
                            callback(alumnos.map { it.first to 0 })
                            return@addOnSuccessListener
                        }

                        val asistenciasPorAlumno = asistenciasSnap.documents.groupBy { it.getString("matriculaAlumno") ?: "" }
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