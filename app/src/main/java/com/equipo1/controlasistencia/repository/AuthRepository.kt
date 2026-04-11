package com.equipo1.controlasistencia.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.equipo1.controlasistencia.model.Usuario

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    /**
     * Registra un nuevo usuario en Firebase Auth y guarda su perfil en Firestore
     */
    fun registrar(
        nombre: String,
        correo: String,
        password: String,
        rol: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(correo, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                val usuario = Usuario(
                    uid = uid,
                    nombre = nombre,
                    correo = correo,
                    rol = rol
                )

                // Guardamos el objeto usuario usando su UID como nombre del documento
                db.collection("usuarios")
                    .document(uid)
                    .set(usuario)
                    .addOnSuccessListener {
                        onResult(true, null)
                    }
                    .addOnFailureListener { e ->
                        onResult(false, "Error al guardar perfil: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, "Error al crear cuenta: ${e.message}")
            }
    }

    /**
     * Inicia sesión y recupera los datos (Rol y Nombre) desde Firestore
     */
    fun login(
        correo: String,
        password: String,
        onResult: (Boolean, String?, String?, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(correo, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                // Una vez logueado, vamos a Firestore por el nombre y rol
                db.collection("usuarios").document(uid).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val rol = document.getString("rol")
                            val nombre = document.getString("nombre")
                            onResult(true, null, rol, nombre)
                        } else {
                            onResult(false, "El perfil del usuario no existe en la base de datos.", null, null)
                        }
                    }
                    .addOnFailureListener { e ->
                        onResult(false, "Error de red al obtener perfil: ${e.message}", null, null)
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, "Correo o contraseña incorrectos", null, null)
            }
    }
}