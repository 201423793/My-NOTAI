package com.notai.app.domain.usecase

import android.graphics.BitmapFactory
import com.notai.app.data.local.entity.HistoryEntity
import com.notai.app.data.repository.HistoryRepository
import com.notai.app.data.repository.UserRepository
import com.notai.app.domain.model.Platform
import com.notai.app.domain.model.ProcessingResult
import com.notai.app.domain.model.WatermarkRegion
import com.notai.app.util.MaskGenerator
import org.opencv.photo.Photo
import java.io.File
import javax.inject.Inject

class RemoveWatermarkUseCase @Inject constructor(
    private val maskGenerator: MaskGenerator,
    private val historyRepository: HistoryRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        originalFile: File,
        platform: Platform,
        region: WatermarkRegion
    ): Result<ProcessingResult> = runCatching {
        val startTime = System.currentTimeMillis()

        // 1. Decode image
        val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath)
            ?: throw IllegalArgumentException("无法解码图片")

        // 2. Convert to OpenCV Mat
        val src = maskGenerator.bitmapToMat(bitmap)
        bitmap.recycle()

        // 3. Generate mask for watermark region
        val mask = maskGenerator.createMask(src.cols(), src.rows(), region)

        // 4. Inpaint using OpenCV Telea algorithm (radius 3px)
        val result = org.opencv.core.Mat()
        Photo.inpaint(src, mask, result, 3.0, Photo.INPAINT_TELEA)

        // 5. Save result to file
        val resultDir = File(originalFile.parentFile, "results").apply { mkdirs() }
        val resultFile = File(resultDir, "result_${System.currentTimeMillis()}.png")
        val resultBitmap = maskGenerator.matToBitmap(result)
        resultFile.outputStream().use { fos ->
            resultBitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos)
        }
        resultBitmap.recycle()

        // 6. Release Mats
        src.release()
        mask.release()
        result.release()

        val processingTime = System.currentTimeMillis() - startTime

        // 7. Save to history DB
        val historyId = historyRepository.insert(
            HistoryEntity(
                originalUri = originalFile.absolutePath,
                resultUri = resultFile.absolutePath,
                platform = platform.name,
                watermarkRegionX = region.x,
                watermarkRegionY = region.y,
                watermarkRegionW = region.w,
                watermarkRegionH = region.h,
                status = "completed",
                processedAt = System.currentTimeMillis(),
                processingTimeMs = processingTime,
                isFavorited = false
            )
        )

        userRepository.decrementCredits()
        userRepository.incrementProcessingCount()

        ProcessingResult(historyId = historyId, resultFile = resultFile, processingTimeMs = processingTime)
    }
}
