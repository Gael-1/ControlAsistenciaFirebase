package com.equipo1.controlasistencia.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

/**
 * Función que convierte un String en un Bitmap de código QR.
 * @param content El texto que contendrá el QR (ej. grupoId|fecha|token).
 * @param size El tamaño en píxeles (ancho y alto) del QR generado.
 * @return Un Bitmap en blanco y negro del código QR.
 */
fun generarQrBitmap(content: String, size: Int = 512): Bitmap? {
    return try {
        // 1. Usamos ZXing para codificar el contenido en una matriz de bits
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            content,
            BarcodeFormat.QR_CODE, // Tipo de código
            size, // Ancho
            size  // Alto
        )

        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)

        // 2. Convertimos la matriz de bits en un array de colores (píxeles)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                // Si el bit es true, pintamos negro; si es false, pintamos blanco
                pixels[offset + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }

        // 3. Creamos el Bitmap final a partir del array de píxeles
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }

    } catch (e: Exception) {
        e.printStackTrace()
        null // Si algo falla (ej. texto muy largo), devolvemos nulo
    }
}