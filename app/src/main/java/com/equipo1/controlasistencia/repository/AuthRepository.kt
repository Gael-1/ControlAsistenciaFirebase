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
        identificador: String,
        password: String,
        tipoAcceso: String,
        callback: (Boolean, String?, String?, String?, String?) -> Unit
    ) {
        val identificadorNum = identificador.toLongOrNull()

        if (identificadorNum == null) {
            val campo = if (tipoAcceso == TIPO_EMPLEADO) {
                "Número de socio"
            } else {
                "Matrícula"
            }

            callback(false, "$campo inválido", null, null, null)
            return
        }

        buscarUsuarioPorAcceso(
            identificadorNum = identificadorNum,
            identificadorTexto = identificador,
            tipoAcceso = tipoAcceso,
            onSuccess = { doc ->
                if (doc == null) {
                    val mensaje = if (tipoAcceso == TIPO_EMPLEADO) {
                        "Número de socio no encontrado"
                    } else {
                        "Matrícula no encontrada"
                    }

                    callback(false, mensaje, null, null, null)
                    return@buscarUsuarioPorAcceso
                }

                val passBD = doc.getString("contraseña")
                val nombre = doc.getString("nombre") ?: ""
                val rol = doc.getString("rol") ?: "alumno"
                val identificadorReal = obtenerIdentificadorComoTexto(doc, tipoAcceso)
                    ?: identificador

                if (passBD == password) {
                    sincronizarConFirebaseAuth(
                        identificador = identificadorReal,
                        password = password,
                        nombre = nombre,
                        documentoId = doc.id
                    )

                    callback(true, null, rol, nombre, identificadorReal)
                } else {
                    callback(false, "Contraseña incorrecta", null, null, null)
                }
            },
            onFailure = { e ->
                callback(false, "Error de conexión: ${e.message}", null, null, null)
            }
        )
    }

    private fun sincronizarConFirebaseAuth(
        identificador: String,
        password: String,
        nombre: String,
        documentoId: String
    ) {
        val email = "$identificador@controlasistencia.local"

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                authResult.user?.updateProfile(
                    com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(nombre)
                        .build()
                )

                db.collection("usuarios")
                    .document(documentoId)
                    .update("firebaseUid", authResult.user?.uid)
            }
            .addOnFailureListener { e ->
                if (e.message?.contains("already in use") != true) {
                    Log.w(TAG, "Error en sincronización: ${e.message}")
                }
            }
    }

    private fun buscarUsuarioPorAcceso(
        identificadorNum: Long,
        identificadorTexto: String,
        tipoAcceso: String,
        onSuccess: (DocumentSnapshot?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val consultas = if (tipoAcceso == TIPO_EMPLEADO) {
            consultasNumeroSocio(identificadorNum, identificadorTexto)
        } else {
            consultasMatricula(identificadorNum, identificadorTexto)
        }

        buscarEnConsultas(
            consultas = consultas,
            rolesPermitidos = rolesPermitidos(tipoAcceso),
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    private fun buscarUsuarioPorMatricula(
        matriculaNum: Long,
        matriculaTexto: String,
        onSuccess: (DocumentSnapshot?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        buscarEnConsultas(
            consultas = consultasMatricula(matriculaNum, matriculaTexto),
            rolesPermitidos = null,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    private fun consultasMatricula(
        numero: Long,
        texto: String
    ): List<Pair<String, Any>> {
        return listOf(
            "matricula" to numero,
            "matricula" to texto,
            "matrícula" to numero,
            "matrícula" to texto
        )
    }

    private fun consultasNumeroSocio(
        numero: Long,
        texto: String
    ): List<Pair<String, Any>> {
        return listOf(
            "numeroSocio" to numero,
            "numeroSocio" to texto,
            "númeroSocio" to numero,
            "númeroSocio" to texto,
            "numSocio" to numero,
            "numSocio" to texto,
            "socio" to numero,
            "socio" to texto,
            "profesorSocio" to numero,
            "profesorSocio" to texto,
            "numero_socio" to numero,
            "numero_socio" to texto
        )
    }

    private fun rolesPermitidos(tipoAcceso: String): Set<String> {
        return if (tipoAcceso == TIPO_EMPLEADO) {
            setOf("admin", "profesor")
        } else {
            setOf("alumno")
        }
    }

    private fun buscarEnConsultas(
        consultas: List<Pair<String, Any>>,
        rolesPermitidos: Set<String>?,
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
                procesarResultadoBusqueda(
                    snapshot = snapshot,
                    consultas = consultas,
                    rolesPermitidos = rolesPermitidos,
                    onSuccess = onSuccess,
                    onFailure = onFailure
                )
            }
            .addOnFailureListener(onFailure)
    }

    private fun procesarResultadoBusqueda(
        snapshot: QuerySnapshot,
        consultas: List<Pair<String, Any>>,
        rolesPermitidos: Set<String>?,
        onSuccess: (DocumentSnapshot?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val doc = snapshot.documents.firstOrNull { document ->
            val rol = document.getString("rol") ?: "alumno"
            rolesPermitidos == null || rol in rolesPermitidos
        }

        if (doc != null) {
            onSuccess(doc)
            return
        }

        buscarEnConsultas(
            consultas = consultas.drop(1),
            rolesPermitidos = rolesPermitidos,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    private fun obtenerIdentificadorComoTexto(
        doc: DocumentSnapshot,
        tipoAcceso: String
    ): String? {
        return if (tipoAcceso == TIPO_EMPLEADO) {
            obtenerNumeroSocioComoTexto(doc)
        } else {
            obtenerMatriculaComoTexto(doc)
        }
    }

    private fun obtenerMatriculaComoTexto(doc: DocumentSnapshot): String? {
        return doc.getLong("matricula")?.toString()
            ?: doc.getString("matricula")
            ?: doc.getLong("matrícula")?.toString()
            ?: doc.getString("matrícula")
    }

    private fun obtenerNumeroSocioComoTexto(doc: DocumentSnapshot): String? {
        return doc.getLong("numeroSocio")?.toString()
            ?: doc.getString("numeroSocio")
            ?: doc.getLong("númeroSocio")?.toString()
            ?: doc.getString("númeroSocio")
            ?: doc.getLong("numSocio")?.toString()
            ?: doc.getString("numSocio")
            ?: doc.getLong("socio")?.toString()
            ?: doc.getString("socio")
            ?: doc.getLong("profesorSocio")?.toString()
            ?: doc.getString("profesorSocio")
            ?: doc.getLong("numero_socio")?.toString()
            ?: doc.getString("numero_socio")
    }

    fun enviarCorreoRestablecimiento(
        matricula: String,
        onResult: (Boolean, String?) -> Unit
    ) {
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
                        .addOnSuccessListener {
                            onResult(true, "Correo enviado a ${enmascararCorreo(correo)}")
                        }
                        .addOnFailureListener { e ->
                            onResult(false, "Error al enviar: ${e.message}")
                        }
                }
            },
            onFailure = { e ->
                onResult(false, "Error de conexión: ${e.message}")
            }
        )
    }

    private fun enmascararCorreo(correo: String): String {
        val partes = correo.split("@")

        if (partes.size != 2) {
            return correo
        }

        val nombre = partes[0]
        val dominio = partes[1]

        return if (nombre.le ngth > 3) {
            "${nombre.take(2)}***${nombre.takeLast(1)}@$dominio"
        } else {
            "${nombre.first()}***@$dominio"
        }
    }

    companion object {
        const val TIPO_ALUMNO = "alumno"
        const val TIPO_EMPLEADO = "empleado"
    }
}