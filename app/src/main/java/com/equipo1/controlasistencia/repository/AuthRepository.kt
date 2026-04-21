package com.equipo1.controlasistencia.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "AuthRepository"
        private const val EMAIL_DOMAIN = "@controlasistencia.local"
    }

    /**
     * Convierte matrícula a email virtual para Firebase Auth
     */
    private fun matriculaToEmail(matricula: String): String {
        return "$matricula$EMAIL_DOMAIN"
    }

    /**
     * Inicia sesión - FUENTE PRINCIPAL: FIRESTORE
     */
    fun login(
        matricula: String,
        password: String,
        onResult: (Boolean, String?, String?, String?, String?) -> Unit
    ) {
        val matriculaNumero = matricula.toLongOrNull()

        if (matriculaNumero == null) {
            onResult(false, "Matrícula inválida", null, null, null)
            return
        }

        Log.d(TAG, "🔐 Login - Matrícula: $matriculaNumero")

        // BUSCAR EN FIRESTORE (Fuente principal de verdad)
        db.collection("usuarios")
            .whereEqualTo("matricula", matriculaNumero)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val data = document.data

                    val nombre = data?.get("nombre")?.toString() ?: ""
                    val rol = data?.get("rol")?.toString() ?: "alumno"
                    val uid = document.id
                    val passwordGuardada = data?.get("contraseña")?.toString()
                    val correoReal = data?.get("correo")?.toString() ?: ""

                    Log.d(TAG, "📄 Usuario encontrado: $nombre, Rol: $rol")
                    Log.d(TAG, "🔑 Password en Firestore: ${passwordGuardada?.take(3)}***")
                    Log.d(TAG, "🔑 Password ingresada: ${password.take(3)}***")

                    // VERIFICAR CONTRASEÑA EN FIRESTORE
                    if (passwordGuardada == password) {
                        Log.d(TAG, "✅ Contraseña correcta en Firestore")

                        // Sincronizar con Firebase Auth (para funcionalidades futuras)
                        sincronizarConFirebaseAuth(matricula, password, nombre, document.id)

                        onResult(true, null, rol, nombre, uid)
                    } else {
                        Log.w(TAG, "❌ Contraseña incorrecta")
                        onResult(false, "Contraseña incorrecta", null, null, null)
                    }
                } else {
                    Log.e(TAG, "❌ Matrícula no encontrada: $matriculaNumero")
                    onResult(false, "Matrícula no encontrada", null, null, null)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al buscar usuario", e)
                onResult(false, "Error de conexión: ${e.message}", null, null, null)
            }
    }

    /**
     * Sincroniza el usuario con Firebase Auth (sin afectar el login)
     */
    private fun sincronizarConFirebaseAuth(
        matricula: String,
        password: String,
        nombre: String,
        documentoId: String
    ) {
        val emailVirtual = matriculaToEmail(matricula)

        // Intentar crear el usuario en Firebase Auth
        auth.createUserWithEmailAndPassword(emailVirtual, password)
            .addOnSuccessListener { authResult ->
                val firebaseUid = authResult.user?.uid
                Log.d(TAG, "✅ Usuario sincronizado con Firebase Auth. UID: $firebaseUid")

                // Actualizar perfil
                authResult.user?.updateProfile(
                    com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(nombre)
                        .build()
                )

                // Guardar UID en Firestore
                if (firebaseUid != null) {
                    db.collection("usuarios").document(documentoId)
                        .update("firebaseUid", firebaseUid)
                }
            }
            .addOnFailureListener { e ->
                if (e.message?.contains("already in use") == true) {
                    Log.d(TAG, "ℹ️ Usuario ya existe en Firebase Auth")
                } else {
                    Log.w(TAG, "⚠️ No se pudo sincronizar con Firebase Auth: ${e.message}")
                }
            }
    }

    /**
     * Cambia la contraseña del usuario - ACTUALIZA FIRESTORE
     */
    fun cambiarContraseña(
        uid: String,
        matricula: String,
        passwordActual: String,
        nuevaPassword: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val matriculaNumero = matricula.toLongOrNull()

        if (matriculaNumero == null) {
            onResult(false, "Matrícula inválida")
            return
        }

        if (nuevaPassword.length < 6) {
            onResult(false, "La contraseña debe tener al menos 6 caracteres")
            return
        }

        Log.d(TAG, "🔄 Cambiando contraseña para matrícula: $matriculaNumero")
        Log.d(TAG, "🔑 Password actual ingresada: ${passwordActual.take(3)}***")
        Log.d(TAG, "🔑 Nueva password: ${nuevaPassword.take(3)}***")

        // Buscar usuario en Firestore
        db.collection("usuarios")
            .whereEqualTo("matricula", matriculaNumero)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val passwordGuardada = document.getString("contraseña")

                    Log.d(TAG, "🔑 Password en Firestore: ${passwordGuardada?.take(3)}***")

                    // Verificar contraseña actual
                    if (passwordGuardada != passwordActual) {
                        Log.w(TAG, "❌ La contraseña actual no coincide")
                        onResult(false, "La contraseña actual es incorrecta")
                        return@addOnSuccessListener
                    }

                    Log.d(TAG, "✅ Contraseña actual verificada. Actualizando...")

                    // ACTUALIZAR EN FIRESTORE
                    val updates = hashMapOf<String, Any>(
                        "contraseña" to nuevaPassword
                    )

                    document.reference.update(updates)
                        .addOnSuccessListener {
                            Log.d(TAG, "✅✅✅ CONTRASEÑA ACTUALIZADA EN FIRESTORE")

                            // Intentar actualizar también en Firebase Auth (opcional)
                            actualizarPasswordEnFirebaseAuth(matricula, nuevaPassword)

                            onResult(true, "Contraseña actualizada correctamente")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "❌ Error al actualizar en Firestore", e)
                            onResult(false, "Error al actualizar: ${e.message}")
                        }
                } else {
                    Log.e(TAG, "❌ Usuario no encontrado")
                    onResult(false, "Usuario no encontrado")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al buscar usuario", e)
                onResult(false, "Error: ${e.message}")
            }
    }

    /**
     * Actualiza la contraseña en Firebase Auth (mejor esfuerzo)
     */
    private fun actualizarPasswordEnFirebaseAuth(matricula: String, nuevaPassword: String) {
        val emailVirtual = matriculaToEmail(matricula)

        // Intentar iniciar sesión y actualizar
        auth.signInWithEmailAndPassword(emailVirtual, nuevaPassword)
            .addOnFailureListener { e ->
                // Si falla el login, intentar crear el usuario
                if (e.message?.contains("no user record") == true) {
                    // No hacemos nada, ya que el login normal lo creará cuando sea necesario
                    Log.d(TAG, "ℹ️ Usuario no existe en Firebase Auth aún")
                }
            }
    }

    /**
     * Restablece la contraseña (solo administrador)
     */
    fun restablecerContraseña(
        matricula: String,
        nuevaPassword: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val matriculaNumero = matricula.toLongOrNull()

        if (matriculaNumero == null) {
            onResult(false, "Matrícula inválida")
            return
        }

        if (nuevaPassword.length < 6) {
            onResult(false, "La contraseña debe tener al menos 6 caracteres")
            return
        }

        Log.d(TAG, "🔄 Restableciendo contraseña para matrícula: $matriculaNumero")

        db.collection("usuarios")
            .whereEqualTo("matricula", matriculaNumero)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]

                    val updates = hashMapOf<String, Any>(
                        "contraseña" to nuevaPassword
                    )

                    document.reference.update(updates)
                        .addOnSuccessListener {
                            Log.d(TAG, "✅ Contraseña restablecida en Firestore")
                            onResult(true, "Contraseña restablecida correctamente")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "❌ Error al restablecer", e)
                            onResult(false, "Error: ${e.message}")
                        }
                } else {
                    onResult(false, "Usuario no encontrado")
                }
            }
            .addOnFailureListener { e ->
                onResult(false, "Error: ${e.message}")
            }
    }

    /**
     * Envía correo de restablecimiento
     */
    fun enviarCorreoRestablecimiento(
        matricula: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val matriculaNumero = matricula.toLongOrNull()

        if (matriculaNumero == null) {
            onResult(false, "Matrícula inválida")
            return
        }

        Log.d(TAG, "📧 Solicitando restablecimiento para: $matriculaNumero")

        db.collection("usuarios")
            .whereEqualTo("matricula", matriculaNumero)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val correo = document.getString("correo")

                    if (correo.isNullOrEmpty() || !correo.contains("@")) {
                        onResult(false, "No hay correo electrónico registrado. Contacta al administrador.")
                        return@addOnSuccessListener
                    }

                    // Enviar correo con Firebase Auth
                    auth.sendPasswordResetEmail(correo)
                        .addOnSuccessListener {
                            Log.d(TAG, "✅ Correo enviado a: $correo")
                            onResult(true, "Correo enviado a ${enmascararCorreo(correo)}")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "❌ Error al enviar correo", e)
                            onResult(false, "Error al enviar correo. Contacta al administrador.")
                        }
                } else {
                    onResult(false, "Matrícula no encontrada")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al buscar usuario", e)
                onResult(false, "Error de conexión")
            }
    }

    /**
     * Cierra sesión
     */
    fun logout() {
        auth.signOut()
        Log.d(TAG, "👋 Sesión cerrada")
    }

    /**
     * Obtiene el UID del usuario actual
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Verifica si hay una sesión activa
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Enmascara el correo para mostrar
     */
    private fun enmascararCorreo(correo: String): String {
        val partes = correo.split("@")
        if (partes.size != 2) return correo
        val nombre = partes[0]
        val dominio = partes[1]
        return if (nombre.length > 3) {
            "${nombre.take(2)}***${nombre.takeLast(1)}@$dominio"
        } else {
            "${nombre.first()}***@$dominio"
        }
    }
}