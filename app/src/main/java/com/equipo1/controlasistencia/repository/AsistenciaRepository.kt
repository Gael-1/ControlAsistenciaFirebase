package com.equipo1.controlasistencia.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class AsistenciaRepository {

    private val db = FirebaseFirestore.getInstance()

    /**
     * El profesor genera un nuevo token dinámico.
     * Se guarda en un documento específico por fecha dentro del grupo.
     */
    fun actualizarTokenAsistencia(
        grupoId: String,
        token: String,
        fecha: String,
        onResult: (Boolean) -> Unit
    ) {
        val datos = hashMapOf(
            "tokenActivo" to token,
            "fecha" to fecha,
            "estado" to "abierto",
            "ultimaActualizacion" to System.currentTimeMillis()
        )

        db.collection("grupos").document(grupoId)
            .collection("asistencias").document(fecha)
            .set(datos, SetOptions.merge())
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    /**
     * El alumno intenta registrarse.
     * Validamos que el token enviado coincida con el almacenado en la nube.
     */
    fun registrarAsistenciaAlumno(
        grupoId: String,
        fecha: String,
        alumnoId: String,
        tokenEnviado: String, // <--- Agregamos validación de seguridad
        onResult: (Boolean, String?) -> Unit
    ) {
        val docRef = db.collection("grupos").document(grupoId)
            .collection("asistencias").document(fecha)

        docRef.get().addOnSuccessListener { snapshot ->
            val tokenReal = snapshot.getString("tokenActivo")

            if (tokenReal == tokenEnviado) {
                // El token es correcto, procedemos a marcar la asistencia
                val actualizacion = hashMapOf<String, Any>(
                    "registros.$alumnoId" to true,
                    "ultimaModificacion" to System.currentTimeMillis()
                )

                docRef.update(actualizacion)
                    .addOnSuccessListener { onResult(true, "Asistencia exitosa") }
                    .addOnFailureListener {
                        // Si el documento no existe (fallback), lo creamos
                        val nuevoRegistro = hashMapOf(
                            "registros" to hashMapOf(alumnoId to true)
                        )
                        docRef.set(nuevoRegistro, SetOptions.merge())
                            .addOnSuccessListener { onResult(true, null) }
                            .addOnFailureListener { onResult(false, it.message) }
                    }
            } else {
                // Intento de fraude o código caducado
                onResult(false, "El código QR ha expirado o es inválido")
            }
        }.addOnFailureListener {
            onResult(false, "No se pudo conectar con el servidor")
        }
    }
}