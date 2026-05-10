package com.equipo1.controlasistencia.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class AuthRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "AuthRepo"

    fun login(
        matricula: String,
        password: String,
        callback: (Boolean, String?, String?, String?, String?) -> Unit
    ) {
        val matriculaNum = matricula.toLongOrNull()
        if (matriculaNum == null) {
            callback(false, "Matrícula inválida", null, null, null)
            return
        }
        buscarUsuarioPorMatricula(
            matriculaNum = matriculaNum,
            matriculaTexto = matricula,
            onSuccess = { doc ->
                if (doc == null) {
                    callback(false, "Matrícula no encontrada", null, null, null)
                    return@buscarUsuarioPorMatricula
                }

                val passBD = doc.getString("contraseña")
                val nombre = doc.getString("nombre") ?: ""
                val rol = doc.getString("rol") ?: "alumno"
                // ⭐️ Devuelve la matrícula como string (NO el ID del documento)
                val matriculaReal = obtenerMatriculaComoTexto(doc) ?: matricula
                if (passBD == password) {
                    sincronizarConFirebaseAuth(matriculaReal, password, nombre, doc.id)
                    callback(true, null, rol, nombre, matriculaReal) // ← aquí el número
                } else {
                    callback(false, "Contraseña incorrecta", null, null, null)
                }
            },
            onFailure = { e ->
                callback(false, "Error de conexión: ${e.message}", null, null, null)
            }
        )
    }

    private fun sincronizarConFirebaseAuth(matricula: String, password: String, nombre: String, documentoId: String) {
        val email = "$matricula@controlasistencia.local"
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                authResult.user?.updateProfile(com.google.firebase.auth.UserProfileChangeRequest.Builder().setDisplayName(nombre).build())
                db.collection("usuarios").document(documentoId).update("firebaseUid", authResult.user?.uid)
            }
            .addOnFailureListener { e ->
                if (e.message?.contains("already in use") != true) Log.w(TAG, "Error en sincronización: ${e.message}")
            }
    }

    private fun buscarUsuarioPorMatricula(
        matriculaNum: Long,
        matriculaTexto: String,
        onSuccess: (DocumentSnapshot?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val consultas = listOf(
            "matricula" to matriculaNum,
            "matricula" to matriculaTexto,
            "matrícula" to matriculaNum,
            "matrícula" to matriculaTexto
        )
        buscarEnConsultas(consultas, onSuccess, onFailure)
    }

    private fun buscarEnConsultas(
        consultas: List<Pair<String, Any>>,
        onSuccess: (DocumentSnapshot?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val consulta = consultas.firstOrNull()
        if (consulta == null) {
            onSuccess(null)
            return
        }

        db.collection("usuarios")
            .whereEqualTo(consulta.first, consulta.second)
            .get()
            .addOnSuccessListener { snapshot ->
                procesarResultadoBusqueda(snapshot, consultas, onSuccess, onFailure)
            }
            .addOnFailureListener(onFailure)
    }

    private fun procesarResultadoBusqueda(
        snapshot: QuerySnapshot,
        consultas: List<Pair<String, Any>>,
        onSuccess: (DocumentSnapshot?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (!snapshot.isEmpty) {
            onSuccess(snapshot.documents[0])
            return
        }
        buscarEnConsultas(consultas.drop(1), onSuccess, onFailure)
    }

    private fun obtenerMatriculaComoTexto(doc: DocumentSnapshot): String? {
        return doc.getLong("matricula")?.toString()
            ?: doc.getString("matricula")
            ?: doc.getLong("matrícula")?.toString()
            ?: doc.getString("matrícula")
    }

    fun enviarCorreoRestablecimiento(matricula: String, onResult: (Boolean, String?) -> Unit) {
        val matriculaNum = matricula.toLongOrNull()
        if (matriculaNum == null) {
            onResult(false, "Matrícula inválida")
            return
        }
        buscarUsuarioPorMatricula(
            matriculaNum = matriculaNum,
            matriculaTexto = matricula,
            onSuccess = { doc ->
                if (doc == null) {
                    onResult(false, "Matrícula no registrada")
                    return@buscarUsuarioPorMatricula
                }

                val correo = doc.getString("correo")
                if (correo.isNullOrEmpty() || !correo.contains("@")) {
                    onResult(false, "No hay correo electrónico registrado")
                } else {
                    auth.sendPasswordResetEmail(correo)
                        .addOnSuccessListener { onResult(true, "Correo enviado a ${enmascararCorreo(correo)}") }
                        .addOnFailureListener { e -> onResult(false, "Error al enviar: ${e.message}") }
                }
            },
            onFailure = { e -> onResult(false, "Error de conexión: ${e.message}") }
        )
    }

    private fun enmascararCorreo(correo: String): String {
        val partes = correo.split("@")
        if (partes.size != 2) return correo
        val nombre = partes[0]
        val dominio = partes[1]
        return if (nombre.length > 3) "${nombre.take(2)}***${nombre.takeLast(1)}@$dominio"
        else "${nombre.first()}***@$dominio"
    }
}