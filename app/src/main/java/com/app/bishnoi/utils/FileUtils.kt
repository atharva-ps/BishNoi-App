package com.app.bishnoi.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileUtils {

    fun compressImage(context: Context, uri: Uri, maxSizeKB: Int = 500): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            var quality = 100

            do {
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                outputStream.close()
                quality -= 10
            } while (file.length() / 1024 > maxSizeKB && quality > 0)

            bitmap.recycle()
            file
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
