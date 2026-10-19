package com.masfitatech.koprafin.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object ImageUtils {

    data class PreparedImage(
        val part: MultipartBody.Part,
        val sizeBytes: Int,
        val width: Int,
        val height: Int
    )

    /**
     * Membaca gambar dari [uri], memperkecil sisi terpanjang ke [maxSide] px,
     * lalu kompres JPEG sampai ukurannya <= [maxBytes]. Dijalankan di IO thread.
     */
    suspend fun prepareImagePart(
        context: Context,
        uri: Uri,
        fieldName: String = "image",
        maxSide: Int = 1600,
        maxBytes: Int = 1_500_000
    ): PreparedImage = withContext(Dispatchers.IO) {
        val resolver = context.applicationContext.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            ?: throw IOException("Gambar tidak dapat dibaca")
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw IOException("File yang dipilih bukan gambar yang valid")
        }

        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxSide) sample *= 2

        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: throw IOException("Gambar tidak dapat dibaca")

        val scale = min(1f, maxSide.toFloat() / max(decoded.width, decoded.height))
        val w = (decoded.width * scale).roundToInt().coerceAtLeast(1)
        val h = (decoded.height * scale).roundToInt().coerceAtLeast(1)

        // Gambar ulang di atas latar putih (PNG transparan tidak jadi hitam saat jadi JPEG)
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        Canvas(output).apply {
            drawColor(Color.WHITE)
            drawBitmap(decoded, null, Rect(0, 0, w, h), Paint(Paint.FILTER_BITMAP_FLAG))
        }
        decoded.recycle()

        var quality = 88
        var bytes: ByteArray
        do {
            val bos = ByteArrayOutputStream()
            output.compress(Bitmap.CompressFormat.JPEG, quality, bos)
            bytes = bos.toByteArray()
            quality -= 10
        } while (bytes.size > maxBytes && quality >= 50)
        output.recycle()

        val body = bytes.toRequestBody("image/jpeg".toMediaType())
        val part = MultipartBody.Part.createFormData(
            fieldName, "banner_${System.currentTimeMillis()}.jpg", body
        )
        PreparedImage(part, bytes.size, w, h)
    }

    fun readableSize(bytes: Int): String =
        if (bytes >= 1_000_000) String.format("%.1f MB", bytes / 1_000_000f)
        else "${(bytes / 1000f).roundToInt()} KB"
}
