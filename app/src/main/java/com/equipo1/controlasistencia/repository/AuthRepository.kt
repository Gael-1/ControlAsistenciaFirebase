package com.equipo1.controlasistencia.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
        db.collection("usuarios")
            .whereEqualTo("matricula", matriculaNum)  // ← campo sin acento
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    callback(false, "Matrícula no encontrada", null, null, null)
                    return@addOnSuccessListener
                }

                val doc = snapshot.documents[0]
                val passBD = doc.getString("contraseña")
                val nombre = doc.getString("nombre") ?: ""
                val rol = doc.getString("rol") ?: "alumno"
                // ⭐️ Devuelve la matrícula numérica como string (NO el ID del documento)
                val matriculaReal = doc.getLong("matricula")?.toString() ?: matricula
                if (passBD == password) {
                    sincronizarConFirebaseAuth(matricula, password, nombre, doc.id)
                    callback(true, null, rol, nombre, matriculaReal) // ← aquí el número
                } else {
                    callback(false, "Contraseña incorrecta", null, null, null)
                }
            }
            .addOnFailureListener { e ->
                callback(false, "Error de conexión: ${e.message}", null, null, null)
            }
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

    fun enviarCorreoRestablecimiento(matricula: String, onResult: (Boolean, String?) -> Unit) {
        val matriculaNum = matricula.toLongOrNull()
        if (matriculaNum == null) {
            onResult(false, "Matrícula inválida")
            return
        }
        db.collection("usuarios")
            .whereEqualTo("matricula", matriculaNum)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    onResult(false, "Matrícula no registrada")
                    return@addOnSuccessListener
                }
                val correo = snapshot.documents[0].getString("correo")
                if (correo.isNullOrEmpty() || !correo.contains("@")) {
                    onResult(false, "No hay correo electrónico registrado")
                } else {
                    auth.sendPasswordResetEmail(correo)
                        .addOnSuccessListener { onResult(true, "Correo enviado a ${enmascararCorreo(correo)}") }
                        .addOnFailureListener { e -> onResult(false, "Error al enviar: ${e.message}") }
                }
            }
            .addOnFailureListener { e -> onResult(false, "Error de conexión: ${e.message}") }
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