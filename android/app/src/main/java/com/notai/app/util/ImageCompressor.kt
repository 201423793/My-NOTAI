package com.notai.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageCompressor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun compress(sourceUri: Uri): File = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(sourceUri)
            ?: throw IllegalArgumentException("无法读取图片")
        val original = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        require(original.width >= Constants.MIN_DIMENSION && original.height >= Constants.MIN_DIMENSION) {
            "图片尺寸太小，最小 ${Constants.MIN_DIMENSION}x${Constants.MIN_DIMENSION}"
        }

        val scaled = if (original.width > Constants.MAX_DIMENSION || original.height > Constants.MAX_DIMENSION) {
            val ratio = minOf(
                Constants.MAX_DIMENSION.toFloat() / original.width,
                Constants.MAX_DIMENSION.toFloat() / original.height
            )
            Bitmap.createScaledBitmap(original, (original.width * ratio).toInt(), (original.height * ratio).toInt(), true)
        } else original

        val outputDir = File(context.cacheDir, "compressed").apply { mkdirs() }
        val outputFile = File(outputDir, "img_${System.currentTimeMillis()}.jpg")

        var quality = Constants.COMPRESS_QUALITY
        do {
            FileOutputStream(outputFile).use { fos ->
                scaled.compress(Bitmap.CompressFormat.JPEG, quality, fos)
            }
            quality -= 10
        } while (outputFile.length() > Constants.MAX_IMAGE_SIZE_BYTES && quality > 20)

        if (scaled !== original) scaled.recycle()
        original.recycle()

        outputFile
    }

    fun getImageDimensions(uri: Uri): Pair<Int, Int> {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
        return (options.outWidth to options.outHeight)
    }

    fun getImageMetadata(uri: Uri): String {
        val sb = StringBuilder()
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = androidx.exifinterface.media.ExifInterface(stream)
                exif.getAttribute(androidx.exifinterface.media.ExifInterface.TAG_IMAGE_DESCRIPTION)?.let {
                    sb.append("description:$it ")
                }
                exif.getAttribute(androidx.exifinterface.media.ExifInterface.TAG_SOFTWARE)?.let {
                    sb.append("software:$it ")
                }
                exif.getAttribute(androidx.exifinterface.media.ExifInterface.TAG_ARTIST)?.let {
                    sb.append("artist:$it ")
                }
                exif.getAttribute(androidx.exifinterface.media.ExifInterface.TAG_USER_COMMENT)?.let {
                    sb.append("comment:$it ")
                }
            }
        } catch (_: Exception) {}
        return sb.toString()
    }
}
