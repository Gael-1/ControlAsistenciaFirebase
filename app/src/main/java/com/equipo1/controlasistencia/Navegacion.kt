package com.equipo1.controlasistencia

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.equipo1.controlasistencia.screens.*
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AppNavegacion() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        // =========================
        // LOGIN
        // =========================

        composable("login") {

            LoginScreen(

                onLoginSuccess = { rol, nombre, uid ->

                    when (rol) {

                        "admin" -> {
                            val nombreAdmin = if (nombre.isBlank()) "Admin" else nombre
                            navController.navigate("home_escolar/$nombreAdmin")
                        }

                        "profesor" -> {
                            navController.navigate("home_profesor/$uid/$nombre")
                        }

                        else -> {
                            navController.navigate("alumno_home/$uid/$nombre")
                        }
                    }
                },

                onNavigateToForgotPassword = {
                    navController.navigate("forgot_password")
                }
            )
        }

        // =========================
        // FORGOT PASSWORD
        // =========================

        composable("forgot_password") {

            ForgotPasswordScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // =========================
        // HOME ESCOLAR (ADMIN)
        // =========================

        composable("home_escolar/{nombreAdmin}") { backStackEntry ->

            val nombreAdmin = backStackEntry.arguments?.getString("nombreAdmin")
                ?.takeIf { it.isNotBlank() } ?: "Admin"

            HomeEscolarScreen(
                nombreAdmin = nombreAdmin,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                },
                onVerDetalleGrupo = { grupoId, nombreGrupo ->
                    // ADMIN → pantalla de gestión de alumnos
                    navController.navigate("detalle_grupo_admin/$grupoId/$nombreGrupo")
                }
            )
        }

        // =========================
        // DETALLE GRUPO ADMIN (agregar alumnos)
        // =========================

        composable("detalle_grupo_admin/{grupoId}/{nombreGrupo}") { backStackEntry ->
            val grupoId = backStackEntry.arguments?.getString("grupoId") ?: ""
            val nombreGrupo = backStackEntry.arguments?.getString("nombreGrupo") ?: ""
            DetalleGrupoAdminScreen(
                grupoId = grupoId,
                nombreGrupo = nombreGrupo,
                onBack = { navController.popBackStack() }
            )
        }

        // =========================
        // HOME PROFESOR
        // =========================

        composable("home_profesor/{matricula}/{nombre}") { backStackEntry ->

            val matricula = backStackEntry.arguments?.getString("matricula") ?: ""
            val nombre = backStackEntry.arguments?.getString("nombre") ?: ""

            HomeProfesorScreen(
                profesorMatricula = matricula,
                nombreProfesor = nombre,
                onGrupoClick = { grupoId, nombreGrupo ->
                    navController.navigate("detalle_grupo_profesor/$grupoId/$nombreGrupo")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // =========================
        // HOME ALUMNO
        // =========================

        composable("alumno_home/{matricula}/{nombre}") { backStackEntry ->

            val matricula = backStackEntry.arguments?.getString("matricula") ?: ""
            val nombre = backStackEntry.arguments?.getString("nombre") ?: ""

            AlumnoHomeScreen(
                matriculaAlumno = matricula,
                nombreAlumno = nombre,
                onEscanearClick = { grupoId, nombreGrupo ->
                    navController.navigate("scanner/$matricula/$grupoId/$nombreGrupo")
                },
                onHistorialClick = { grupoId, nombreGrupo ->
                    navController.navigate("historial/$matricula/$grupoId/$nombreGrupo")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                }
            )
        }

        // =========================
        // DETALLE GRUPO PROFESOR
        // =========================

        composable("detalle_grupo_profesor/{grupoId}/{nombreGrupo}") { backStackEntry ->

            val grupoId = backStackEntry.arguments?.getString("grupoId") ?: ""
            val nombreGrupo = backStackEntry.arguments?.getString("nombreGrupo") ?: ""

            DetalleGrupoProfesorScreen(
                nombreGrupo = nombreGrupo,
                onVerAlumnos = {
                    navController.navigate("lista_alumnos/$grupoId/$nombreGrupo/profesor")
                },
                onTomarAsistencia = {
                    navController.navigate("tomar_asistencia/$grupoId/$nombreGrupo")
                },
                onVerAsistenciaDia = {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("grupos")
                        .document(grupoId)
                        .get()
                        .addOnSuccessListener { doc ->
                            val fecha = doc.getString("fechaAsistenciaActual") ?: ""
                            val token = doc.getString("tokenAsistenciaActual") ?: ""
                            navController.navigate("lista_asistencia_dia/$grupoId/$nombreGrupo?fecha=$fecha&token=$token")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Navegacion", "Error: ${e.message}")
                            navController.navigate("lista_asistencia_dia/$grupoId/$nombreGrupo?fecha=&token=")
                        }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // =========================
        // LISTA ALUMNOS (solo lectura)
        // =========================

        composable("lista_alumnos/{grupoId}/{nombreGrupo}/{rol}") { backStackEntry ->

            val grupoId = backStackEntry.arguments?.getString("grupoId") ?: ""
            val nombreGrupo = backStackEntry.arguments?.getString("nombreGrupo") ?: ""
            val rol = backStackEntry.arguments?.getString("rol") ?: "profesor"

            ListaAlumnosScreen(
                grupoId = grupoId,
                nombreGrupo = nombreGrupo,
                esAdmin = rol == "admin",
                onBack = { navController.popBackStack() }
            )
        }

        // =========================
        // TOMAR ASISTENCIA (profesor)
        // =========================

        composable("tomar_asistencia/{grupoId}/{nombreGrupo}") { backStackEntry ->

            val grupoId = backStackEntry.arguments?.getString("grupoId") ?: ""
            val nombreGrupo = backStackEntry.arguments?.getString("nombreGrupo") ?: ""

            TomarAsistenciaScreen(
                grupoId = grupoId,
                nombreGrupo = nombreGrupo,
                onBack = { navController.popBackStack() }
            )
        }

        // =========================
        // SCANNER (alumno)
        // =========================

        composable("scanner/{matricula}/{grupoId}/{nombreGrupo}") { backStackEntry ->

            val matricula = backStackEntry.arguments?.getString("matricula") ?: ""
            val grupoId = backStackEntry.arguments?.getString("grupoId") ?: ""
            val nombreGrupo = backStackEntry.arguments?.getString("nombreGrupo") ?: ""

            ScannerScreen(
                matriculaAlumno = matricula,
                grupoIdEsperado = grupoId,
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // =========================
        // HISTORIAL ALUMNO
        // =========================

        composable("historial/{matricula}/{grupoId}/{nombreGrupo}") { backStackEntry ->

            val matricula = backStackEntry.arguments?.getString("matricula") ?: ""
            val grupoId = backStackEntry.arguments?.getString("grupoId") ?: ""
            val nombreGrupo = backStackEntry.arguments?.getString("nombreGrupo") ?: ""

            HistorialAsistenciaScreen(
                matriculaAlumno = matricula,
                grupoId = grupoId,
                nombreGrupo = nombreGrupo,
                onBack = { navController.popBackStack() }
            )
        }

        // =========================
        // GESTION ALUMNOS (admin) - (obsoleto, se usa DetalleGrupoAdminScreen)
        // =========================

        composable("gestion_alumnos/{grupoId}/{nombreGrupo}") { backStackEntry ->

            val grupoId = backStackEntry.arguments?.getString("grupoId") ?: ""
            val nombreGrupo = backStackEntry.arguments?.getString("nombreGrupo") ?: ""

            GestionAlumnosGrupoScreen(
                grupoId = grupoId,
                nombreGrupo = nombreGrupo,
                onBack = { navController.popBackStack() }
            )
        }

        // =========================
        // REPORTES
        // =========================

        composable("reportes/{grupoId}/{nombreGrupo}") { backStackEntry ->

            val grupoId = backStackEntry.arguments?.getString("grupoId") ?: ""
            val nombreGrupo = backStackEntry.arguments?.getString("nombreGrupo") ?: ""

            ReportesScreen(
                grupoId = grupoId,
                nombreGrupo = nombreGrupo,
                onBack = { navController.popBackStack() }
            )
        }

        // =========================
        // LISTA ASISTENCIA DIA (profesor)
        // =========================

        composable(
            route = "lista_asistencia_dia/{grupoId}/{nombreGrupo}?fecha={fecha}&token={token}",
            arguments = listOf(
                navArgument("grupoId") { type = NavType.StringType },
                navArgument("nombreGrupo") { type = NavType.StringType },
                navArgument("fecha") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                },
                navArgument("token") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->

            val grupoId = backStackEntry.arguments?.getString("grupoId") ?: ""
            val nombreGrupo = backStackEntry.arguments?.getString("nombreGrupo") ?: ""
            val fecha = backStackEntry.arguments?.getString("fecha") ?: ""
            val token = backStackEntry.arguments?.getString("token") ?: ""

            ListaAsistenciaDiaProfesorScreen(
                grupoId = grupoId,
                nombreGrupo = nombreGrupo,
                fechaSesion = fecha,
                token = token,
                onBack = { navController.popBackStack() }
            )
        }
    }
}