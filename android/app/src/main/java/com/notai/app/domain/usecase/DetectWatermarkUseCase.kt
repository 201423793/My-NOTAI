package com.notai.app.domain.usecase

import android.net.Uri
import com.notai.app.domain.model.DetectionResult
import com.notai.app.util.ImageCompressor
import com.notai.app.util.PlatformDetector
import javax.inject.Inject

class DetectWatermarkUseCase @Inject constructor(
    private val platformDetector: PlatformDetector,
    private val imageCompressor: ImageCompressor
) {
    operator fun invoke(imageUri: Uri): DetectionResult {
        val (width, height) = imageCompressor.getImageDimensions(imageUri)
        val metadata = imageCompressor.getImageMetadata(imageUri)
        return platformDetector.detect(width, height, metadata)
    }
}
